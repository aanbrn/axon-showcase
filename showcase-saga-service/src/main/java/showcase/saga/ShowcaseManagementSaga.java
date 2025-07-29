package showcase.saga;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import showcase.command.FinishShowcaseCommand;
import showcase.command.FinishShowcaseUseCase;
import showcase.command.ShowcaseCommandException;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.command.StartShowcaseCommand;
import showcase.command.StartShowcaseUseCase;

@Saga
@ProcessingGroup("sagas")
@Slf4j
public final class ShowcaseManagementSaga {

    private String startShowcaseId;

    private String finishShowcaseId;

    @StartSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseScheduledEvent event, @NonNull DeadlineManager deadlineManager) {
        log.info("Scheduling starting deadline for scheduled showcase: {}", event);

        startShowcaseId = deadlineManager.schedule(event.getStartTime(), "startShowcase", event.getShowcaseId());
    }

    @DeadlineHandler(deadlineName = "startShowcase")
    void startShowcase(@NonNull String showcaseId, @NonNull StartShowcaseUseCase startShowcaseUseCase) {
        log.info("Starting showcase with ID on deadline: {}", showcaseId);

        try {
            startShowcaseUseCase
                    .start(StartShowcaseCommand
                                   .builder()
                                   .showcaseId(showcaseId)
                                   .build())
                    .block();
        } catch (ShowcaseCommandException e) {
            log.warn("Failed to start showcase with ID {}, errorDetails: {}", showcaseId, e.getErrorDetails());
        }
    }

    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseStartedEvent event, @NonNull DeadlineManager deadlineManager) {
        if (startShowcaseId != null) {
            log.info("Cancelling starting deadline on showcase start: {}", event);

            try {
                deadlineManager.cancelSchedule("startShowcase", startShowcaseId);
            } finally {
                startShowcaseId = null;
            }
        }

        log.info("Scheduling finishing deadline for started showcase: {}", event);

        val finishTime = event.getStartedAt().plus(event.getDuration());
        finishShowcaseId = deadlineManager.schedule(finishTime, "finishShowcase", event.getShowcaseId());
    }

    @DeadlineHandler(deadlineName = "finishShowcase")
    void finishShowcase(@NonNull String showcaseId, @NonNull FinishShowcaseUseCase finishShowcaseUseCase) {
        log.info("Finishing showcase with ID on deadline: {}", showcaseId);

        try {
            finishShowcaseUseCase
                    .finish(FinishShowcaseCommand
                                    .builder()
                                    .showcaseId(showcaseId)
                                    .build())
                    .block();
        } catch (ShowcaseCommandException e) {
            log.error("Failed to finish showcase with ID {}, errorDetails: {}", showcaseId, e.getErrorDetails());
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseFinishedEvent event, @NonNull DeadlineManager deadlineManager) {
        if (finishShowcaseId != null) {
            log.info("Cancelling finishing deadline on showcase finish: {}", event);

            try {
                deadlineManager.cancelSchedule("finishShowcase", finishShowcaseId);
            } finally {
                finishShowcaseId = null;
            }
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseRemovedEvent event, @NonNull DeadlineManager deadlineManager) {
        if (startShowcaseId != null) {
            log.info("Cancelling starting deadline on showcase remove: {}", event);

            try {
                deadlineManager.cancelSchedule("startShowcase", startShowcaseId);
            } finally {
                startShowcaseId = null;
            }
        }
        if (finishShowcaseId != null) {
            log.info("Cancelling finishing deadline on showcase remove: {}", event);

            try {
                deadlineManager.cancelSchedule("finishShowcase", finishShowcaseId);
            } finally {
                finishShowcaseId = null;
            }
        }
    }
}
