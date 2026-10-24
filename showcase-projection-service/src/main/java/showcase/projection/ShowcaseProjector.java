// SPDX-License-Identifier: MIT
package showcase.projection;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationWords;
import static org.axonframework.micrometer.TagsUtil.PAYLOAD_TYPE_TAGGER_FUNCTION;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.messaging.Message;
import org.axonframework.micrometer.MessageCountingMonitor;
import org.axonframework.micrometer.MessageTimerMonitor;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.monitoring.MessageMonitor.MonitorCallback;
import org.axonframework.monitoring.MultiMessageMonitor;
import org.axonframework.monitoring.NoOpMessageMonitorCallback;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.projection.ShowcaseBatchWriter.ProjectionItem;

/**
 * Consumes showcase events from Kafka and hands each batch to the {@link ShowcaseBatchWriter}.
 *
 * <p>Messages are started and stopped via {@link SmartLifecycle}. Events are processed in batches per partition,
 * acknowledged after the batch's writes resolve, and monitored with Micrometer metrics.
 */
@Component
@Slf4j
class ShowcaseProjector implements SmartLifecycle {
    /**
     * The prefix for the metric names recorded by the projector.
     */
    private static final String METER_NAME_PREFIX = "showcaseProjector";

    /**
     * Message monitor recording the age of a {@link ShowcaseEvent} message when it is ingested for projection.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    @NullUnmarked
    private static class EventAgeMonitor implements MessageMonitor<EventMessage<?>> {
        /**
         * The prefix for the recorded metric names.
         */
        @NonNull
        private final String meterNamePrefix;

        /**
         * The registry to which the metrics are registered.
         */
        @NonNull
        private final MeterRegistry meterRegistry;

        /**
         * Builds the tags applied to the recorded metrics, empty by default.
         */
        @Builder.Default
        private final Function<Message<?>, Iterable<Tag>> tagsBuilder = message -> Tags.empty();

        /**
         * The clock used to measure the event's age, defaults to the system clock.
         */
        @Builder.Default
        private final Clock clock = Clock.SYSTEM;

