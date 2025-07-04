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
        startShowcaseId = deadlineManager.schedule(event.getStartTime(), "startShowcase", event.getShowcaseId());
    }

    @DeadlineHandler(deadlineName = "startShowcase")
    void startShowcase(@NonNull String showcaseId, @NonNull StartShowcaseUseCase startShowcaseUseCase) {
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
            try {
                deadlineManager.cancelSchedule("startShowcase", startShowcaseId);
            } finally {
                startShowcaseId = null;
            }
        }
        val finishTime = event.getStartedAt().plus(event.getDuration());
        finishShowcaseId = deadlineManager.schedule(finishTime, "finishShowcase", event.getShowcaseId());
    }

    @DeadlineHandler(deadlineName = "finishShowcase")
    void finishShowcase(@NonNull String showcaseId, @NonNull FinishShowcaseUseCase finishShowcaseUseCase) {
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
            try {
                deadlineManager.cancelSchedule("startShowcase", startShowcaseId);
            } finally {
                startShowcaseId = null;
            }
        }
        if (finishShowcaseId != null) {
            try {
                deadlineManager.cancelSchedule("finishShowcase", finishShowcaseId);
            } finally {
                finishShowcaseId = null;
            }
        }
    }
}
