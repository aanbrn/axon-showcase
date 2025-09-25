package showcase.saga;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@ProcessingGroup("showcaseManagementSaga")
@Slf4j
public final class ShowcaseManagementSaga {

    private String startShowcaseDeadlineId;

    private String finishShowcaseDeadlineId;

    @StartSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseScheduledEvent event, @NonNull DeadlineManager deadlineManager) {
        log.debug("Scheduling starting deadline for scheduled showcase: {}", event);

        startShowcaseDeadlineId = deadlineManager.schedule(
                event.getStartTime(), "startShowcase", event.getShowcaseId());
    }

    @DeadlineHandler(deadlineName = "startShowcase")
    void startShowcase(@NonNull String showcaseId, @NonNull StartShowcaseUseCase startShowcaseUseCase) {
        log.debug("Starting showcase with ID on deadline: {}", showcaseId);

        startShowcaseDeadlineId = null;

        startShowcaseUseCase
                .start(StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
                .doOnError(t -> {
                    if (t instanceof ShowcaseCommandException e) {
                        log.error("Failed to start showcase with ID {}, errorDetails: {}",
                                  showcaseId, e.getErrorDetails());
                    } else {
                        log.error("Failed to start showcase with ID {}", showcaseId, t);
                    }
                })
                .block();
    }

    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseStartedEvent event, @NonNull DeadlineManager deadlineManager) {
        if (startShowcaseDeadlineId != null) {
            log.debug("Cancelling starting deadline on showcase start: {}", event);

            try {
                deadlineManager.cancelSchedule("startShowcase", startShowcaseDeadlineId);
            } catch (Exception e) {
                log.error("Failed to cancel starting deadline on showcase start: {}", startShowcaseDeadlineId, e);
            } finally {
                startShowcaseDeadlineId = null;
            }
        }

        log.debug("Scheduling finishing deadline for started showcase: {}", event);

        finishShowcaseDeadlineId = deadlineManager.schedule(
                event.getStartedAt().plus(event.getDuration()), "finishShowcase", event.getShowcaseId());
    }

    @DeadlineHandler(deadlineName = "finishShowcase")
    void finishShowcase(@NonNull String showcaseId, @NonNull FinishShowcaseUseCase finishShowcaseUseCase) {
        log.debug("Finishing showcase with ID on deadline: {}", showcaseId);

        finishShowcaseDeadlineId = null;

        finishShowcaseUseCase
                .finish(FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .doOnError(t -> {
                    if (t instanceof ShowcaseCommandException e) {
                        log.error("Failed to finish showcase with ID {}, errorDetails: {}",
                                  showcaseId, e.getErrorDetails());
                    } else {
                        log.error("Failed to finish showcase with ID {}", showcaseId, t);
                    }
                })
                .block();
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseFinishedEvent event, @NonNull DeadlineManager deadlineManager) {
        if (finishShowcaseDeadlineId != null) {
            log.debug("Cancelling finishing deadline on showcase finish: {}", event);

            try {
                deadlineManager.cancelSchedule("finishShowcase", finishShowcaseDeadlineId);
            } catch (Exception e) {
                log.error("Failed to cancel finishing deadline on showcase finish: {}", finishShowcaseDeadlineId, e);
            } finally {
                finishShowcaseDeadlineId = null;
            }
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "showcaseId")
    void handle(@NonNull ShowcaseRemovedEvent event, @NonNull DeadlineManager deadlineManager) {
        if (startShowcaseDeadlineId != null) {
            log.debug("Cancelling starting deadline on showcase remove: {}", event);

            try {
                deadlineManager.cancelSchedule("startShowcase", startShowcaseDeadlineId);
            } catch (Exception e) {
                log.error("Failed to cancel starting deadline on showcase remove: {}", startShowcaseDeadlineId, e);
            } finally {
                startShowcaseDeadlineId = null;
            }
        }
        if (finishShowcaseDeadlineId != null) {
            log.debug("Cancelling finishing deadline on showcase remove: {}", event);

            try {
                deadlineManager.cancelSchedule("finishShowcase", finishShowcaseDeadlineId);
            } catch (Exception e) {
                log.error("Failed to cancel finishing deadline on showcase remove: {}", finishShowcaseDeadlineId, e);
            } finally {
                finishShowcaseDeadlineId = null;
            }
        }
    }
}
