package showcase.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@Slf4j
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

    private Instant removedAt;

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    void handle(@NonNull ScheduleShowcaseCommand command,
                @NonNull ShowcaseTitleReservation showcaseTitleReservation) {
        if (Objects.equals(showcaseId, command.getShowcaseId())) {
            if (Objects.equals(title, command.getTitle())
                        && Objects.equals(status, ShowcaseStatus.SCHEDULED)
                        && Objects.equals(startTime, command.getStartTime())
                        && Objects.equals(duration, command.getDuration())) {
                log.debug("Retry to schedule showcase: {}", command);
                return;
            } else {
                log.error("Attempt to reuse showcase ID: {}", command);

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
            log.error("Attempt to reuse showcase title: {}", command);

            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails
                            .builder()
                            .errorCode(ShowcaseCommandErrorCode.TITLE_IN_USE)
                            .errorMessage("Given title is in use already")
                            .build(),
                    e.getCause());
        }

        val event =
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(command.getShowcaseId())
                        .title(command.getTitle())
                        .startTime(command.getStartTime())
                        .duration(command.getDuration())
                        .scheduledAt(clock.instant())
                        .build();

        log.debug("Showcase scheduled: {}", event);

        apply(event);
    }

    @CommandHandler
    void handle(@NonNull StartShowcaseCommand command) {
        if (status == ShowcaseStatus.FINISHED) {
            log.error("Attempt to start already finished showcase: {}", command);

            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails
                            .builder()
                            .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                            .errorMessage("Showcase is finished already")
                            .build());
        }
        if (status == ShowcaseStatus.STARTED) {
            log.debug("Retry to start showcase: {}", command);
            return;
        }

        val event =
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(clock.instant())
                        .build();

        log.debug("Showcase started: {}", event);

        apply(event);
    }

    @CommandHandler
    void handle(@NonNull FinishShowcaseCommand command) {
        if (status == ShowcaseStatus.SCHEDULED) {
            log.error("Attempt to finish not started yet showcase: {}", command);

            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails
                            .builder()
                            .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                            .errorMessage("Showcase must be started first")
                            .build());
        }
        if (status == ShowcaseStatus.FINISHED) {
            log.debug("Retry to finish showcase: {}", command);
            return;
        }

        val event =
                ShowcaseFinishedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .finishedAt(clock.instant())
                        .build();

        log.debug("Showcase finished: {}", event);

        apply(event);
    }

    @CommandHandler
    void handle(
            @NonNull RemoveShowcaseCommand command,
            @NonNull ShowcaseTitleReservation showcaseTitleReservation) {
        showcaseTitleReservation.delete(title);

        val now = clock.instant();

        if (status == ShowcaseStatus.STARTED) {
            val event =
                    ShowcaseFinishedEvent
                            .builder()
                            .showcaseId(command.getShowcaseId())
                            .finishedAt(now)
                            .build();

            log.debug("Showcase finished on remove: {}", event);

            apply(event);
        }
        {
            val event =
                    ShowcaseRemovedEvent
                            .builder()
                            .showcaseId(command.getShowcaseId())
                            .removedAt(now)
                            .build();

            log.debug("Showcase removed: {}", event);

            apply(event);
        }
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
        this.removedAt = event.getRemovedAt();

        markDeleted();
    }
}
