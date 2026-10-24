// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import lombok.val;
import org.axonframework.monitoring.MessageMonitor.MonitorCallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.projection.ShowcaseBatchWriter.ItemOutcome;
import showcase.projection.ShowcaseBatchWriter.ProjectionItem;
import showcase.projection.ShowcaseBatchWriter.RoundExecutor;

@DisplayName("Showcase batch writer tests")
class ShowcaseBatchWriterTests {

    private static final String SHOWCASE_A = "showcase-a";

    private static final String SHOWCASE_B = "showcase-b";

    @Test
    @DisplayName("A batch is drained in rounds with one event per aggregate per round")
    void write_drainsOneEventPerAggregatePerRound() {
        val executor = new RecordingExecutor(ItemOutcome.APPLIED, ItemOutcome.APPLIED, ItemOutcome.APPLIED);
        val writer = writer(executor, 3);
        val callbacks = List.of(mock(MonitorCallback.class), mock(MonitorCallback.class), mock(MonitorCallback.class));

        StepVerifier.create(writer.write(List.of(
                        scheduled(SHOWCASE_A, callbacks.getFirst()),
                        scheduled(SHOWCASE_B, callbacks.get(1)),
                        started(callbacks.get(2)))))
                .verifyComplete();

        assertThat(executor.rounds).hasSize(2);
        assertThat(executor.rounds.getFirst())
                .extracting(event -> event.payload().showcaseId())
                .containsExactly(SHOWCASE_A, SHOWCASE_B);
        assertThat(executor.rounds.get(1)).hasSize(1);
        assertThat(executor.rounds.get(1).getFirst().payload()).isInstanceOf(ShowcaseStartedEvent.class);
        callbacks.forEach(callback -> verify(callback).reportSuccess());
    }

    @Test
    @DisplayName("A retryable event keeps its aggregate's successor undispatched and is re-sent alone")
    void write_retryableItemHoldsTheSuccessorAndIsResentAlone() {
        val executor = new RecordingExecutor(
                ItemOutcome.RETRYABLE, ItemOutcome.APPLIED, ItemOutcome.APPLIED, ItemOutcome.APPLIED);
        val writer = writer(executor, 3);

        StepVerifier.create(writer.write(List.of(
                        scheduled(SHOWCASE_A, mock(MonitorCallback.class)),
                        scheduled(SHOWCASE_B, mock(MonitorCallback.class)),
                        started(mock(MonitorCallback.class)))))
                .verifyComplete();

        assertThat(executor.rounds).hasSize(3);
        assertThat(executor.rounds.getFirst()).hasSize(2);
        assertThat(executor.rounds.get(1)).hasSize(1);
        assertThat(executor.rounds.get(1).getFirst().payload()).isInstanceOf(ShowcaseScheduledEvent.class);
        assertThat(executor.rounds.get(2).getFirst().payload()).isInstanceOf(ShowcaseStartedEvent.class);
    }

    @Test
    @DisplayName("An exhausted retry budget skips the event, reports the failure, and releases the successor")
    void write_exhaustedBudgetSkipsTheItemAndReleasesTheSuccessor() {
        val executor = new RecordingExecutor(ItemOutcome.RETRYABLE, ItemOutcome.RETRYABLE, ItemOutcome.APPLIED);
        val writer = writer(executor, 1);
        val firstCallback = mock(MonitorCallback.class);

        StepVerifier.create(writer.write(
                        List.of(scheduled(SHOWCASE_A, firstCallback), started(mock(MonitorCallback.class)))))
                .verifyComplete();

        assertThat(executor.rounds).hasSize(3);
        verify(firstCallback).reportFailure(null);
        assertThat(executor.rounds.get(2).getFirst().payload()).isInstanceOf(ShowcaseStartedEvent.class);
    }

    @Test
    @DisplayName("A terminal event is not re-sent while a retryable one in the same round is")
    void write_terminalItemIsNotResent() {
        val executor = new RecordingExecutor(ItemOutcome.TERMINAL, ItemOutcome.RETRYABLE, ItemOutcome.APPLIED);
        val writer = writer(executor, 3);

        StepVerifier.create(writer.write(List.of(
                        scheduled(SHOWCASE_A, mock(MonitorCallback.class)),
                        scheduled(SHOWCASE_B, mock(MonitorCallback.class)))))
                .verifyComplete();

        assertThat(executor.rounds).hasSize(2);
        assertThat(executor.rounds.get(1)).hasSize(1);
        assertThat(executor.rounds.get(1).getFirst().payload().showcaseId()).isEqualTo(SHOWCASE_B);
    }

