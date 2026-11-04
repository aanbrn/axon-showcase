// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the Prometheus exposition names the Grafana dashboard's projector panels query.
 *
 * <p>The dashboard reads `showcaseProjector_state`, `showcaseProjector_processing`, and
 * `showcaseProjector_last_progress_seconds` — the last carrying the registry's unit suffix, which the Micrometer name
 * (`showcaseProjector.last.progress`) does not show. Renaming a gauge in {@link ShowcaseProjectorStateMonitor} would
 * otherwise leave a panel that renders "No data", which reads as a quiet system.
 */
@DisplayName("Projector state monitor Prometheus exposition tests")
class ShowcaseProjectorStateMonitorPrometheusTests {

    @Test
    @DisplayName("The three gauges are exposed under the names the dashboard panels query")
    void gauges_areExposedUnderThePanelNames() {
        var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        var monitor = new ShowcaseProjectorStateMonitor(registry);

        monitor.running();
        monitor.batchStarted();
        monitor.batchResolved();

        var samples = samples(registry.scrape());

        assertThat(samples.keySet())
                .containsExactlyInAnyOrder(
                        "showcaseProjector_state",
                        "showcaseProjector_processing",
                        "showcaseProjector_last_progress_seconds");
        assertThat(samples.get("showcaseProjector_state")).isEqualTo(1d);
        assertThat(samples.get("showcaseProjector_processing")).isZero();
        assertThat(samples.get("showcaseProjector_last_progress_seconds")).isPositive();
    }

    private static Map<String, Double> samples(String scrape) {
        var samples = new LinkedHashMap<String, Double>();
        for (var line : scrape.lines().toList()) {
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }
            var parts = line.split(" ");
            samples.put(parts[0], Double.parseDouble(parts[1]));
        }
        return samples;
    }
}
