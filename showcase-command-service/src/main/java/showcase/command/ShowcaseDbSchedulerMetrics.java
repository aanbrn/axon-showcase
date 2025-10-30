package showcase.command;

import com.github.kagkarlsson.scheduler.stats.StatsRegistry;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete.Result;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.axonframework.deadline.dbscheduler.DbSchedulerBinaryDeadlineDetails;
import org.axonframework.deadline.dbscheduler.DbSchedulerHumanReadableDeadlineDetails;
import org.axonframework.eventhandling.scheduling.dbscheduler.DbSchedulerBinaryEventData;
import org.axonframework.eventhandling.scheduling.dbscheduler.DbSchedulerHumanReadableEventData;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Slf4j
final class ShowcaseDbSchedulerMetrics implements StatsRegistry {

    private final MeterRegistry meterRegistry;

    private final Field executionCompleteTimeStartedField =
            requireNonNull(FieldUtils.getField(ExecutionComplete.class, "timeStarted", true),
                           "\"ExecutionComplete::timeStarted\" field must be accessible");

    @Override
    public void register(SchedulerStatsEvent event) {
        Counter.builder("dbscheduler.schedulerEvents")
               .tag("event", event.name())
               .register(meterRegistry)
               .increment();
    }

    @Override
    public void register(CandidateStatsEvent event) {
        Counter.builder("dbscheduler.candidateEvents")
               .tag("event", event.name())
               .register(meterRegistry)
               .increment();
    }

    @Override
    public void register(ExecutionStatsEvent event) {
        Counter.builder("dbscheduler.executionEvents")
               .tag("event", event.name())
               .register(meterRegistry)
               .increment();
    }

    @Override
    public void registerSingleCompletedExecution(ExecutionComplete event) {
        val taskInstance = Optional.ofNullable(event.getExecution())
                                   .map(execution -> execution.taskInstance);
        val taskName = taskInstance.map(TaskInstance::getTaskName)
                                   .orElse("");
        val taskData = taskInstance.map(TaskInstance::getData);
        val deadlineName = taskData.filter(__ -> taskName.equals("AxonDeadline"))
                                   .filter(DbSchedulerBinaryDeadlineDetails.class::isInstance)
                                   .map(DbSchedulerBinaryDeadlineDetails.class::cast)
                                   .map(DbSchedulerBinaryDeadlineDetails::getD)
                                   .or(() -> taskData.filter(DbSchedulerHumanReadableDeadlineDetails.class::isInstance)
                                                     .map(DbSchedulerHumanReadableDeadlineDetails.class::cast)
                                                     .map(DbSchedulerHumanReadableDeadlineDetails::getDeadlineName))
                                   .orElse("");
        val eventType = taskData.filter(__ -> taskName.equals("AxonScheduledEvent"))
                                .filter(DbSchedulerBinaryEventData.class::isInstance)
                                .map(DbSchedulerBinaryEventData.class::cast)
                                .map(DbSchedulerBinaryEventData::getC)
                                .or(() -> taskData.filter(DbSchedulerHumanReadableEventData.class::isInstance)
                                                  .map(DbSchedulerHumanReadableEventData.class::cast)
                                                  .map(DbSchedulerHumanReadableEventData::getPayloadClass))
                                .orElse("");
        val result = Optional.ofNullable(event.getResult())
                             .map(Result::name)
                             .map(String::toLowerCase)
                             .orElse("");
        val error = event.getCause()
                         .map(Throwable::getClass)
                         .map(Class::getSimpleName)
                         .orElse("");

        Counter.builder("dbscheduler.executions")
               .tag("task", taskName)
               .tag("deadline", deadlineName)
               .tag("event", eventType)
               .tag("result", result)
               .tag("error", error)
               .register(meterRegistry)
               .increment();

        Timer.builder("dbscheduler.executionDuration")
             .distributionStatisticExpiry(Duration.of(10, ChronoUnit.MINUTES))
             .publishPercentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999)
             .tag("task", taskName)
             .tag("deadline", deadlineName)
             .tag("event", eventType)
             .tag("result", result)
             .tag("error", error)
             .register(meterRegistry)
             .record(event.getDuration().toMillis(), TimeUnit.MILLISECONDS);

        try {
            val timeStarted = (Instant) executionCompleteTimeStartedField.get(event);
            val executionLag = Duration.between(event.getExecution().executionTime, timeStarted);

            Timer.builder("dbscheduler.executionLag")
                 .distributionStatisticExpiry(Duration.of(10, ChronoUnit.MINUTES))
                 .publishPercentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999)
                 .tag("task", taskName)
                 .tag("deadline", deadlineName)
                 .tag("event", eventType)
                 .register(meterRegistry)
                 .record(executionLag.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Failed to get value of \"ExecutionComplete::timeStarted\" field", e);
        }
    }
}
