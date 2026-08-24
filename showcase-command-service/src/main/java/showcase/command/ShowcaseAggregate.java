// SPDX-License-Identifier: MIT
package showcase.command;

import static com.google.common.base.Preconditions.checkState;
import static org.axonframework.eventhandling.GenericEventMessage.clock;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
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

/**
 * Axon aggregate managing the lifecycle of a showcase.
 *
 * <p>Supports four commands: schedule, start, finish, and remove. Events are persisted via Axon's event sourcing
 * mechanism and also published to Kafka through the command service. The aggregate uses a title reservation service
 * to enforce uniqueness of showcase titles.
 */
@Aggregate(cache = "showcaseCache", snapshotTriggerDefinition = "showcaseSnapshotTrigger")
@Revision("1.0")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(AccessLevel.PACKAGE)
@Slf4j
final class ShowcaseAggregate {
    /**
     * The unique identifier of the showcase.
     */
    @AggregateIdentifier
    @Nullable
    private String showcaseId;

    /**
     * The unique title of the showcase.
     */
    @Nullable
    private String title;

    /**
     * The date-time when the showcase should start automatically.
     */
    @Nullable
    private Instant startTime;

    /**
     * The duration after which the started showcase should be finished automatically.
     */
    @Nullable
    private Duration duration;

    /**
     * The current lifecycle status of the showcase.
     */
    @Nullable
    private ShowcaseStatus status;

    /**
     * The date-time when the showcase was scheduled.
     */
    @Nullable
    private Instant scheduledAt;

    /**
     * The date-time when the showcase was started.
     */
    @Nullable
    private Instant startedAt;

    /**
     * The date-time when the showcase was finished.
     */
    @Nullable
    private Instant finishedAt;

    /**
     * The date-time when the showcase was removed.
     */
    @Nullable
    private Instant removedAt;

    /**
     * Schedules a new showcase.
     *
     * <p>If the aggregate already exists with the same ID and identical parameters, the command is a no-op
     * (idempotent retry). Reusing an ID with different parameters is rejected. Title uniqueness is enforced via the
     * title reservation service.
     *
     * @param command the schedule command to handle
     * @param showcaseTitleReservation the title reservation service
     */
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

                throw new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
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
                    ShowcaseCommandErrorDetails.builder()
                            .errorCode(ShowcaseCommandErrorCode.TITLE_IN_USE)
                            .errorMessage("Given title is in use already")
                            .build(),
                    e.getCause());
        }

        val event = ShowcaseScheduledEvent.builder()
                .showcaseId(command.showcaseId())
                .title(command.title())
                .startTime(command.startTime())
                .duration(command.duration())
                .scheduledAt(clock.instant())
                .build();

        log.trace("Showcase scheduled: {}", event);

        apply(event);
    }

    /**
     * Starts a scheduled showcase.
     *
     * <p>The showcase must be in {@link ShowcaseStatus#SCHEDULED} state. Starting an already started showcase is
     * a no-op (idempotent retry). Finishing an already finished showcase is rejected.
     *
     * @param command the start command to handle
     */
    @CommandHandler
    void handle(StartShowcaseCommand command) {
        checkState(Objects.equals(showcaseId, command.showcaseId()), "\"showcaseId\" must be same as command's one");
        checkState(Objects.nonNull(duration), "\"duration\" is required");

        if (status == ShowcaseStatus.FINISHED) {
            log.error("Attempt to start already finished showcase: {}", command);

            throw new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                    .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                    .errorMessage("Showcase is finished already")
                    .build());
        }
        if (status == ShowcaseStatus.STARTED) {
            log.trace("Retry to start showcase: {}", command);
            return;
        }

        val event = ShowcaseStartedEvent.builder()
                .showcaseId(showcaseId)
                .duration(duration)
                .startedAt(clock.instant())
                .build();

        log.trace("Showcase started: {}", event);

        apply(event);
    }

    /**
     * Finishes a started showcase.
     *
     * <p>The showcase must be in {@link ShowcaseStatus#STARTED} state. Finishing an already finished showcase is
     * a no-op (idempotent retry). Finishing a not-yet-started showcase is rejected.
     *
     * @param command the finish command to handle
     */
    @CommandHandler
    void handle(FinishShowcaseCommand command) {
        checkState(Objects.equals(showcaseId, command.showcaseId()), "\"showcaseId\" must be same as command's one");

        if (status == ShowcaseStatus.SCHEDULED) {
            log.error("Attempt to finish not started yet showcase: {}", command);

            throw new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                    .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                    .errorMessage("Showcase must be started first")
                    .build());
        }
        if (status == ShowcaseStatus.FINISHED) {
            log.trace("Retry to finish showcase: {}", command);
            return;
        }

        val event = ShowcaseFinishedEvent.builder()
                .showcaseId(showcaseId)
                .finishedAt(clock.instant())
                .build();

        log.trace("Showcase finished: {}", event);

        apply(event);
    }

    /**
     * Removes a showcase, finishing it first if it has already started.
     *
     * <p>Releases the title reservation before applying events. If the showcase was started, a
     * {@link ShowcaseFinishedEvent} is emitted first, followed by a {@link ShowcaseRemovedEvent}.
     *
     * @param command the remove command to handle
     * @param showcaseTitleReservation the title reservation service
     */
    @CommandHandler
    void handle(RemoveShowcaseCommand command, ShowcaseTitleReservation showcaseTitleReservation) {
        checkState(Objects.equals(showcaseId, command.showcaseId()), "\"showcaseId\" must be same as command's one");
        checkState(Objects.nonNull(title), "\"title\" is required");

        showcaseTitleReservation.delete(title);

        val now = clock.instant();

        if (status == ShowcaseStatus.STARTED) {
            val event = ShowcaseFinishedEvent.builder()
                    .showcaseId(showcaseId)
                    .finishedAt(now)
                    .build();

            log.trace("Showcase finished on remove: {}", event);

            apply(event);
        }
        {
            val event = ShowcaseRemovedEvent.builder()
                    .showcaseId(showcaseId)
                    .removedAt(now)
                    .build();

            log.trace("Showcase removed: {}", event);

            apply(event);
        }
    }

    /**
     * Applies the scheduled event to the aggregate state.
     *
     * @param event the event to apply
     */
    @EventSourcingHandler
    void on(ShowcaseScheduledEvent event) {
        this.showcaseId = event.showcaseId();
        this.title = event.title();
        this.startTime = event.startTime();
        this.duration = event.duration();
        this.status = ShowcaseStatus.SCHEDULED;
        this.scheduledAt = event.scheduledAt();
    }

    /**
     * Applies the started event to the aggregate state.
     *
     * @param event the event to apply
     */
    @EventSourcingHandler
    void on(ShowcaseStartedEvent event) {
        this.status = ShowcaseStatus.STARTED;
        this.duration = event.duration();
        this.startedAt = event.startedAt();
    }

    /**
     * Applies the finished event to the aggregate state.
     *
     * @param event the event to apply
     */
    @EventSourcingHandler
    void on(ShowcaseFinishedEvent event) {
        this.status = ShowcaseStatus.FINISHED;
        this.finishedAt = event.finishedAt();
    }

    /**
     * Applies the removed event to the aggregate state and marks the aggregate as deleted.
     *
     * @param event the event to apply
     */
    @EventSourcingHandler
    void on(ShowcaseRemovedEvent event) {
        this.removedAt = event.removedAt();

        markDeleted();
    }
}
