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
        if (Objects.equals(showcaseId, command.showcaseId())) {
            if (Objects.equals(title, command.title())
                        && Objects.equals(status, ShowcaseStatus.SCHEDULED)
                        && Objects.equals(startTime, command.startTime())
                        && Objects.equals(duration, command.duration())) {
                log.trace("Retry to schedule showcase: {}", command);
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
            showcaseTitleReservation.save(command.title());
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
                        .showcaseId(command.showcaseId())
                        .title(command.title())
                        .startTime(command.startTime())
                        .duration(command.duration())
                        .scheduledAt(clock.instant())
                        .build();

        log.trace("Showcase scheduled: {}", event);

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
            log.trace("Retry to start showcase: {}", command);
            return;
        }

        val event =
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(clock.instant())
                        .build();

        log.trace("Showcase started: {}", event);

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
            log.trace("Retry to finish showcase: {}", command);
            return;
        }

        val event =
                ShowcaseFinishedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .finishedAt(clock.instant())
                        .build();

        log.trace("Showcase finished: {}", event);

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
                            .showcaseId(command.showcaseId())
                            .finishedAt(now)
                            .build();

            log.trace("Showcase finished on remove: {}", event);

            apply(event);
        }
        {
            val event =
                    ShowcaseRemovedEvent
                            .builder()
                            .showcaseId(command.showcaseId())
                            .removedAt(now)
                            .build();

            log.trace("Showcase removed: {}", event);

            apply(event);
        }
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseScheduledEvent event) {
        this.showcaseId = event.showcaseId();
        this.title = event.title();
        this.startTime = event.startTime();
        this.duration = event.duration();
        this.status = ShowcaseStatus.SCHEDULED;
        this.scheduledAt = event.scheduledAt();
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseStartedEvent event) {
        this.status = ShowcaseStatus.STARTED;
        this.startedAt = event.startedAt();
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseFinishedEvent event) {
        this.status = ShowcaseStatus.FINISHED;
        this.finishedAt = event.finishedAt();
    }

    @EventSourcingHandler
    void on(@NonNull ShowcaseRemovedEvent event) {
        this.removedAt = event.removedAt();

        markDeleted();
    }
}
