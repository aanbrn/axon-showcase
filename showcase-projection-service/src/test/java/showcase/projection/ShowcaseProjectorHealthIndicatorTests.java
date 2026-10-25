// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

@DisplayName("Projector health indicator tests")
class ShowcaseProjectorHealthIndicatorTests {

    private final ShowcaseProjectorStateMonitor monitor = new ShowcaseProjectorStateMonitor(new SimpleMeterRegistry());

    private final ShowcaseProjectorHealthIndicator indicator = new ShowcaseProjectorHealthIndicator(monitor);

    @Test
    @DisplayName("A stopped projector is down")
    void stopped_isDown() {
        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("state", "stopped");
    }

    @Test
    @DisplayName("A running projector is up with its progress")
    void running_isUp() {
        monitor.batchStarted();
        monitor.batchResolved();
        monitor.running();

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("state", "running");
        assertThat((Long) health.getDetails().get("lastProgressEpochSecond")).isPositive();
    }

    @Test
    @DisplayName("A retrying projector is down with the failure")
    void retrying_isDownWithTheFailure() {
        monitor.retrying(new IllegalStateException("kafka unreachable"));

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("state", "retrying");
        assertThat((String) health.getDetails().get("lastFailure"))
                .contains("IllegalStateException")
                .contains("kafka unreachable");
    }

    @Test
    @DisplayName("A projector that is running but has no events yet is up")
    void runningWithoutEvents_isUp() {
        monitor.running();

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("state", "running");
        assertThat(health.getDetails()).containsEntry("lastProgressEpochSecond", 0L);
    }
}
