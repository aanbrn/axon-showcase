// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Projector backlog sampler tests")
class ShowcaseProjectorBacklogSamplerTests {

    private static final String TOPIC = "axon-showcase-events";

    private static final TopicPartition PARTITION_0 = new TopicPartition(TOPIC, 0);

    private static final TopicPartition PARTITION_1 = new TopicPartition(TOPIC, 1);

    private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    private final ShowcaseProjectorBacklogSampler sampler =
            new ShowcaseProjectorBacklogSampler(new KafkaProperties(), new ShowcaseProjectorProperties(), registry);

    @Test
    @DisplayName("A partition behind its end offset resolves to the distance")
    void behind_resolvesTheDistance() {
        var resolution = ShowcaseProjectorBacklogSampler.resolve(
                Map.of(PARTITION_0, 12L), Map.of(PARTITION_0, new OffsetAndMetadata(5L)));

        assertThat(resolution.distances()).containsEntry(PARTITION_0, 7L);
        assertThat(resolution.unresolved()).isEmpty();
    }

    @Test
    @DisplayName("A partition the group committed to but the topic no longer reports is unresolved")
    void vanishedPartition_isUnresolved() {
        var resolution =
                ShowcaseProjectorBacklogSampler.resolve(Map.of(), Map.of(PARTITION_0, new OffsetAndMetadata(5L)));

        assertThat(resolution.distances()).isEmpty();
        assertThat(resolution.unresolved()).containsExactly(PARTITION_0);
    }

    @Test
    @DisplayName("A partition the group never committed is unresolved")
    void uncommittedPartition_isUnresolved() {
        var resolution = ShowcaseProjectorBacklogSampler.resolve(Map.of(PARTITION_0, 12L), Map.of());

        assertThat(resolution.distances()).isEmpty();
        assertThat(resolution.unresolved()).containsExactly(PARTITION_0);
    }

    @Test
    @DisplayName("A committed offset past the end offset is unresolved")
    void committedPastTheEnd_isUnresolved() {
        var resolution = ShowcaseProjectorBacklogSampler.resolve(
                Map.of(PARTITION_0, 5L), Map.of(PARTITION_0, new OffsetAndMetadata(9L)));

        assertThat(resolution.distances()).isEmpty();
        assertThat(resolution.unresolved()).containsExactly(PARTITION_0);
    }

    @Test
    @DisplayName("An empty topic with no committed offsets resolves nothing")
    void emptyTopic_resolvesNothing() {
        var resolution = ShowcaseProjectorBacklogSampler.resolve(Map.of(), Map.of());

        assertThat(resolution.distances()).isEmpty();
        assertThat(resolution.unresolved()).isEmpty();
    }

    @Test
    @DisplayName("The sampler runs from start to stop and stops sampling")
    @SuppressWarnings("BusyWait")
    void sampler_runsBetweenStartAndStop() throws InterruptedException {
        var properties = new KafkaProperties();
        properties.setBootstrapServers(java.util.List.of("localhost:1"));
        var projectorProperties = new ShowcaseProjectorProperties();
        projectorProperties.getBacklog().setInterval(Duration.ofSeconds(1));
        var sampler = new ShowcaseProjectorBacklogSampler(properties, projectorProperties, registry);

        sampler.start();
        var running = sampler.isRunning();
        var sampleDeadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
        while (failures() == 0d && System.nanoTime() < sampleDeadline) {
            Thread.sleep(50);
        }
        var failuresBeforeStop = failures();
        sampler.stop();
        var stoppedAt = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        var attemptedAfterStop = false;
        while (System.nanoTime() < stoppedAt) {
            if (failures() != failuresBeforeStop) {
                attemptedAfterStop = true;
                break;
            }
            Thread.sleep(50);
        }

        assertThat(failuresBeforeStop)
                .as("the unreachable broker fails every attempt")
                .isPositive();
        assertThat(attemptedAfterStop)
                .as("a stopped sampler attempts no further sample")
                .isFalse();
        assertThat(running).isTrue();
        assertThat(sampler.isRunning()).isFalse();
    }

    @Test
    @DisplayName("Stopping the sampler closes its admin client")
    void stop_closesTheAdminClient() {
        var properties = new KafkaProperties();
        properties.setBootstrapServers(java.util.List.of("localhost:1"));
        var client = org.mockito.Mockito.mock(org.apache.kafka.clients.admin.AdminClient.class);
        var sampler = new ShowcaseProjectorBacklogSampler(
                properties, new ShowcaseProjectorProperties(), registry, config -> client);

        sampler.start();
        sampler.stop();

        org.mockito.Mockito.verify(client).close(org.mockito.ArgumentMatchers.any(Duration.class));
    }

