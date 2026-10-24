// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Projector state monitor tests")
class ShowcaseProjectorStateMonitorTests {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final ShowcaseProjectorStateMonitor monitor = new ShowcaseProjectorStateMonitor(meterRegistry);

    @Test
    @DisplayName("A new monitor reports stopped and a zero progress")
    void newMonitor_reportsStoppedAndNoProgress() {
        assertThat(monitor.getState()).isEqualTo(ShowcaseProjectorStateMonitor.State.STOPPED);
        assertThat(monitor.lastProgressEpochSecond()).isZero();
        assertThat(gauge("showcaseProjector.state")).isZero();
        assertThat(gauge("showcaseProjector.processing")).isZero();
        assertThat(gauge("showcaseProjector.last.progress")).isZero();
    }

    @Test
    @DisplayName("An assignment reports running")
    void running_reportsRunning() {
        monitor.running();

        assertThat(monitor.getState()).isEqualTo(ShowcaseProjectorStateMonitor.State.RUNNING);
        assertThat(gauge("showcaseProjector.state")).isEqualTo(1d);
    }

    @Test
    @DisplayName("A stream failure reports retrying and keeps the failure")
    void retrying_reportsRetryingAndTheFailure() {
        monitor.retrying(new IllegalStateException("kafka unreachable"));

        assertThat(monitor.getState()).isEqualTo(ShowcaseProjectorStateMonitor.State.RETRYING);
        assertThat(monitor.lastFailure()).contains("IllegalStateException").contains("kafka unreachable");
        assertThat(gauge("showcaseProjector.state")).isEqualTo(2d);
    }

    @Test
    @DisplayName("A resolved batch clears the in-flight count and stamps the progress")
    void batchResolved_clearsInFlightAndStampsProgress() {
        var before = java.time.Instant.now().getEpochSecond();
        monitor.batchStarted();
        assertThat(gauge("showcaseProjector.processing")).isEqualTo(1d);

        monitor.batchResolved();

        assertThat(gauge("showcaseProjector.processing")).isZero();
        assertThat(monitor.lastProgressEpochSecond())
                .isBetween(before, java.time.Instant.now().getEpochSecond());
        assertThat(gauge("showcaseProjector.last.progress")).isEqualTo((double) monitor.lastProgressEpochSecond());
    }

    @Test
    @DisplayName("A failed batch clears the in-flight count without stamping the progress")
    void batchFailed_clearsInFlightWithoutProgress() {
        monitor.batchStarted();

        monitor.batchFailed();

        assertThat(gauge("showcaseProjector.processing")).isZero();
        assertThat(monitor.lastProgressEpochSecond()).isZero();
    }

    @Test
    @DisplayName("Stopping reports stopped")
    void stopped_reportsStopped() {
        monitor.running();
        monitor.stopped();

        assertThat(monitor.getState()).isEqualTo(ShowcaseProjectorStateMonitor.State.STOPPED);
        assertThat(gauge("showcaseProjector.state")).isZero();
    }

    private double gauge(String name) {
        return meterRegistry.get(name).gauge().value();
    }
}
