// SPDX-License-Identifier: MIT
package showcase.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kagkarlsson.scheduler.stats.StatsRegistry;
import com.github.kagkarlsson.scheduler.task.Execution;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Showcase DB scheduler metrics component tests")
class ShowcaseDbSchedulerMetricsCT {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final ShowcaseDbSchedulerMetrics metrics =
            ShowcaseDbSchedulerMetrics.builder().meterRegistry(meterRegistry).build();

    @Test
    @DisplayName("Registering a scheduler event increments its counter")
    void register_schedulerEvent_incrementsCounter() {
        metrics.register(StatsRegistry.SchedulerStatsEvent.UNEXPECTED_ERROR);

        assertThat(meterRegistry
                        .counter("dbscheduler.schedulerEvents", "event", "UNEXPECTED_ERROR")
                        .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Registering a candidate event increments its counter")
    void register_candidateEvent_incrementsCounter() {
        metrics.register(StatsRegistry.CandidateStatsEvent.EXECUTED);

        assertThat(meterRegistry
                        .counter("dbscheduler.candidateEvents", "event", "EXECUTED")
                        .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Registering an execution event increments its counter")
    void register_executionEvent_incrementsCounter() {
        metrics.register(StatsRegistry.ExecutionStatsEvent.COMPLETED);

        assertThat(meterRegistry
                        .counter("dbscheduler.executionEvents", "event", "COMPLETED")
                        .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Registering a successful completion records the execution counter and timers")
    void registerSingleCompletedExecution_success_recordsExecutionCounterAndTimers() {
        val taskName = "my-task";
        val execution = new Execution(Instant.now(), new TaskInstance<>(taskName, "instance-1"));
        val complete = ExecutionComplete.success(execution, Instant.now().minusSeconds(1), Instant.now());

        metrics.registerSingleCompletedExecution(complete);

        assertThat(meterRegistry
                        .counter(
                                "dbscheduler.executions",
                                "task",
                                taskName,
                                "deadline",
                                "",
                                "event",
                                "",
                                "result",
                                "ok",
                                "error",
                                "")
                        .count())
                .isEqualTo(1);
        assertThat(meterRegistry.find("dbscheduler.executionDuration").timer()).isNotNull();
        assertThat(meterRegistry.find("dbscheduler.executionLag").timer()).isNotNull();
    }

    @Test
    @DisplayName("Registering a failed completion records the failure result and error tags")
    void registerSingleCompletedExecution_failure_recordsFailureResultAndErrorTags() {
        val execution = new Execution(Instant.now(), new TaskInstance<>("my-task", "instance-1"));
        val complete = ExecutionComplete.failure(
                execution, Instant.now().minusSeconds(1), Instant.now(), new IllegalStateException("boom"));

        metrics.registerSingleCompletedExecution(complete);

        assertThat(meterRegistry
                        .counter(
                                "dbscheduler.executions",
                                "task",
                                "my-task",
                                "deadline",
                                "",
                                "event",
                                "",
                                "result",
                                "failed",
                                "error",
                                "IllegalStateException")
                        .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Registering a completion without an execution time skips the execution-lag timer")
    void registerSingleCompletedExecution_nullExecutionTime_skipsExecutionLagTimer() {
        val execution = new Execution(null, new TaskInstance<>("my-task", "instance-1"));
        val complete = ExecutionComplete.success(execution, Instant.now().minusSeconds(1), Instant.now());

        metrics.registerSingleCompletedExecution(complete);

        assertThat(meterRegistry.find("dbscheduler.executionLag").timer()).isNull();
        assertThat(meterRegistry
                        .counter(
                                "dbscheduler.executions",
                                "task",
                                "my-task",
                                "deadline",
                                "",
                                "event",
                                "",
                                "result",
                                "ok",
                                "error",
                                "")
                        .count())
                .isEqualTo(1);
    }
}
