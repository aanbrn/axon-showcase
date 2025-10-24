package showcase.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Aggregate;
import org.jspecify.annotations.Nullable;
import showcase.command.ShowcaseTitleReservation.DuplicateTitleException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
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
    @Nullable
    private String showcaseId;

    @Nullable
    private String title;

    @Nullable
    private Instant startTime;

    @Nullable
    private Duration duration;

    @Nullable
    private ShowcaseStatus status;

    @Nullable
    private Instant scheduledAt;

    @Nullable
    private Instant startedAt;

    @Nullable
    private Instant finishedAt;

    @Nullable
    private Instant removedAt;

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    void handle(ScheduleShowcaseCommand command, ShowcaseTitleReservation showcaseTitleReservation) {
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
    void handle(StartShowcaseCommand command) {
        checkState(Objects.equals(showcaseId, command.showcaseId()), "\"showcaseId\" must be same as command's one");
        checkState(Objects.nonNull(duration), "\"duration\" is required");

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
    void handle(FinishShowcaseCommand command) {
        checkState(Objects.equals(showcaseId, command.showcaseId()), "\"showcaseId\" must be same as command's one");

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
    void handle(RemoveShowcaseCommand command, ShowcaseTitleReservation showcaseTitleReservation) {
        checkState(Objects.equals(showcaseId, command.showcaseId()), "\"showcaseId\" must be same as command's one");
        checkState(Objects.nonNull(title), "\"title\" is required");

        showcaseTitleReservation.delete(title);

        val now = clock.instant();

        if (status == ShowcaseStatus.STARTED) {
            val event =
                    ShowcaseFinishedEvent
                            .builder()
                            .showcaseId(showcaseId)
                            .finishedAt(now)
                            .build();

            log.trace("Showcase finished on remove: {}", event);

            apply(event);
        }
        {
            val event =
                    ShowcaseRemovedEvent
                            .builder()
                            .showcaseId(showcaseId)
                            .removedAt(now)
                            .build();

            log.trace("Showcase removed: {}", event);

            apply(event);
        }
    }

    @EventSourcingHandler
    void on(ShowcaseScheduledEvent event) {
        this.showcaseId = event.showcaseId();
        this.title = event.title();
        this.startTime = event.startTime();
        this.duration = event.duration();
        this.status = ShowcaseStatus.SCHEDULED;
        this.scheduledAt = event.scheduledAt();
    }

    @EventSourcingHandler
    void on(ShowcaseStartedEvent event) {
        this.status = ShowcaseStatus.STARTED;
        this.duration = event.duration();
        this.startedAt = event.startedAt();
    }

    @EventSourcingHandler
    void on(ShowcaseFinishedEvent event) {
        this.status = ShowcaseStatus.FINISHED;
        this.finishedAt = event.finishedAt();
    }

    @EventSourcingHandler
    void on(ShowcaseRemovedEvent event) {
        this.removedAt = event.removedAt();

        markDeleted();
    }
}
