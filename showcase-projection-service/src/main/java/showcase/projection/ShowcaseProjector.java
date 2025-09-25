package showcase.projection;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.common.Registration;
import org.axonframework.common.io.IOUtils;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.kafka.eventhandling.consumer.subscribable.SubscribableKafkaMessageSource;
import org.axonframework.lifecycle.Lifecycle;
import org.axonframework.lifecycle.Phase;
import org.axonframework.micrometer.GlobalMetricRegistry;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.monitoring.MessageMonitor.MonitorCallback;
import org.axonframework.tracing.Span;
import org.axonframework.tracing.SpanFactory;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.axonframework.micrometer.TagsUtil.PAYLOAD_TYPE_TAG;

@Component
@Slf4j
class ShowcaseProjector implements Lifecycle {

    @Builder
    private record ProjectionUnit(

            @NonNull
            ShowcaseEvent event,

            @NonNull
            MonitorCallback monitorCallback,

            @NonNull
            Span span
    ) {
    }

    private final SubscribableKafkaMessageSource<?, ?> messageSource;

    private final ElasticsearchConverter elasticsearchConverter;

    private final IndexCoordinates showcaseIndex;

    private final MessageMonitor<? super EventMessage<?>> messageMonitor;

    private final SpanFactory spanFactory;

    private final Function<BulkRequest, BulkResponse> executeBatchFunc;

    private final AtomicReference<Registration> eventSubscriptionRef = new AtomicReference<>();

    ShowcaseProjector(
            @NonNull SubscribableKafkaMessageSource<?, ?> messageSource,
            @NonNull OpenSearchTemplate openSearchTemplate,
            @NonNull ElasticsearchConverter elasticsearchConverter,
            @NonNull GlobalMetricRegistry metricRegistry,
            @NonNull SpanFactory spanFactory,
            @NonNull ObjectProvider<CircuitBreakerRegistry> circuitBreakerRegistryProvider) {
        this.messageSource = messageSource;
        this.elasticsearchConverter = elasticsearchConverter;
        this.showcaseIndex = openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
        this.messageMonitor =
                metricRegistry.registerEventProcessor(
                        "showcaseProjector",
                        message -> Tags.of(PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName()),
                        message -> Tags.empty());
        this.spanFactory = spanFactory;

        Function<BulkRequest, BulkResponse> executeBatchFunc =
                request -> openSearchTemplate.execute(client -> client.bulk(request));
        this.executeBatchFunc =
                Optional.ofNullable(circuitBreakerRegistryProvider.getIfAvailable())
                        .map(circuitBreakerRegistry -> circuitBreakerRegistry.circuitBreaker("opensearch"))
                        .map(circuitBreaker -> CircuitBreaker.decorateFunction(circuitBreaker, executeBatchFunc))
                        .orElse(executeBatchFunc);
    }

    @Override
    public void registerLifecycleHandlers(@Nonnull LifecycleRegistry lifecycle) {
        lifecycle.onStart(Phase.LOCAL_MESSAGE_HANDLER_REGISTRATIONS, this::start);
        lifecycle.onShutdown(Phase.LOCAL_MESSAGE_HANDLER_REGISTRATIONS, this::shutdown);
    }

    @SuppressWarnings("resource")
    private void start() {
        eventSubscriptionRef.updateAndGet(current -> {
            if (current != null) {
                return current;
            }

            log.info("Projector is starting...");

            val eventSubscription = messageSource.subscribe(this::process);

            log.info("Projector has started");

            return eventSubscription;
        });
    }

    private void shutdown() {
        val eventSubscription = eventSubscriptionRef.getAndSet(null);
        if (eventSubscription == null) {
            return;
        }

        log.info("Projector is shutting down...");

        IOUtils.closeQuietly(eventSubscription);

        log.info("Projector has shut down");
    }

    private void process(List<? extends EventMessage<?>> eventMessages) {
        val units = new LinkedHashMap<String, ProjectionUnit>(eventMessages.size());
        for (val eventMessage : eventMessages) {
            val monitorCallback = messageMonitor.onMessageIngested(eventMessage);
            if (eventMessage.getPayload() instanceof ShowcaseEvent event) {
                val span = spanFactory.createChildHandlerSpan(() -> "ShowcaseProjector.project", eventMessage)
                                      .start();
                units.put(event.getShowcaseId(),
                          ProjectionUnit
                                  .builder()
                                  .event(event)
                                  .monitorCallback(monitorCallback)
                                  .span(span)
                                  .build());
            } else {
                log.warn("Skipping event message with payload type: {}", eventMessage.getPayloadType());
                monitorCallback.reportIgnored();
            }
        }
        if (units.isEmpty()) {
            return;
        }

        project(units);
    }

