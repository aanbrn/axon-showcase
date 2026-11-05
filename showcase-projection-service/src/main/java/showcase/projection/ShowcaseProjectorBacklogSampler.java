// SPDX-License-Identifier: MIT
package showcase.projection;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Samples how far the consumer group's committed offsets are behind the topic's end offsets.
 *
 * <p>The sample runs on its own virtual thread against an {@link AdminClient} created from the configured consumer
 * properties, so it observes the group from outside the consumption pipeline: a consumer that stops polling does not stop
 * the observation, and the distance includes records that were fetched and never acknowledged. The health indicator and
 * the metrics read the last sample's snapshot, so neither waits on Kafka.
 *
 * <p>A partition the sample cannot resolve — such as one with no committed offset, a committed offset past the end, or
 * one the topic no longer reports — publishes no distance and is counted as unresolved, rather than publishing a zero
 * that would read as a healthy idle projector. An end-offset read that fails fails the whole sample instead, which the
 * failure counter reports.
 */
@Component
@Slf4j
class ShowcaseProjectorBacklogSampler implements SmartLifecycle {
    /**
     * The metric name carrying each partition's outstanding distance.
     */
    static final String CONSUMER_LAG = "showcaseProjector.consumerLag";

    /**
     * The metric name counting the partitions the last sample could not resolve.
     */
    static final String UNRESOLVED = CONSUMER_LAG + ".unresolved";

    /**
     * The metric name carrying the epoch second of the last successful sample.
     */
    static final String OBSERVED = CONSUMER_LAG + ".observed";

    /**
     * The metric name counting the samples that failed.
     */
    static final String FAILED = CONSUMER_LAG + ".failed";

    /**
     * The description of the distance gauge.
     */
    private static final String CONSUMER_LAG_DESCRIPTION =
            "Records the consumer group has committed against the topic's end offset, per partition";

    /**
     * The suffix appended to the consumer's client id for the sampler's own admin client.
     */
    private static final String ADMIN_CLIENT_ID_SUFFIX = "-backlog";

    /**
     * The configured Kafka connection settings.
     */
    private final KafkaProperties kafkaProperties;

    /**
     * The projector configuration holding the backlog sampling settings.
     */
    private final ShowcaseProjectorProperties projectionProperties;

    /**
     * The registry receiving the backlog metrics.
     */
    private final MeterRegistry meterRegistry;

    /**
     * The factory creating the admin client the samples read with.
     */
    private final AdminClientFactory adminClientFactory;

    /**
     * The counter incremented by every failed sample.
     */
    private final Counter failedSamples;

    /**
     * The partitions whose distance gauge has been registered.
     */
    private final Set<TopicPartition> registeredPartitions = ConcurrentHashMap.newKeySet();

    /**
     * The last sample's outcome, read by the metrics and the health indicator.
     */
    private volatile Sample sample = Sample.none();

    /**
     * The admin client, present while the sampler is running.
     */
    private final AtomicReference<@Nullable AdminClient> adminClient = new AtomicReference<>();

    /**
     * The scheduler driving the samples, present while the sampler is running.
     */
    private final AtomicReference<@Nullable ScheduledExecutorService> scheduler = new AtomicReference<>();

    /**
     * Creates the sampler and registers the gauges that report the last sample.
     *
     * @param kafkaProperties the Kafka connection settings the admin client is derived from
     * @param projectionProperties the projector configuration holding the sampling settings
     * @param meterRegistry the registry receiving the backlog metrics
     */
    @Autowired
    ShowcaseProjectorBacklogSampler(
            KafkaProperties kafkaProperties,
            ShowcaseProjectorProperties projectionProperties,
            MeterRegistry meterRegistry) {
        this(kafkaProperties, projectionProperties, meterRegistry, AdminClient::create);
    }

    /**
     * Creates the sampler with a given admin client factory, so a test can observe the client's lifecycle.
     *
     * @param kafkaProperties the Kafka connection settings the admin client is derived from
     * @param projectionProperties the projector configuration holding the sampling settings
     * @param meterRegistry the registry receiving the backlog metrics
     * @param adminClientFactory the factory creating the admin client
     */
    ShowcaseProjectorBacklogSampler(
            KafkaProperties kafkaProperties,
            ShowcaseProjectorProperties projectionProperties,
            MeterRegistry meterRegistry,
            AdminClientFactory adminClientFactory) {
        this.kafkaProperties = kafkaProperties;
        this.projectionProperties = projectionProperties;
        this.meterRegistry = meterRegistry;
        this.adminClientFactory = adminClientFactory;
        this.failedSamples = Counter.builder(FAILED)
                .description("Backlog samples that failed")
                .register(meterRegistry);
        Gauge.builder(UNRESOLVED, () -> sample.unresolved().size())
                .description("Partitions the last backlog sample could not resolve")
                .baseUnit("partitions")
                .register(meterRegistry);
        Gauge.builder(OBSERVED, () -> sample.observedEpochSecond())
                .description("Epoch second of the last successful backlog sample, 0 before one")
                .baseUnit("seconds")
                .register(meterRegistry);
    }

