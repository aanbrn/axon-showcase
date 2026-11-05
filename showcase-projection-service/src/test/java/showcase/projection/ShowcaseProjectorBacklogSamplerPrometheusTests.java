// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the Prometheus exposition names the Grafana dashboard's backlog panels query.
 *
 * <p>The dashboard reads `showcaseProjector_consumerLag_records`, its `_unresolved_partitions` and
 * `_observed_seconds` companions, and the `_failed_total` counter — names the registry's unit suffix and counter
 * convention produce, which the Micrometer names do not show. Renaming a meter or a base unit in
 * {@link ShowcaseProjectorBacklogSampler} would otherwise leave a panel that renders "No data", which reads as a quiet
 * system.
 */
@DisplayName("Projector backlog sampler Prometheus exposition tests")
class ShowcaseProjectorBacklogSamplerPrometheusTests {

    private static final TopicPartition PARTITION = new TopicPartition("axon-showcase-events", 0);

    @Test
    @DisplayName("The sampler is exposed under the names the dashboard panels query")
    void gauges_areExposedUnderThePanelNames() {
        var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        var sampler =
                new ShowcaseProjectorBacklogSampler(new KafkaProperties(), new ShowcaseProjectorProperties(), registry);

        sampler.publish(
                ShowcaseProjectorBacklogSampler.resolve(
                        Map.of(PARTITION, 10L), Map.of(PARTITION, new OffsetAndMetadata(4L))),
                1_700_000_000L);

        var series = series(registry.scrape());

        assertThat(series)
                .contains(
                        "showcaseProjector_consumerLag_records{partition=\"0\",topic=\"axon-showcase-events\"}",
                        "showcaseProjector_consumerLag_unresolved_partitions",
                        "showcaseProjector_consumerLag_observed_seconds")
                .anyMatch(name -> name.startsWith("showcaseProjector_consumerLag_failed_total"));
    }

    private static List<String> series(String scrape) {
        return scrape.lines()
                .filter(line -> !line.startsWith("#") && !line.isBlank())
                .map(line -> line.substring(0, line.lastIndexOf(' ')))
                .toList();
    }
}
