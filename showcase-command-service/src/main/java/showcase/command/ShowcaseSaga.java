package showcase.command;

import lombok.NonNull;
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

@Saga
@ProcessingGroup("showcase-saga")
@Slf4j
public class ShowcaseSaga {

    private ShowcaseStatus showcaseStatus;

    @StartSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseScheduledEvent event, @NonNull DeadlineManager deadlineManager) {
        showcaseStatus = ShowcaseStatus.SCHEDULED;

        deadlineManager.schedule(event.getStartTime(), "startShowcase", event.getShowcaseId());

        log.debug("Scheduled deadline to start showcase with ID {} at {}", event.getShowcaseId(), event.getStartTime());
    }

    @DeadlineHandler(deadlineName = "startShowcase")
    void startShowcase(@NonNull String showcaseId, @NonNull CommandGateway commandGateway) {
        if (ObjectUtils.notEqual(showcaseStatus, ShowcaseStatus.SCHEDULED)) {
            log.debug("On starting deadline, showcase has status {}, so skipping", showcaseStatus);
            return;
        }

        log.debug("Starting showcase with ID {}...", showcaseId);

        try {
            commandGateway.sendAndWait(
                    StartShowcaseCommand
                            .builder()
                            .showcaseId(showcaseId)
                            .build());

            log.debug("Started showcase with ID {}", showcaseId);
        } catch (CommandExecutionException e) {
            val errorDetails = e.getDetails();
            if (errorDetails.isPresent()) {
                log.error("Failed to start showcase with ID {}, details: {}", showcaseId, errorDetails.get());
            } else {
                log.error("Failed to start showcase with ID {}", showcaseId, e);
            }
        }
    }

    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseStartedEvent event, @NonNull DeadlineManager deadlineManager) {
        showcaseStatus = ShowcaseStatus.STARTED;

        val finishTime = event.getStartedAt().plus(event.getDuration());

        deadlineManager.schedule(finishTime, "finishShowcase", event.getShowcaseId());

        log.debug("Scheduled deadline to finish showcase with ID {} at {}", event.getShowcaseId(), finishTime);
    }

    @EndSaga
    @DeadlineHandler(deadlineName = "finishShowcase")
    void finishShowcase(@NonNull String showcaseId, @NonNull CommandGateway commandGateway) {
        if (ObjectUtils.notEqual(showcaseStatus, ShowcaseStatus.STARTED)) {
            log.debug("On finishing deadline, showcase has status {}, so skipping", showcaseStatus);
            return;
        }

        log.debug("Finishing showcase with ID {}...", showcaseId);

        try {
            commandGateway.sendAndWait(
                    FinishShowcaseCommand
                            .builder()
                            .showcaseId(showcaseId)
                            .build());

            log.debug("Finished showcase with ID {}", showcaseId);
        } catch (CommandExecutionException e) {
            val errorDetails = e.getDetails();
            if (errorDetails.isPresent()) {
                log.error("Failed to finish showcase with ID {}, details: {}", showcaseId, errorDetails.get());
            } else {
                log.error("Failed to finish showcase with ID {}", showcaseId, e);
            }
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseFinishedEvent event) {
        showcaseStatus = ShowcaseStatus.FINISHED;
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseRemovedEvent event) {
        showcaseStatus = null;
    }
}