    /**
     * Starts the sampler: creates the admin client and schedules the first sample immediately.
     */
    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void start() {
        if (scheduler.get() != null) {
            return;
        }
        adminClient.set(newAdminClient());
        var executor = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("showcase-backlog", 0).factory());
        var interval = projectionProperties.getBacklog().getInterval().toMillis();
        executor.scheduleWithFixedDelay(this::sample, 0, interval, TimeUnit.MILLISECONDS);
        scheduler.set(executor);
    }

    /**
     * Stops the sampler: cancels the schedule and closes the admin client.
     */
    @Override
    public void stop() {
        var executor = scheduler.getAndSet(null);
        if (executor != null) {
            executor.shutdownNow();
        }
        var client = adminClient.getAndSet(null);
        if (client != null) {
            client.close(Duration.ofSeconds(1));
        }
    }

    /**
     * Returns whether the sampler is running.
     *
     * @return {@code true} while the schedule is active
     */
    @Override
    public boolean isRunning() {
        return scheduler.get() != null;
    }

    /**
     * Reads one sample and publishes it, counting a failure instead of publishing a value it does not have.
     */
    void sample() {
        var client = adminClient.get();
        if (client == null) {
            return;
        }
        try {
            publish(read(client), Instant.now().getEpochSecond());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (Exception failure) {
            failedSamples.increment();
            log.debug("The backlog sample failed: {}", failure.toString());
        }
    }

    /**
     * Reads the topic's end offsets and the group's committed offsets.
     *
     * @param client the admin client to read with
     * @return the resolved distances and the partitions the sample could not resolve
     * @throws Exception when an admin call fails or exceeds its timeout
     */
    private Resolution read(AdminClient client) throws Exception {
        var timeout = projectionProperties.getBacklog().getTimeout();
        var topic = kafkaProperties.getDefaultTopic();
        var descriptions =
                client.describeTopics(List.of(topic)).allTopicNames().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        var described = descriptions.get(topic);
        if (described == null) {
            throw new IllegalStateException("The topic " + topic + " was not described");
        }
        var partitions = described.partitions().stream()
                .map(partition -> new TopicPartition(topic, partition.partition()))
                .toList();
        var specs = new HashMap<TopicPartition, OffsetSpec>();
        partitions.forEach(partition -> specs.put(partition, OffsetSpec.latest()));
        var endOffsets = new HashMap<TopicPartition, Long>();
        client.listOffsets(specs)
                .all()
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .forEach((partition, result) -> endOffsets.put(partition, result.offset()));
        var committed = new HashMap<TopicPartition, OffsetAndMetadata>();
        client.listConsumerGroupOffsets(groupId())
                .partitionsToOffsetAndMetadata()
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .forEach((partition, metadata) -> {
                    if (partition.topic().equals(topic)) {
                        committed.put(partition, metadata);
                    }
                });
        return resolve(endOffsets, committed);
    }

    /**
     * Resolves each visited partition to its outstanding distance or to an unresolved outcome.
     *
     * <p>The visited set is the union of the topic's partitions and the group's committed partitions, so a partition the
     * group committed but the topic no longer reports is still visited and reported unresolved.
     *
     * @param endOffsets the topic's end offsets by partition
     * @param committed the group's committed offsets by partition
     * @return the resolved distances and the unresolved partitions
     */
    static Resolution resolve(Map<TopicPartition, Long> endOffsets, Map<TopicPartition, OffsetAndMetadata> committed) {
        var visited = new HashSet<>(endOffsets.keySet());
        visited.addAll(committed.keySet());
        var distances = new HashMap<TopicPartition, Long>();
        var unresolved = new HashSet<TopicPartition>();
        for (var partition : visited) {
            var end = endOffsets.get(partition);
            var offset = committed.get(partition);
            if (end == null || offset == null || offset.offset() > end) {
                unresolved.add(partition);
            } else {
                distances.put(partition, end - offset.offset());
            }
        }
        return new Resolution(distances, unresolved);
    }

    /**
     * Publishes a sample: registers a gauge for a partition seen for the first time and stores the snapshot.
     *
     * @param resolution the sample's resolved distances and unresolved partitions
     * @param observedEpochSecond the epoch second the sample succeeded
     */
    void publish(Resolution resolution, long observedEpochSecond) {
        var visited = new HashSet<>(resolution.distances().keySet());
        visited.addAll(resolution.unresolved());
        visited.forEach(this::registerGauge);
        sample = new Sample(resolution.distances(), resolution.unresolved(), observedEpochSecond);
    }

    /**
     * Registers the distance gauge of a partition the first time the sampler visits it.
     *
     * @param partition the partition to register
     */
    private void registerGauge(TopicPartition partition) {
        if (!registeredPartitions.add(partition)) {
            return;
        }
        Gauge.builder(CONSUMER_LAG, () -> valueOf(partition))
                .description(CONSUMER_LAG_DESCRIPTION)
                .baseUnit("records")
                .tag("topic", partition.topic())
                .tag("partition", Integer.toString(partition.partition()))
                .register(meterRegistry);
    }

    /**
     * Returns the last sample's distance for a partition.
     *
     * @param partition the partition to read
     * @return the distance, or {@code NaN} when the last sample did not resolve the partition
     */
    private double valueOf(TopicPartition partition) {
        var distance = sample.distances().get(partition);
        return distance == null ? Double.NaN : distance.doubleValue();
    }

    /**
     * Returns the largest distance the last sample resolved.
     *
     * @return the maximum resolved distance, or {@code null} when no partition resolved
     */
    @Nullable
    Long maxResolvedDistance() {
        return sample.maxResolvedDistance();
    }

    /**
     * Returns how many partitions the last sample could not resolve.
     *
     * @return the unresolved partition count
     */
    int unresolvedPartitions() {
        return sample.unresolved().size();
    }

    /**
     * Returns the epoch second of the last successful sample.
     *
     * @return the epoch second, or {@code 0} before the first successful sample
     */
    long observedEpochSecond() {
        return sample.observedEpochSecond();
    }

    /**
     * Returns the consumer group whose offsets are sampled.
     *
     * @return the configured group id
     */
    private String groupId() {
        return String.valueOf(kafkaProperties.buildConsumerProperties().get(ConsumerConfig.GROUP_ID_CONFIG));
    }

    /**
     * Creates the admin client from the consumer properties, keeping only the keys an admin client accepts.
     *
     * <p>The connection material — bootstrap servers, security protocol, and any SASL settings — arrives through the
     * consumer properties, while consumer-only keys such as the group id or the assignment strategy would draw a warning
     * from the client.
     *
     * @return the admin client the sampler reads with
     */
    private AdminClient newAdminClient() {
        return adminClientFactory.create(adminConfig());
    }

    /**
     * Builds the admin client's configuration from the consumer properties, keeping only the keys it accepts.
     *
     * @return the configuration the sampler's admin client is created with
     */
    Map<String, Object> adminConfig() {
        var config = new HashMap<>(kafkaProperties.buildConsumerProperties());
        config.keySet().retainAll(AdminClientConfig.configNames());
        var clientId =
                String.valueOf(config.getOrDefault(AdminClientConfig.CLIENT_ID_CONFIG, "showcase-projector-backlog"));
        config.put(AdminClientConfig.CLIENT_ID_CONFIG, clientId + ADMIN_CLIENT_ID_SUFFIX);
        return config;
    }

    /**
     * Creates the admin client a sample reads with.
     */
    @FunctionalInterface
    interface AdminClientFactory {
        /**
         * Creates an admin client for the given configuration.
         *
         * @param config the admin client's configuration
         * @return the admin client
         */
        AdminClient create(Map<String, Object> config);
    }

    /**
     * One sample's outcome.
     *
     * @param distances the resolved distance of each partition
     * @param unresolved the partitions the sample could not resolve
     * @param observedEpochSecond the epoch second the sample succeeded
     */
    record Sample(Map<TopicPartition, Long> distances, Set<TopicPartition> unresolved, long observedEpochSecond) {
        /**
         * The outcome before the first successful sample.
         *
         * @return the empty sample
         */
        static Sample none() {
            return new Sample(Map.of(), Set.of(), 0L);
        }

        /**
         * Returns the largest resolved distance.
         *
         * @return the maximum distance, or {@code null} when no partition resolved
         */
        @Nullable
        Long maxResolvedDistance() {
            return distances.values().stream().max(Long::compare).orElse(null);
        }
    }

    /**
     * A sample's partitions: those it resolved and those it could not.
     *
     * @param distances the resolved distance of each partition
     * @param unresolved the partitions the sample could not resolve
     */
    record Resolution(Map<TopicPartition, Long> distances, Set<TopicPartition> unresolved) {}
}