    @Test
    @DisplayName("A finished event is not written before the started event it follows has been applied")
    void write_finishedWaitsForTheRetriedStarted() {
        val executor = new RecordingExecutor(
                ItemOutcome.APPLIED, ItemOutcome.RETRYABLE, ItemOutcome.APPLIED, ItemOutcome.APPLIED);
        val writer = writer(executor, 3);

        StepVerifier.create(writer.write(List.of(
                        scheduled(SHOWCASE_A, mock(MonitorCallback.class)),
                        started(mock(MonitorCallback.class)),
                        finished(mock(MonitorCallback.class)))))
                .verifyComplete();

        assertThat(executor.rounds).hasSize(4);
        assertThat(executor.rounds.get(1).getFirst().payload()).isInstanceOf(ShowcaseStartedEvent.class);
        assertThat(executor.rounds.get(2).getFirst().payload()).isInstanceOf(ShowcaseStartedEvent.class);
        assertThat(executor.rounds.get(3).getFirst().payload()).isInstanceOf(ShowcaseFinishedEvent.class);
    }

    @Test
    @DisplayName("The batch does not complete while its final round's response is pending")
    void write_doesNotCompleteWhileTheFinalRoundIsPending() {
        val sink = Sinks.<List<ItemOutcome>>one();
        val writer = new ShowcaseBatchWriter(properties(3), events -> sink.asMono());

        val result = writer.write(List.of(scheduled(SHOWCASE_A, mock(MonitorCallback.class))));

        StepVerifier.create(result)
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(100))
                .then(() -> sink.tryEmitValue(List.of(ItemOutcome.APPLIED)))
                .verifyComplete();
    }

    @Test
    @DisplayName("A failed write leaves the batch in error so the acknowledgement cannot run")
    void write_failedWriteLeavesTheBatchInError() {
        val writer =
                new ShowcaseBatchWriter(properties(3), events -> Mono.error(new IllegalStateException("write failed")));

        StepVerifier.create(writer.write(List.of(scheduled(SHOWCASE_A, mock(MonitorCallback.class)))))
                .verifyError(IllegalStateException.class);
    }

    private ShowcaseBatchWriter writer(RecordingExecutor executor, int maxAttempts) {
        return new ShowcaseBatchWriter(properties(maxAttempts), executor);
    }

    private static ShowcaseProjectorProperties properties(int maxAttempts) {
        val properties = new ShowcaseProjectorProperties();
        properties.setRetry(new ShowcaseProjectorProperties.Retry(maxAttempts, Duration.ofMillis(1)));
        return properties;
    }

    private static ProjectionItem scheduled(String showcaseId, MonitorCallback callback) {
        return new ProjectionItem(
                0L,
                ShowcaseScheduledEvent.builder()
                        .showcaseId(showcaseId)
                        .title("Title " + showcaseId)
                        .startTime(Instant.parse("2026-10-24T09:00:00Z"))
                        .duration(Duration.ofMinutes(60))
                        .scheduledAt(Instant.parse("2026-10-23T09:00:00Z"))
                        .build(),
                callback);
    }

    private static ProjectionItem started(MonitorCallback callback) {
        return new ProjectionItem(
                1L,
                ShowcaseStartedEvent.builder()
                        .showcaseId(SHOWCASE_A)
                        .duration(Duration.ofMinutes(60))
                        .startedAt(Instant.parse("2026-10-24T09:00:00Z"))
                        .build(),
                callback);
    }

    private static ProjectionItem finished(MonitorCallback callback) {
        return new ProjectionItem(
                2L,
                ShowcaseFinishedEvent.builder()
                        .showcaseId(SHOWCASE_A)
                        .finishedAt(Instant.parse("2026-10-24T11:00:00Z"))
                        .build(),
                callback);
    }

    private static final class RecordingExecutor implements RoundExecutor {

        private final Deque<ItemOutcome> outcomes = new ArrayDeque<>();

        private final List<List<ProjectionItem>> rounds = new ArrayList<>();

        private RecordingExecutor(ItemOutcome... outcomes) {
            this.outcomes.addAll(List.of(outcomes));
        }

        @Override
        public Mono<List<ItemOutcome>> execute(List<ProjectionItem> events) {
            rounds.add(events);
            val result = new ArrayList<ItemOutcome>();
            for (int i = 0; i < events.size(); i++) {
                val outcome = outcomes.poll();
                if (outcome == null) {
                    return Mono.error(new IllegalStateException("No scripted outcome"));
                }
                result.add(outcome);
            }
            return Mono.just(result);
        }
    }
}
