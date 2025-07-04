package showcase.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Aggregate;
import showcase.command.ShowcaseTitleReservation.DuplicateTitleException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static org.axonframework.eventhandling.GenericEventMessage.clock;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate(cache = "showcaseCache", snapshotTriggerDefinition = "showcaseSnapshotTrigger")
@Revision("1.0")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(AccessLevel.PACKAGE)
final class ShowcaseAggregate {

    @AggregateIdentifier
    private String showcaseId;

    private String title;

    private Instant startTime;

    private Duration duration;

    private ShowcaseStatus status;

    private Instant scheduledAt;

    private Instant startedAt;

    private Instant finishedAt;

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    void handle(@NonNull ScheduleShowcaseCommand command,
                @NonNull ShowcaseTitleReservation showcaseTitleReservation) {
        if (Objects.equals(showcaseId, command.getShowcaseId())) {
            if (Objects.equals(title, command.getTitle())
                        && Objects.equals(status, ShowcaseStatus.SCHEDULED)
                        && Objects.equals(startTime, command.getStartTime())
                        && Objects.equals(duration, command.getDuration())) {
                return;
            } else {
                throw new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails
                                .builder()
                                .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                                .errorMessage("Showcase cannot be rescheduled")
                                .build());
            }
        }

        try {
            showcaseTitleReservation.save(command.getTitle());
        } catch (DuplicateTitleException e) {
            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails
                            .builder()
                            .errorCode(ShowcaseCommandErrorCode.TITLE_IN_USE)
                            .errorMessage("Given title is in use already")
                            .build(),
                    e.getCause());
        }

        apply(ShowcaseScheduledEvent
                      .builder()
                      .showcaseId(command.getShowcaseId())
                      .title(command.getTitle())
                      .startTime(command.getStartTime())
                      .duration(command.getDuration())
                      .scheduledAt(clock.instant())
                      .build());
    }

    @CommandHandler
    void handle(@NonNull StartShowcaseCommand command) {
        if (status == ShowcaseStatus.FINISHED) {
            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails
                            .builder()
                            .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                            .errorMessage("Showcase is finished already")
                            .build());
        }
        if (status == ShowcaseStatus.STARTED) {
            return;
        }

        apply(ShowcaseStartedEvent
                      .builder()
                      .showcaseId(showcaseId)
                      .duration(duration)
                      .startedAt(clock.instant())
                      .build());
    }

    @CommandHandler
    void handle(@NonNull FinishShowcaseCommand command) {
        if (status == ShowcaseStatus.SCHEDULED) {
            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails
                            .builder()
                            .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                            .errorMessage("Showcase must be started first")
                            .build());
        }
        if (status == ShowcaseStatus.FINISHED) {
            return;
        }

        apply(ShowcaseFinishedEvent
                      .builder()
                      .showcaseId(showcaseId)
                      .finishedAt(clock.instant())
                      .build());
    }

    @CommandHandler
    void handle(
            @NonNull RemoveShowcaseCommand command,
            @NonNull ShowcaseTitleReservation showcaseTitleReservation) {
        showcaseTitleReservation.delete(title);

        if (status == ShowcaseStatus.STARTED) {
            apply(ShowcaseFinishedEvent
                          .builder()
                          .showcaseId(command.getShowcaseId())
                          .finishedAt(clock.instant())
                          .build());
        }
        apply(ShowcaseRemovedEvent
                      .builder()
                      .showcaseId(command.getShowcaseId())
                      .build());
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseScheduledEvent event) {
        this.showcaseId = event.getShowcaseId();
        this.title = event.getTitle();
        this.startTime = event.getStartTime();
        this.duration = event.getDuration();
        this.status = ShowcaseStatus.SCHEDULED;
        this.scheduledAt = event.getScheduledAt();
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseStartedEvent event) {
        this.status = ShowcaseStatus.STARTED;
        this.startedAt = event.getStartedAt();
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseFinishedEvent event) {
        this.status = ShowcaseStatus.FINISHED;
        this.finishedAt = event.getFinishedAt();
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseRemovedEvent event) {
        markDeleted();
    }
}
