package showcase.command;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.jspecify.annotations.Nullable;

/**
 * Saga that manages the automatic lifecycle of a showcase through deadlines.
 *
 * <p>When a showcase is scheduled, a deadline is set to start it at the scheduled time. When started, a second
 * deadline is set to finish it after the configured duration. The saga ends when the showcase is either finished or
 * removed.
 */
@Saga
@ProcessingGroup("showcase-saga")
@Slf4j
public class ShowcaseSaga {
    /**
     * The current lifecycle status of the showcase, if known.
     */
    @Nullable
    private ShowcaseStatus showcaseStatus;

    /**
     * Handles the scheduling of a showcase by setting a deadline to start it at the configured start time.
     *
     * @param event the scheduled event
     * @param deadlineManager the deadline manager used to schedule the start
     */
    @StartSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(ShowcaseScheduledEvent event, DeadlineManager deadlineManager) {
        showcaseStatus = ShowcaseStatus.SCHEDULED;

        deadlineManager.schedule(event.startTime(), "startShowcase", event.showcaseId());

        log.trace("Scheduled deadline to start showcase with ID {} at {}", event.showcaseId(), event.startTime());
    }

    /**
     * Fires when the start deadline triggers, sending a {@link StartShowcaseCommand}.
     *
     * <p>If the showcase is no longer in {@link ShowcaseStatus#SCHEDULED} state (e.g., it was removed or already
     * finished), the deadline is skipped.
     *
     * @param showcaseId the ID of the showcase to start
     * @param commandGateway the command gateway used to send the start command
     */
    @DeadlineHandler(deadlineName = "startShowcase")
    void startShowcase(String showcaseId, CommandGateway commandGateway) {
        if (ObjectUtils.notEqual(showcaseStatus, ShowcaseStatus.SCHEDULED)) {
            log.trace("On starting deadline, showcase has status {}, so skipping", showcaseStatus);
            return;
        }

        log.trace("Starting showcase with ID {}...", showcaseId);

        try {
            commandGateway.sendAndWait(
                    StartShowcaseCommand
                            .builder()
                            .showcaseId(showcaseId)
                            .build());

            log.trace("Started showcase with ID {}", showcaseId);
        } catch (CommandExecutionException e) {
            val errorDetails = e.getDetails();
            if (errorDetails.isPresent()) {
                log.error("Failed to start showcase with ID {}, details: {}", showcaseId, errorDetails.get());
            } else {
                log.error("Failed to start showcase with ID {}", showcaseId, e);
            }
        }
    }

    /**
     * Handles the {@link ShowcaseStartedEvent} by scheduling a deadline to finish the showcase after its configured
     * duration.
     *
     * @param event the started event
     * @param deadlineManager the deadline manager used to schedule the finish
     */
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(ShowcaseStartedEvent event, DeadlineManager deadlineManager) {
        showcaseStatus = ShowcaseStatus.STARTED;

        val finishTime = event.startedAt().plus(event.duration());

        deadlineManager.schedule(finishTime, "finishShowcase", event.showcaseId());

        log.trace("Scheduled deadline to finish showcase with ID {} at {}", event.showcaseId(), finishTime);
    }

    /**
     * Fires when the finish deadline triggers, sending a {@link FinishShowcaseCommand}.
     *
     * <p>If the showcase is no longer in {@link ShowcaseStatus#STARTED} state (e.g., it was removed or not yet
     * started), the deadline is skipped.
     *
     * @param showcaseId the ID of the showcase to finish
     * @param commandGateway the command gateway used to send the finish command
     */
    @DeadlineHandler(deadlineName = "finishShowcase")
    void finishShowcase(String showcaseId, CommandGateway commandGateway) {
        if (ObjectUtils.notEqual(showcaseStatus, ShowcaseStatus.STARTED)) {
            log.trace("On finishing deadline, showcase has status {}, so skipping", showcaseStatus);
            return;
        }

        log.trace("Finishing showcase with ID {}...", showcaseId);

        try {
            commandGateway.sendAndWait(
                    FinishShowcaseCommand
                            .builder()
                            .showcaseId(showcaseId)
                            .build());

            log.trace("Finished showcase with ID {}", showcaseId);
        } catch (CommandExecutionException e) {
            val errorDetails = e.getDetails();
            if (errorDetails.isPresent()) {
                log.error("Failed to finish showcase with ID {}, details: {}", showcaseId, errorDetails.get());
            } else {
                log.error("Failed to finish showcase with ID {}", showcaseId, e);
            }
        }
    }

    /**
     * Marks the showcase as finished and ends the saga.
     *
     * @param event the finished event
     */
    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(ShowcaseFinishedEvent event) {
        showcaseStatus = ShowcaseStatus.FINISHED;
    }

    /**
     * Clears the showcase status and ends the saga when a showcase is removed.
     *
     * @param event the removed event
     */
    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(ShowcaseRemovedEvent event) {
        showcaseStatus = null;
    }
}
