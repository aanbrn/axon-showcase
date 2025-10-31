package showcase.projection;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.ObservationRegistry;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.data.client.osc.ReactiveOpenSearchTemplate;
import org.springframework.context.SmartLifecycle;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationWords;
import static org.axonframework.micrometer.TagsUtil.PAYLOAD_TYPE_TAGGER_FUNCTION;

@Component
@Slf4j
class ShowcaseProjector implements SmartLifecycle {

    private static final String METER_NAME_PREFIX = "showcaseProjector";

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    @NullUnmarked
    private static class ProjectionLagMonitor implements MessageMonitor<EventMessage<?>> {

        @NonNull
        private final String meterNamePrefix;

        @NonNull
        private final MeterRegistry meterRegistry;

        @Builder.Default
        private final Function<Message<?>, Iterable<Tag>> tagsBuilder = message -> Tags.empty();

        @Builder.Default
        private final Clock clock = Clock.SYSTEM;

        @NullMarked
        @Override
        public MonitorCallback onMessageIngested(EventMessage<?> message) {
            if (message.getPayload() instanceof ShowcaseEvent event) {
                val lag = clock.wallTime() - switch (event) {
                    case ShowcaseScheduledEvent scheduledEvent -> scheduledEvent.scheduledAt().toEpochMilli();
                    case ShowcaseStartedEvent startedEvent -> startedEvent.startedAt().toEpochMilli();
                    case ShowcaseFinishedEvent finishedEvent -> finishedEvent.finishedAt().toEpochMilli();
                    case ShowcaseRemovedEvent removedEvent -> removedEvent.removedAt().toEpochMilli();
                };
                if (lag >= 0) {
                    Timer.builder(meterNamePrefix + ".projectionLag")
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

    private final ShowcaseProjectorProperties projectionProperties;

    private final KafkaReceiver<String, byte[]> kafkaReceiver;

    private final KafkaMessageConverter<String, byte[]> kafkaMessageConverter;

    private final ReactiveOpenSearchTemplate openSearchTemplate;

    private final ElasticsearchConverter elasticsearchConverter;

    private final IndexCoordinates showcaseIndex;

    private final MessageMonitor<? super EventMessage<?>> messageMonitor;

    private final DistributionSummary batchSizeDistribution;

    private final ObservationRegistry observationRegistry;

    private final AtomicReference<@Nullable Disposable> subscription = new AtomicReference<>();

    ShowcaseProjector(
            ShowcaseProjectorProperties projectionProperties,
            KafkaProperties kafkaProperties,
            KafkaMessageConverter<String, byte[]> kafkaMessageConverter,
            ReactiveOpenSearchTemplate openSearchTemplate,
            ElasticsearchConverter elasticsearchConverter,
            MeterRegistry meterRegistry,
            ObservationRegistry observationRegistry) {
        this.projectionProperties = projectionProperties;
        this.kafkaReceiver =
                KafkaReceiver.create(
                        ReceiverOptions.<String, byte[]>create(kafkaProperties.buildConsumerProperties())
                                       .withObservation(observationRegistry)
                                       .subscription(List.of(kafkaProperties.getDefaultTopic())));
        this.kafkaMessageConverter = kafkaMessageConverter;
        this.openSearchTemplate = openSearchTemplate;
        this.elasticsearchConverter = elasticsearchConverter;
        this.showcaseIndex = openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
        this.messageMonitor = new MultiMessageMonitor<>(
                MessageTimerMonitor
                        .builder()
                        .meterNamePrefix(METER_NAME_PREFIX)
                        .meterRegistry(meterRegistry)
                        .tagsBuilder(PAYLOAD_TYPE_TAGGER_FUNCTION)
                        .timerCustomization(timerBuilder -> timerBuilder.publishPercentileHistogram(true))
                        .build(),
                MessageCountingMonitor.buildMonitor(
                        METER_NAME_PREFIX,
                        meterRegistry,
                        PAYLOAD_TYPE_TAGGER_FUNCTION),
                ProjectionLagMonitor
                        .builder()
                        .meterNamePrefix(METER_NAME_PREFIX)
                        .meterRegistry(meterRegistry)
                        .tagsBuilder(PAYLOAD_TYPE_TAGGER_FUNCTION)
                        .build());
        this.batchSizeDistribution =
                DistributionSummary
                        .builder(METER_NAME_PREFIX + ".batch.size")
                        .description("Size of processed batches (in events)")
                        .baseUnit("events")
                        .distributionStatisticExpiry(Duration.of(10, ChronoUnit.MINUTES))
                        .publishPercentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999)
                        .minimumExpectedValue(Integer.valueOf(1).doubleValue())
                        .maximumExpectedValue(Integer.valueOf(100).doubleValue())
                        .register(meterRegistry);
        this.observationRegistry = observationRegistry;
    }

    @Override
    public boolean isRunning() {
        return subscription.get() != null;
    }

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
                       .groupBy(record -> record.receiverOffset().topicPartition(),
                                projectionProperties.getMaxConcurrency())
                       .flatMap(records -> Flux.using(
                                        () -> Schedulers.fromExecutorService(
                                                newScheduledThreadPool(
                                                        projectionProperties.getMinConcurrency(),
                                                        Thread.ofVirtual()
                                                              .name("showcase-projector", 0)
                                                              .factory()),
                                                "showcase-projector"),
                                        scheduler -> records.bufferTimeout(
                                                                    projectionProperties.getBatch().getMaxSize(),
                                                                    projectionProperties.getBatch().getMaxTime(),
                                                                    scheduler)
                                                            .onBackpressureBuffer(
                                                                    projectionProperties.getBatch().getBufferMaxSize(),
                                                                    BufferOverflowStrategy.ERROR)
                                                            .concatMap(messages -> {
                                                                log.trace("Received {} message(s)", messages.size());
                                                                return processMessages(messages).then(
                                                                        Flux.fromIterable(messages)
                                                                            .map(ReceiverRecord::receiverOffset)
                                                                            .doOnNext(ReceiverOffset::acknowledge)
                                                                            .then());
                                                            }),
                                        Scheduler::dispose),
                                projectionProperties.getMaxConcurrency(),
                                projectionProperties.getBatch().getMaxSize())
                       .tap(Micrometer.observation(observationRegistry))
                       .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, projectionProperties.getRestart().getDelay())
                                       .doBeforeRetry(signal -> log.warn(
                                               "Projector has failed and will restart in {}...",
                                               formatDurationWords(
                                                       projectionProperties.getRestart().getDelay().toMillis(),
                                                       true, true),
                                               signal.failure())))
                       .subscribe();
        });
    }

    @Override
    public void stop() {
        val subscription = this.subscription.getAndSet(null);
        if (subscription == null) {
            return;
        }

        log.info("Projector is stopping...");

        subscription.dispose();
    }

    private Mono<Void> processMessages(List<? extends ConsumerRecord<String, byte[]>> messages) {
        return Flux.fromIterable(messages)
                   .map(kafkaMessageConverter::readKafkaMessage)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collectList()
                   .filter(eventMessages -> {
                       if (eventMessages.isEmpty()) {
                           log.trace("No event messages");
                           return false;
                       } else {
                           log.trace("Extracted {} event message(s)", eventMessages.size());
                           return true;
                       }
                   })
                   .flatMapIterable(Function.identity())
                   .map(eventMessage -> Tuples.of(eventMessage, messageMonitor.onMessageIngested(eventMessage)))
                   .filter(TupleUtils.predicate((eventMessage, monitorCallback) -> {
                       if (eventMessage.getPayload() instanceof ShowcaseEvent) {
                           return true;
                       } else {
                           monitorCallback.reportIgnored();
                           log.warn("Skipped event message with payload type: {}", eventMessage.getPayloadType());
                           return false;
                       }
                   }))
                   .map(it -> it.mapT1(EventMessage::getPayload))
                   .map(it -> it.mapT1(ShowcaseEvent.class::cast))
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
                   .flatMap(this::processEvents);
    }

    private Mono<Void> processEvents(List<Tuple2<ShowcaseEvent, MonitorCallback>> events) {
        return Flux.zip(Flux.fromIterable(events)
                            .map(Tuple2::getT1),
                        Flux.fromIterable(events)
                            .map(Tuple2::getT2),
                        Flux.fromIterable(events)
                            .map(Tuple2::getT1)
                            .map(this::eventToBulkOperation)
                            .collectList()
                            .map(operations -> BulkRequest.of(request -> request.operations(operations)))
                            .flatMap(this::execute)
                            .map(BulkResponse::items)
                            .flatMapIterable(Function.identity()))
                   .doOnNext(TupleUtils.consumer((event, monitorCallback, responseItem) -> {
                       if (responseItem.error() == null) {
                           monitorCallback.reportSuccess();

                           if (Result.NotFound.jsonValue().equals(responseItem.result())) {
                               log.warn("On {}, [{}] [{}] [{}]: document missing",
                                        event.getClass().getSimpleName(),
                                        responseItem.operationType(),
                                        Result.NotFound.jsonValue(),
                                        responseItem.id());
                           } else {
                               log.trace("On {}, [{}]: succeeded",
                                         event.getClass().getSimpleName(),
                                         event.showcaseId());
                           }
                       } else {
                           monitorCallback.reportFailure(null);

                           log.error("On {}, [{}] [{}] {}",
                                     event.getClass().getSimpleName(),
                                     responseItem.operationType(),
                                     responseItem.error().type(),
                                     Objects.toString(responseItem.error().reason(), ""));
                       }
                   }))
                   .then();
    }

    private BulkOperation eventToBulkOperation(ShowcaseEvent event) {
        return switch (event) {
            case ShowcaseScheduledEvent scheduledEvent -> BulkOperation.of(operation -> operation.create(
                    request -> request.id(scheduledEvent.showcaseId())
                                      .document(elasticsearchConverter.mapObject(
                                              ShowcaseEntity
                                                      .builder()
                                                      .showcaseId(scheduledEvent.showcaseId())
                                                      .title(scheduledEvent.title())
                                                      .startTime(scheduledEvent.startTime())
                                                      .duration(scheduledEvent.duration())
                                                      .status(ShowcaseStatus.SCHEDULED)
                                                      .scheduledAt(scheduledEvent.scheduledAt())
                                                      .build()))
                                      .index(showcaseIndex.getIndexName())
                                      .routing(scheduledEvent.showcaseId())));
            case ShowcaseStartedEvent startedEvent -> BulkOperation.of(operation -> operation.update(
                    request -> request.id(startedEvent.showcaseId())
                                      .document(elasticsearchConverter.mapObject(
                                              ShowcaseEntity
                                                      .builder()
                                                      .duration(startedEvent.duration())
                                                      .status(ShowcaseStatus.STARTED)
                                                      .startedAt(startedEvent.startedAt())
                                                      .build()))
                                      .index(showcaseIndex.getIndexName())
                                      .routing(startedEvent.showcaseId())));
            case ShowcaseFinishedEvent finishedEvent -> BulkOperation.of(operation -> operation.update(
                    request -> request.id(finishedEvent.showcaseId())
                                      .document(elasticsearchConverter.mapObject(
                                              ShowcaseEntity
                                                      .builder()
                                                      .status(ShowcaseStatus.FINISHED)
                                                      .finishedAt(finishedEvent.finishedAt())
                                                      .build()))
                                      .index(showcaseIndex.getIndexName())
                                      .routing(finishedEvent.showcaseId())));
            case ShowcaseRemovedEvent removedEvent -> BulkOperation.of(operation -> operation.delete(
                    request -> request.id(removedEvent.showcaseId())
                                      .index(showcaseIndex.getIndexName())
                                      .routing(removedEvent.showcaseId())));
        };
    }

    private Mono<BulkResponse> execute(BulkRequest request) {
        return Mono.from(openSearchTemplate.execute(client -> client.bulk(request)))
                   .retryWhen(Retry.backoff(projectionProperties.getRetry().getMaxAttempts(),
                                            projectionProperties.getRetry().getMinBackoff())
                                   .filter(TransientDataAccessException.class::isInstance)
                                   .onRetryExhaustedThrow((__, signal) -> signal.failure()));
    }
}