    private void project(Map<String, ProjectionUnit> units) {
        val operations = new ArrayList<BulkOperation>(units.size());
        for (val unit : units.values()) {
            operations.add(switch (unit.event()) {
                case ShowcaseScheduledEvent scheduledEvent -> BulkOperation.of(operation -> operation.create(
                        request -> request.id(scheduledEvent.getShowcaseId())
                                          .document(elasticsearchConverter.mapObject(
                                                  ShowcaseEntity
                                                          .builder()
                                                          .showcaseId(scheduledEvent.getShowcaseId())
                                                          .title(scheduledEvent.getTitle())
                                                          .startTime(scheduledEvent.getStartTime())
                                                          .duration(scheduledEvent.getDuration())
                                                          .status(ShowcaseStatus.SCHEDULED)
                                                          .scheduledAt(scheduledEvent.getScheduledAt())
                                                          .build()))
                                          .index(showcaseIndex.getIndexName())
                                          .routing(scheduledEvent.getShowcaseId())));
                case ShowcaseStartedEvent startedEvent -> BulkOperation.of(operation -> operation.update(
                        request -> request.id(startedEvent.getShowcaseId())
                                          .document(elasticsearchConverter.mapObject(
                                                  ShowcaseEntity
                                                          .builder()
                                                          .duration(startedEvent.getDuration())
                                                          .status(ShowcaseStatus.STARTED)
                                                          .startedAt(startedEvent.getStartedAt())
                                                          .build()))
                                          .index(showcaseIndex.getIndexName())
                                          .routing(startedEvent.getShowcaseId())));
                case ShowcaseFinishedEvent finishedEvent -> BulkOperation.of(operation -> operation.update(
                        request -> request.id(finishedEvent.getShowcaseId())
                                          .document(elasticsearchConverter.mapObject(
                                                  ShowcaseEntity
                                                          .builder()
                                                          .status(ShowcaseStatus.FINISHED)
                                                          .finishedAt(finishedEvent.getFinishedAt())
                                                          .build()))
                                          .index(showcaseIndex.getIndexName())
                                          .routing(finishedEvent.getShowcaseId())));
                case ShowcaseRemovedEvent removedEvent -> BulkOperation.of(operation -> operation.delete(
                        request -> request.id(removedEvent.getShowcaseId())
                                          .index(showcaseIndex.getIndexName())
                                          .routing(removedEvent.getShowcaseId())));
            });
        }

        val response = executeBatchFunc.apply(BulkRequest.of(request -> request.operations(operations)));
        for (val item : response.items()) {
            val unit = units.get(item.id());
            try (val ignored = unit.span().makeCurrent()) {
                if (item.error() == null) {
                    unit.monitorCallback().reportSuccess();

                    if (Result.NotFound.jsonValue().equals(item.result())) {
                        log.warn("On {}(showcaseId={}), [{}] [{}] [{}]: document missing",
                                 unit.event().getClass().getSimpleName(),
                                 unit.event().getShowcaseId(),
                                 item.operationType(),
                                 Result.NotFound.jsonValue(),
                                 item.id());
                    }
                } else {
                    val errorDescriptionBuilder = new StringBuilder();
                    errorDescriptionBuilder.append("[");
                    errorDescriptionBuilder.append(item.operationType());
                    errorDescriptionBuilder.append("] [");
                    errorDescriptionBuilder.append(item.error().type());
                    errorDescriptionBuilder.append("]");
                    if (item.error().reason() != null) {
                        errorDescriptionBuilder.append(" ");
                        errorDescriptionBuilder.append(item.error().reason());
                    }
                    val errorDescription = errorDescriptionBuilder.toString();
                    val exception = new Exception(errorDescription);
                    unit.monitorCallback().reportFailure(exception);
                    unit.span().recordException(exception);

                    log.error("On {}(showcaseId={}), {}",
                              unit.event().getClass().getSimpleName(),
                              unit.event().getShowcaseId(),
                              errorDescription);
                }
            } finally {
                unit.span().end();
            }
        }
    }
}