    /**
     * Reads the failed-sample counter from the registry.
     *
     * @return the counter's value, or zero when it is absent
     */
    private double failures() {
        var counter = registry.find(ShowcaseProjectorBacklogSampler.FAILED).counter();
        return counter == null ? 0d : counter.count();
    }

    @Test
    @DisplayName("The admin client is configured from the connection keys only")
    void adminConfig_keepsTheConnectionKeysAndDropsTheConsumerOnlyOnes() {
        var properties = new KafkaProperties();
        properties.setBootstrapServers(java.util.List.of("localhost:9092"));
        properties.setClientId("showcase-projector");
        properties
                .getConsumer()
                .getProperties()
                .put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "g");
        properties
                .getConsumer()
                .getProperties()
                .put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
        var config = new ShowcaseProjectorBacklogSampler(properties, new ShowcaseProjectorProperties(), registry)
                .adminConfig();

        assertThat(config)
                .containsEntry("bootstrap.servers", java.util.List.of("localhost:9092"))
                .containsEntry("client.id", "showcase-projector-backlog")
                .doesNotContainKeys("group.id", "partition.assignment.strategy");
    }

    @Test
    @DisplayName("A sample publishes the distance, the unresolved count, and the observation time")
    void sample_publishesTheThreeSignals() {
        sampler.publish(
                ShowcaseProjectorBacklogSampler.resolve(
                        Map.of(PARTITION_0, 12L), Map.of(PARTITION_0, new OffsetAndMetadata(5L))),
                1_700_000_000L);

        var samples = samples();

        assertThat(samples).containsEntry(lagSeries(0), 7d);
        assertThat(samples).containsEntry("showcaseProjector_consumerLag_unresolved_partitions", 0d);
        assertThat(samples).containsEntry("showcaseProjector_consumerLag_observed_seconds", 1.7E9);
        assertThat(sampler.observedEpochSecond()).isEqualTo(1_700_000_000L);
        assertThat(sampler.maxResolvedDistance()).isEqualTo(7L);
        assertThat(sampler.unresolvedPartitions()).isZero();
    }

    @Test
    @DisplayName("An unresolved partition publishes no distance and is counted")
    void unresolvedPartition_publishesNoDistance() {
        sampler.publish(
                ShowcaseProjectorBacklogSampler.resolve(
                        Map.of(PARTITION_0, 12L, PARTITION_1, 20L), Map.of(PARTITION_0, new OffsetAndMetadata(5L))),
                1_700_000_000L);

        var samples = samples();

        assertThat(samples.get(lagSeries(0)))
                .as("the resolved partition keeps its distance")
                .isEqualTo(7d);
        assertThat(samples.get(lagSeries(1)))
                .as("the unresolved partition publishes no distance")
                .isNaN();
        assertThat(samples).containsEntry("showcaseProjector_consumerLag_unresolved_partitions", 1d);
        assertThat(sampler.unresolvedPartitions()).isEqualTo(1);
    }

    @Test
    @DisplayName("A sample that resolves nothing publishes no distance")
    void emptySample_publishesNoDistance() {
        sampler.publish(ShowcaseProjectorBacklogSampler.resolve(Map.of(), Map.of()), 1_700_000_000L);

        assertThat(samples()).doesNotContainKey("showcaseProjector_consumerLag_records");
        assertThat(sampler.maxResolvedDistance()).isNull();
        assertThat(sampler.observedEpochSecond()).isEqualTo(1_700_000_000L);
    }

    /**
     * Reads the scrape into a map keyed by the series name, keeping the label set so two partitions do not overwrite one
     * another.
     *
     * @return each series' value by its exposition line's name and labels
     */
    private Map<String, Double> samples() {
        var samples = new LinkedHashMap<String, Double>();
        for (var line : registry.scrape().lines().toList()) {
            if (line.startsWith("#") || line.isBlank() || line.contains("_created")) {
                continue;
            }
            var space = line.lastIndexOf(' ');
            if (space < 0) {
                continue;
            }
            samples.put(line.substring(0, space), Double.valueOf(line.substring(space + 1)));
        }
        return samples;
    }

    /**
     * Builds the exposition series name of one partition's distance.
     *
     * @param partition the partition's index
     * @return the series name a scrape prints for that partition
     */
    private static String lagSeries(int partition) {
        return "showcaseProjector_consumerLag_records{partition=\"" + partition + "\",topic=\"" + TOPIC + "\"}";
    }
}
