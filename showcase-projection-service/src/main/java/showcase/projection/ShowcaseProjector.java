package showcase.projection;

import io.micrometer.core.instrument.Tags;
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
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.data.client.osc.OpenSearchTemplate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.axonframework.micrometer.TagsUtil.PAYLOAD_TYPE_TAG;

@Component
@Slf4j
class ShowcaseProjector implements Lifecycle {

    private final SubscribableKafkaMessageSource<?, ?> messageSource;

    private final MessageMonitor<? super EventMessage<?>> messageMonitor;

    private final OpenSearchTemplate openSearchTemplate;

    private final ElasticsearchConverter elasticsearchConverter;

    private final IndexCoordinates showcaseIndex;

    private final AtomicReference<Registration> eventSubscriptionRef = new AtomicReference<>();

    ShowcaseProjector(
            @NonNull SubscribableKafkaMessageSource<?, ?> messageSource,
            @NonNull OpenSearchTemplate openSearchTemplate,
            @NonNull ElasticsearchConverter elasticsearchConverter,
            @NonNull GlobalMetricRegistry metricRegistry) {
        this.messageSource = messageSource;
        this.openSearchTemplate = openSearchTemplate;
        this.elasticsearchConverter = elasticsearchConverter;
        this.showcaseIndex = openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
        this.messageMonitor =
                metricRegistry.registerEventProcessor(
                        "showcaseProjector",
                        message -> Tags.of(PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName()),
                        message -> Tags.empty());
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
        val events = new ArrayList<ShowcaseEvent>(eventMessages.size());
        val monitorCallbacks = new HashMap<String, MonitorCallback>(eventMessages.size());
        for (val eventMessage : eventMessages) {
            val monitorCallback = messageMonitor.onMessageIngested(eventMessage);
            if (eventMessage.getPayload() instanceof ShowcaseEvent event) {
                events.add(event);
                monitorCallbacks.put(event.getShowcaseId(), monitorCallback);
            } else {
                log.warn("Skipping event message with payload type: {}", eventMessage.getPayloadType());
                monitorCallback.reportIgnored();
            }
        }
        if (events.isEmpty()) {
            return;
        }

        project(events, monitorCallbacks);
    }

    private void project(List<ShowcaseEvent> events, Map<String, MonitorCallback> monitorCallbacks) {
        val operations = new ArrayList<BulkOperation>(events.size());
        for (val event : events) {
            operations.add(switch (event) {
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

        val response = openSearchTemplate.execute(client -> client.bulk(request -> request.operations(operations)));
        for (val item : response.items()) {
            val monitorCallback = monitorCallbacks.get(item.id());
            if (item.error() == null) {
                monitorCallback.reportSuccess();

                if (Result.NotFound.jsonValue().equals(item.result())) {
                    log.warn("[{}] [{}] [{}]: document missing", item.operationType(), Result.NotFound.jsonValue(),
                             item.id());
                }
            } else {
                monitorCallback.reportFailure(null);

                log.error("[{}] [{}] {}", item.operationType(), item.error().type(), item.error().reason());
            }
        }
    }
}
