// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.val;
import org.apache.kafka.common.TopicPartition;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;
import showcase.command.ShowcaseScheduledEvent;

@DisplayName("Showcase projector tests")
class ShowcaseProjectorTests {

    private final ShowcaseProjectorProperties properties = new ShowcaseProjectorProperties();

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final ShowcaseBatchWriter batchWriter = mock(ShowcaseBatchWriter.class);

    @SuppressWarnings("unchecked")
    private final KafkaMessageConverter<String, byte[]> converter = mock(KafkaMessageConverter.class);

    private final ShowcaseProjectorStateMonitor stateMonitor = new ShowcaseProjectorStateMonitor(meterRegistry);

    private ShowcaseProjector projector;

    @BeforeEach
    void setUp() {
        properties.setMinConcurrency(1);
        properties.setMaxConcurrency(1);
        properties.setBatch(new ShowcaseProjectorProperties.Batch(2, Duration.ofMillis(50), 4));
        val kafkaProperties = new KafkaProperties();
        kafkaProperties.setDefaultTopic("axon-showcase-events");
        val message = new GenericDomainEventMessage<>(
                "Showcase",
                "showcase-a",
                0L,
                ShowcaseScheduledEvent.builder()
                        .showcaseId("showcase-a")
                        .title("Title")
                        .startTime(java.time.Instant.parse("2026-10-24T09:00:00Z"))
                        .duration(Duration.ofMinutes(60))
                        .scheduledAt(java.time.Instant.parse("2026-10-23T09:00:00Z"))
                        .build());
        when(converter.readKafkaMessage(any())).thenReturn(Optional.of(message));
        projector = new ShowcaseProjector(
                properties,
                kafkaProperties,
                converter,
                batchWriter,
                meterRegistry,
                ObservationRegistry.NOOP,
                stateMonitor);
    }

    @Test
    @DisplayName("A burst beyond the buffer overflows in events, not whole batches")
    void project_burstBeyondTheBuffer_overflowsInEvents() {
        val emitted = new AtomicInteger();
        when(batchWriter.write(anyList()))
                .thenReturn(Mono.delay(Duration.ofMillis(300)).then());

        StepVerifier.create(projector.projectPartition(push(100, emitted)))
                .expectError(IllegalStateException.class)
                .verify(Duration.ofSeconds(5));

        assertThat(emitted.get())
                .isEqualTo(properties.getBatch().getBufferMaxSize()
                        + 2 * properties.getBatch().getMaxSize()
                        + 1);
        assertThat(meterRegistry.get("showcaseProjector.processing").gauge().value())
                .isZero();
    }

    @Test
    @DisplayName("A resolved batch stamps the progress and clears the in-flight count")
    void project_resolvedBatch_stampsProgress() {
        when(batchWriter.write(anyList())).thenReturn(Mono.empty());
        val emitted = new AtomicInteger();

        StepVerifier.create(projector.projectPartition(push(1, emitted))).verifyComplete();

        assertThat(stateMonitor.lastProgressEpochSecond()).isPositive();
        assertThat(meterRegistry.get("showcaseProjector.processing").gauge().value())
                .isZero();
    }

    @Test
    @DisplayName("A failed batch clears the in-flight count without stamping the progress")
    void project_failedBatch_clearsInFlightWithoutProgress() {
        when(batchWriter.write(anyList())).thenReturn(Mono.error(new IllegalStateException("write failed")));
        val emitted = new AtomicInteger();

        StepVerifier.create(projector.projectPartition(push(1, emitted)))
                .expectError(IllegalStateException.class)
                .verify(Duration.ofSeconds(5));

        assertThat(stateMonitor.lastProgressEpochSecond()).isZero();
        assertThat(meterRegistry.get("showcaseProjector.processing").gauge().value())
                .isZero();
    }

    @Test
    @DisplayName("A demand-respecting source below the capacity completes without overflowing")
    void project_demandRespectingSource_completes() {
        when(batchWriter.write(anyList())).thenReturn(Mono.empty());
        val source = Flux.range(0, 5).map(__ -> record());

        StepVerifier.create(projector.projectPartition(source)).verifyComplete();

        assertThat(stateMonitor.lastProgressEpochSecond()).isPositive();
    }

    @Test
    @DisplayName("A burst below the buffer completes without overflowing")
    void project_burstBelowTheBuffer_completes() {
        when(batchWriter.write(anyList())).thenReturn(Mono.empty());
        val emitted = new AtomicInteger();

        StepVerifier.create(projector.projectPartition(push(3, emitted))).verifyComplete();

        assertThat(emitted.get()).isEqualTo(3);
        assertThat(stateMonitor.lastProgressEpochSecond()).isPositive();
    }

    @Test
    @DisplayName("A partial batch flushes when its window expires")
    void project_partialBatch_flushesOnTheTimer() throws InterruptedException {
        val flushed = new CountDownLatch(1);
        when(batchWriter.write(anyList())).thenAnswer(invocation -> {
            flushed.countDown();
            return Mono.empty();
        });
        val emitted = new AtomicInteger();
        val source = Flux.concat(push(1, emitted), Mono.never());

        val subscription = projector.projectPartition(source).subscribe();

        try {
            assertThat(flushed.await(5, TimeUnit.SECONDS))
                    .as("the window flushes the partial batch while the source is still open")
                    .isTrue();
        } finally {
            subscription.dispose();
        }
    }

    private Flux<ReceiverRecord<String, byte[]>> push(int count, AtomicInteger emitted) {
        val cancelled = new AtomicBoolean();
        return Flux.from(subscriber -> {
            subscriber.onSubscribe(new org.reactivestreams.Subscription() {
                @Override
                public void request(long requested) {}

                @Override
                public void cancel() {
                    cancelled.set(true);
                }
            });
            for (var index = 0; index < count && !cancelled.get(); index++) {
                emitted.incrementAndGet();
                subscriber.onNext(record());
            }
            if (!cancelled.get()) {
                subscriber.onComplete();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ReceiverRecord<String, byte[]> record() {
        val offset = mock(ReceiverOffset.class);
        when(offset.topicPartition()).thenReturn(new TopicPartition("axon-showcase-events", 0));
        val record = mock(ReceiverRecord.class);
        when(record.receiverOffset()).thenReturn(offset);
        return record;
    }
}