        /**
         * Records the age of a showcase event at ingest and returns a no-op callback.
         *
         * @param message the ingested event message
         * @return the message monitor callback
         */
        @NullMarked
        @Override
        public MonitorCallback onMessageIngested(EventMessage<?> message) {
            if (message.getPayload() instanceof ShowcaseEvent event) {
                val lag = clock.wallTime()
                        - switch (event) {
                            case ShowcaseScheduledEvent scheduledEvent ->
                                scheduledEvent.scheduledAt().toEpochMilli();
                            case ShowcaseStartedEvent startedEvent ->
                                startedEvent.startedAt().toEpochMilli();
                            case ShowcaseFinishedEvent finishedEvent ->
                                finishedEvent.finishedAt().toEpochMilli();
                            case ShowcaseRemovedEvent removedEvent ->
                                removedEvent.removedAt().toEpochMilli();
                        };
                if (lag >= 0) {
                    Timer.builder(meterNamePrefix + ".eventAge")
                            .distributionStatisticExpiry(Duration.of(10, ChronoUnit.MINUTES))
                            .publishPercentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999)
                            .tags(tagsBuilder.apply(message))
                            .register(meterRegistry)
                            .record(lag, TimeUnit.MILLISECONDS);
                }
            }
            return NoOpMessageMonitorCallback.INSTANCE;
        }
    }

    /**
     * The projection configuration.
     */
    private final ShowcaseProjectorProperties projectionProperties;

    /**
     * The Kafka receiver used to consume event messages.
     */
    private final KafkaReceiver<String, byte[]> kafkaReceiver;

    /**
     * The converter used to deserialize consumed Kafka messages.
     */
    private final KafkaMessageConverter<String, byte[]> kafkaMessageConverter;

    /**
     * The writer draining each batch into OpenSearch.
     */
    private final ShowcaseBatchWriter batchWriter;

    /**
     * The composite monitor tracking message processing metrics.
     */
    private final MessageMonitor<? super EventMessage<?>> messageMonitor;

    /**
     * The distribution tracking the size of processed batches.
     */
    private final DistributionSummary batchSizeDistribution;

    /**
     * The observation registry for tracing the projection flow.
     */
    private final ObservationRegistry observationRegistry;

    /**
     * The current stream subscription, if the projector is running.
     */
    private final AtomicReference<@Nullable Disposable> subscription = new AtomicReference<>();

    /**
     * Creates a projector that subscribes to the showcase events Kafka topic and projects each event into OpenSearch.
     *
     * @param projectionProperties the projector processing settings
     * @param kafkaProperties the Kafka connection settings (consumer properties and default topic)
     * @param kafkaMessageConverter the converter from Kafka records to domain events
     * @param batchWriter the writer draining each batch into OpenSearch
     * @param meterRegistry the registry for projection metrics
     * @param observationRegistry the registry for tracing the projection flow
     */
    ShowcaseProjector(
            ShowcaseProjectorProperties projectionProperties,
            KafkaProperties kafkaProperties,
            KafkaMessageConverter<String, byte[]> kafkaMessageConverter,
            ShowcaseBatchWriter batchWriter,
            MeterRegistry meterRegistry,
            ObservationRegistry observationRegistry) {
        this.projectionProperties = projectionProperties;
        this.kafkaReceiver =
                KafkaReceiver.create(ReceiverOptions.<String, byte[]>create(kafkaProperties.buildConsumerProperties())
                        .withObservation(observationRegistry)
                        .subscription(List.of(kafkaProperties.getDefaultTopic())));
        this.kafkaMessageConverter = kafkaMessageConverter;
        this.batchWriter = batchWriter;
        this.messageMonitor = new MultiMessageMonitor<>(
                MessageTimerMonitor.builder()
                        .meterNamePrefix(METER_NAME_PREFIX)
                        .meterRegistry(meterRegistry)
                        .tagsBuilder(PAYLOAD_TYPE_TAGGER_FUNCTION)
                        .timerCustomization(timerBuilder -> timerBuilder.publishPercentileHistogram(true))
                        .build(),
                MessageCountingMonitor.buildMonitor(METER_NAME_PREFIX, meterRegistry, PAYLOAD_TYPE_TAGGER_FUNCTION),
                EventAgeMonitor.builder()
                        .meterNamePrefix(METER_NAME_PREFIX)
                        .meterRegistry(meterRegistry)
                        .tagsBuilder(PAYLOAD_TYPE_TAGGER_FUNCTION)
                        .build());
        this.batchSizeDistribution = DistributionSummary.builder(METER_NAME_PREFIX + ".batch.size")
                .description("Size of processed batches (in events)")
                .baseUnit("events")
                .distributionStatisticExpiry(Duration.of(10, ChronoUnit.MINUTES))
                .publishPercentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999)
                .minimumExpectedValue(1d)
                .maximumExpectedValue((double) projectionProperties.getBatch().getMaxSize())
                .register(meterRegistry);
        this.observationRegistry = observationRegistry;
    }

    /**
     * Returns whether the projector is currently running.
     *
     * @return {@code true} if the subscription is active
     */
    @Override
    public boolean isRunning() {
        return subscription.get() != null;
    }

    /**
     * Starts the projection stream, consuming and processing events from Kafka.
     */
    @Override
    public void start() {
        this.subscription.updateAndGet(current -> {
            if (current != null) {
                return current;
            }

            log.info("Projector is starting...");

            return Flux.defer(kafkaReceiver::receive)
                    .name("project-showcase")
                    .doOnSubscribe(subscription -> log.info("Projector has started"))
                    .doOnCancel(() -> log.info("Projector has stopped"))
                    .groupBy(
                            record -> record.receiverOffset().topicPartition(),
                            projectionProperties.getMaxConcurrency())
                    .flatMap(
                            records -> Flux.using(
                                    () -> Schedulers.fromExecutorService(
                                            newScheduledThreadPool(
                                                    projectionProperties.getMinConcurrency(),
                                                    Thread.ofVirtual()
                                                            .name("showcase-projector", 0)
                                                            .factory()),
                                            "showcase-projector"),
                                    scheduler -> records.bufferTimeout(
                                                    projectionProperties
                                                            .getBatch()
                                                            .getMaxSize(),
                                                    projectionProperties
                                                            .getBatch()
                                                            .getMaxTime(),
                                                    scheduler)
                                            .onBackpressureBuffer(
                                                    projectionProperties
                                                            .getBatch()
                                                            .getBufferMaxSize(),
                                                    BufferOverflowStrategy.ERROR)
                                            .concatMap(messages -> {
                                                log.trace("Received {} message(s)", messages.size());
                                                return processMessages(messages)
                                                        .then(Flux.fromIterable(messages)
                                                                .map(ReceiverRecord::receiverOffset)
                                                                .doOnNext(ReceiverOffset::acknowledge)
                                                                .then());
                                            }),
                                    Scheduler::dispose),
                            projectionProperties.getMaxConcurrency(),
                            projectionProperties.getBatch().getMaxSize())
                    .tap(Micrometer.observation(observationRegistry))
                    .retryWhen(Retry.fixedDelay(
                                    Long.MAX_VALUE,
                                    projectionProperties.getRestart().getDelay())
                            .doBeforeRetry(signal -> log.warn(
                                    "Projector has failed and will restart in {}...",
                                    formatDurationWords(
                                            projectionProperties
                                                    .getRestart()
                                                    .getDelay()
                                                    .toMillis(),
                                            true,
                                            true),
                                    signal.failure())))
                    .subscribe();
        });
    }

    /**
     * Stops the projection stream and disposes the active subscription.
     */
    @Override
    public void stop() {
        val subscription = this.subscription.getAndSet(null);
        if (subscription == null) {
            return;
        }

        log.info("Projector is stopping...");

        subscription.dispose();
    }

    /**
     * Converts the given Kafka records into projection events and writes them.
     *
     * @param messages the consumed Kafka records
     * @return a {@link Mono} completing once every event has a resolved outcome
     */
    private Mono<Void> processMessages(List<? extends ConsumerRecord<String, byte[]>> messages) {
        return Flux.fromIterable(messages)
                .map(kafkaMessageConverter::readKafkaMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(eventMessage -> Tuples.of(eventMessage, messageMonitor.onMessageIngested(eventMessage)))
                .flatMap(tuple -> Mono.justOrEmpty(toProjectionItem(tuple.getT1(), tuple.getT2())))
                .collectList()
                .filter(events -> {
                    if (events.isEmpty()) {
                        log.trace("No events");
                        return false;
                    } else {
                        batchSizeDistribution.record(events.size());
                        log.trace("Extracted {} event(s)", events.size());
                        return true;
                    }
                })
                .flatMap(batchWriter::write);
    }

    /**
     * Converts a consumed message to a projection event, reporting a message that cannot be projected.
     *
     * @param message the consumed event message
     * @param callback the message monitor callback
     * @return the projection event, or empty when the message is skipped
     */
    private Optional<ProjectionItem> toProjectionItem(EventMessage<?> message, MonitorCallback callback) {
        if (!(message.getPayload() instanceof ShowcaseEvent)) {
            callback.reportIgnored();
            log.warn("Skipped event message with payload type: {}", message.getPayloadType());
            return Optional.empty();
        }
        if (!(message instanceof DomainEventMessage<?> domainMessage)) {
            callback.reportFailure(null);
            log.error("Skipped event message without an aggregate sequence: {}", message.getPayloadType());
            return Optional.empty();
        }
        return Optional.of(
                new ProjectionItem(domainMessage.getSequenceNumber(), (ShowcaseEvent) message.getPayload(), callback));
    }
}
