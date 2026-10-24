// SPDX-License-Identifier: MIT
package showcase.projection;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.monitoring.MessageMonitor.MonitorCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import showcase.command.ShowcaseEvent;

/**
 * Writes consumed showcase events in causal order, one event per aggregate per dispatch round.
 *
 * <p>A batch is grouped by showcase id and drained in rounds: each round sends at most one event per aggregate, a
 * successful item releases that aggregate's next event into the following round, and a retryable item keeps its
 * aggregate's successor undispatched until the item is resolved. The batch completes only when every item has a
 * resolved outcome.
 */
@Component
@Slf4j
class ShowcaseBatchWriter {
    /**
     * Executes one round of projection events and classifies each of them.
     */
    @FunctionalInterface
    interface RoundExecutor {
        /**
         * Executes the given events and returns one outcome per event, in order.
         *
         * @param events the round's events, one per aggregate
         * @return the outcomes, one per event
         */
        Mono<List<ItemOutcome>> execute(List<ProjectionItem> events);
    }

    /**
     * The classified outcome of one written event.
     */
    enum ItemOutcome {
        /**
         * The event was applied.
         */
        APPLIED,
        /**
         * The event was already applied, so it converges on the existing document.
         */
        CONVERGED,
        /**
         * The event failed terminally and is skipped.
         */
        TERMINAL,
        /**
         * The event failed with a retryable status.
         */
        RETRYABLE,
        /**
         * The event exhausted its retry budget and is skipped.
         */
        EXHAUSTED
    }

    /**
     * One event to write, with its aggregate sequence and monitor callback.
     *
     * @param sequence the aggregate sequence of the event
     * @param payload the event payload
     * @param callback the monitor callback reported with the item's outcome
     */
    record ProjectionItem(long sequence, ShowcaseEvent payload, MonitorCallback callback) {}

    /**
     * The projector settings supplying the retry budget.
     */
    private final ShowcaseProjectorProperties properties;

    /**
     * The executor writing one round of events.
     */
    private final RoundExecutor executor;

    /**
     * Creates a writer for the given settings and executor.
     *
     * @param properties the projector settings supplying the retry budget
     * @param executor the executor writing one round of events
     */
    ShowcaseBatchWriter(ShowcaseProjectorProperties properties, RoundExecutor executor) {
        this.properties = properties;
        this.executor = executor;
    }

    /**
     * Writes the given events, draining the batch in rounds.
     *
     * @param events the events to write
     * @return a {@link Mono} completing once every event has a resolved outcome
     */
    Mono<Void> write(List<ProjectionItem> events) {
        Map<String, Deque<ProjectionItem>> queues = new LinkedHashMap<>();
        for (val event : events) {
            queues.computeIfAbsent(event.payload().showcaseId(), __ -> new ArrayDeque<>())
                    .add(event);
        }
        return drain(queues);
    }

    /**
     * Sends one round with at most one event per aggregate and recurses until every queue is empty.
     *
     * @param queues the undispatched events per aggregate
     * @return a {@link Mono} completing once the batch is drained
     */
    private Mono<Void> drain(Map<String, Deque<ProjectionItem>> queues) {
        val heads = new ArrayList<Head>();
        for (val queue : queues.values()) {
            if (!queue.isEmpty()) {
                heads.add(new Head(queue, queue.peek()));
            }
        }
        if (heads.isEmpty()) {
            return Mono.empty();
        }
        val events = heads.stream().map(Head::event).toList();
        return executeWithRetry(events, 0).flatMap(outcomes -> {
            for (int i = 0; i < heads.size(); i++) {
                val head = heads.get(i);
                head.queue().poll();
                applyOutcome(head.event(), outcomes.get(i));
            }
            return drain(queues);
        });
    }

    /**
     * Executes the round, re-sending only the retryable events until none remain or the budget is spent.
     *
     * @param events the round's events
     * @param attempt the number of re-sends already made
     * @return one outcome per event
     */
    private Mono<List<ItemOutcome>> executeWithRetry(List<ProjectionItem> events, int attempt) {
        return executor.execute(events).flatMap(outcomes -> {
            if (outcomes.size() != events.size()) {
                return Mono.error(new IllegalStateException(
                        "Expected %d bulk outcomes, received %d".formatted(events.size(), outcomes.size())));
            }
            val resolved = new ItemOutcome[events.size()];
            Arrays.fill(resolved, ItemOutcome.EXHAUSTED);
            val retryable = new ArrayList<Integer>();
            for (int i = 0; i < outcomes.size(); i++) {
                val outcome = outcomes.get(i);
                if (outcome == ItemOutcome.RETRYABLE
                        && attempt < properties.getRetry().getMaxAttempts()) {
                    retryable.add(i);
                } else if (outcome != ItemOutcome.RETRYABLE) {
                    resolved[i] = outcome;
                }
            }
            if (retryable.isEmpty()) {
                return Mono.just(Arrays.asList(resolved));
            }
            val retryEvents = retryable.stream().map(events::get).toList();
            return Mono.delay(delay(attempt))
                    .then(Mono.defer(() -> executeWithRetry(retryEvents, attempt + 1)))
                    .map(second -> merge(resolved, retryable, second));
        });
    }

    /**
     * Merges a retry round's outcomes into the resolved slots; a slot the retry did not resolve keeps its default,
     * which marks the event as having exhausted its retry budget.
     *
     * @param resolved the round's outcomes, defaulting to exhausted
     * @param retried the retried events' positions
     * @param second the retry round's outcomes
     * @return the outcomes in order
     */
    private static List<ItemOutcome> merge(ItemOutcome[] resolved, List<Integer> retried, List<ItemOutcome> second) {
        for (int i = 0; i < retried.size(); i++) {
            resolved[retried.get(i)] = second.get(i);
        }
        return Arrays.asList(resolved);
    }

    /**
     * Computes the delay before the next re-send, doubling up to a fixed cap.
     *
     * @param attempt the number of re-sends already made
     * @return the delay
     */
    private Duration delay(int attempt) {
        return properties.getRetry().getMinBackoff().multipliedBy(1L << Math.min(attempt, 10));
    }

    /**
     * Reports one event's outcome to its monitor callback and logs it.
     *
     * @param event the event whose outcome resolved
     * @param outcome the outcome
     */
    private static void applyOutcome(ProjectionItem event, ItemOutcome outcome) {
        switch (outcome) {
            case APPLIED -> {
                event.callback().reportSuccess();
                log.trace("On {}, [{}]: succeeded", event.payload().getClass().getSimpleName(), event.sequence());
            }
            case CONVERGED -> {
                event.callback().reportSuccess();
                log.debug(
                        "On {}, [{}]: converged on an already-applied event",
                        event.payload().getClass().getSimpleName(),
                        event.sequence());
            }
            case TERMINAL -> {
                event.callback().reportFailure(null);
                log.error(
                        "On {}, [{}]: skipped after a terminal write outcome",
                        event.payload().getClass().getSimpleName(),
                        event.sequence());
            }
            case EXHAUSTED -> {
                event.callback().reportFailure(null);
                log.error(
                        "On {}, [{}]: skipped after exhausting the retry budget",
                        event.payload().getClass().getSimpleName(),
                        event.sequence());
            }
            case RETRYABLE -> throw new IllegalStateException("Unresolved retryable outcome");
        }
    }

    /**
     * The head of one aggregate's queue in a round.
     *
     * @param queue the aggregate's undispatched events
     * @param event the next event to dispatch
     */
    private record Head(Deque<ProjectionItem> queue, ProjectionItem event) {}
}
