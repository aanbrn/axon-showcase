// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

@DisplayName("Projector health indicator tests")
class ShowcaseProjectorHealthIndicatorTests {

    private final ShowcaseProjectorStateMonitor monitor = new ShowcaseProjectorStateMonitor(new SimpleMeterRegistry());

    private final ShowcaseProjectorBacklogSampler sampler = new ShowcaseProjectorBacklogSampler(
            new KafkaProperties(), new ShowcaseProjectorProperties(), new SimpleMeterRegistry());

    private final ShowcaseProjectorHealthIndicator indicator = new ShowcaseProjectorHealthIndicator(monitor, sampler);

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

    @Test
    @DisplayName("A projector with no backlog sample yet carries no distance")
    void withoutABacklogSample_carriesNoDistance() {
        monitor.running();

        var health = indicator.health();

        assertThat(health.getDetails()).doesNotContainKey("consumerLag");
        assertThat(health.getDetails()).containsEntry("consumerLagUnresolvedPartitions", 0);
        assertThat(health.getDetails()).containsEntry("consumerLagObservedEpochSecond", 0L);
    }

    @Test
    @DisplayName("A partial backlog sample reports the distance beside the unresolved count")
    void partialSample_reportsDistanceBesideUnresolvedCount() {
        monitor.running();
        var resolved = new TopicPartition("axon-showcase-events", 0);
        var unresolved = new TopicPartition("axon-showcase-events", 1);
        sampler.publish(
                ShowcaseProjectorBacklogSampler.resolve(
                        java.util.Map.of(resolved, 10L, unresolved, 20L),
                        java.util.Map.of(resolved, new OffsetAndMetadata(4L))),
                1_700_000_000L);

        var health = indicator.health();

        assertThat(health.getDetails()).containsEntry("consumerLag", 6L);
        assertThat(health.getDetails()).containsEntry("consumerLagUnresolvedPartitions", 1);
        assertThat(health.getDetails()).containsEntry("consumerLagObservedEpochSecond", 1_700_000_000L);
    }

    @Test
    @DisplayName("A backlog sample does not change the status the lifecycle state decides")
    void backlog_doesNotChangeTheStatus() {
        var resolved = new TopicPartition("axon-showcase-events", 0);
        sampler.publish(
                ShowcaseProjectorBacklogSampler.resolve(
                        java.util.Map.of(resolved, 10L), java.util.Map.of(resolved, new OffsetAndMetadata(0L))),
                1_700_000_000L);

        var stoppedHealth = indicator.health();
        monitor.running();
        var runningHealth = indicator.health();

        assertThat(stoppedHealth.getStatus()).isEqualTo(Status.DOWN);
        assertThat(runningHealth.getStatus()).isEqualTo(Status.UP);
        assertThat(stoppedHealth.getDetails()).containsEntry("consumerLag", 10L);
        assertThat(runningHealth.getDetails()).containsEntry("consumerLag", 10L);
    }
}
