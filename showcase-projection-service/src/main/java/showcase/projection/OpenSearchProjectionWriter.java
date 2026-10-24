// SPDX-License-Identifier: MIT
package showcase.projection;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.BuiltinScriptLanguage;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.Script;
import org.opensearch.client.opensearch._types.ScriptLanguage;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.data.client.osc.ReactiveOpenSearchTemplate;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.projection.ShowcaseBatchWriter.ItemOutcome;
import showcase.projection.ShowcaseBatchWriter.ProjectionItem;
import showcase.projection.ShowcaseBatchWriter.RoundExecutor;

/**
 * Writes one round of projection events to OpenSearch: maps each event to its bulk operation, executes the request with
 * an immediate refresh, and classifies each item's outcome.
 */
@Component
@Slf4j
class OpenSearchProjectionWriter implements RoundExecutor {
    /**
     * The inline script merging a partial document only when the incoming aggregate sequence is newer.
     */
    private static final String UPDATE_GUARD_SCRIPT = """
            if (ctx._source.sequence == null || params.sequence > ctx._source.sequence) {
              ctx._source.putAll(params.doc);
              ctx._source.sequence = params.sequence;
            }
            """;

    /**
     * The reactive OpenSearch client.
     */
    private final ReactiveOpenSearchTemplate openSearchTemplate;

    /**
     * The converter mapping entities to their OpenSearch representation.
     */
    private final ElasticsearchConverter elasticsearchConverter;

    /**
     * The coordinates of the showcase index.
     */
    private final IndexCoordinates showcaseIndex;

    /**
     * The projector settings supplying the call-level retry budget.
     */
    private final ShowcaseProjectorProperties properties;

    /**
     * Creates the writer for the given collaborators.
     *
     * @param openSearchTemplate the reactive OpenSearch client
     * @param elasticsearchConverter the converter mapping entities to their OpenSearch representation
     * @param properties the projector settings supplying the call-level retry budget
     */
    OpenSearchProjectionWriter(
            ReactiveOpenSearchTemplate openSearchTemplate,
            ElasticsearchConverter elasticsearchConverter,
            ShowcaseProjectorProperties properties) {
        this.openSearchTemplate = openSearchTemplate;
        this.elasticsearchConverter = elasticsearchConverter;
        this.showcaseIndex = openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
        this.properties = properties;
    }

    /**
     * Executes the given events against OpenSearch, retrying a transient call failure.
     *
     * @param events the round's events, one per aggregate
     * @return one classified outcome per event
     */
    @Override
    public Mono<List<ItemOutcome>> execute(List<ProjectionItem> events) {
        val operations = events.stream().map(this::toBulkOperation).toList();
        val request = BulkRequest.of(builder -> builder.operations(operations).refresh(Refresh.True));
        return Mono.defer(() -> Mono.from(openSearchTemplate.execute(client -> client.bulk(request))))
                .retryWhen(Retry.backoff(
                                properties.getRetry().getMaxAttempts(),
                                properties.getRetry().getMinBackoff())
                        .filter(TransientDataAccessException.class::isInstance)
                        .onRetryExhaustedThrow((__, signal) -> signal.failure()))
                .map(response -> classify(operations, response));
    }

    /**
     * Classifies each item of the bulk response.
     *
     * @param operations the executed operations, in order
     * @param response the bulk response
     * @return one outcome per operation
     */
    private static List<ItemOutcome> classify(List<BulkOperation> operations, BulkResponse response) {
        val items = response.items();
        if (items.size() != operations.size()) {
            throw new IllegalStateException(
                    "Expected %d bulk items, received %d".formatted(operations.size(), items.size()));
        }
        return IntStream.range(0, items.size())
                .mapToObj(i -> classify(operations.get(i), items.get(i)))
                .toList();
    }

    /**
     * Classifies one bulk item by its HTTP status and the operation it applied.
     *
     * @param operation the executed operation
     * @param item the response item
     * @return the item's outcome
     */
    private static ItemOutcome classify(BulkOperation operation, BulkResponseItem item) {
        val status = item.status();
        if (status >= 200 && status < 300) {
            return ItemOutcome.APPLIED;
        }
        if (status == 429 || status == 503) {
            return ItemOutcome.RETRYABLE;
        }
        if (status == 409 && operation.isCreate()) {
            return ItemOutcome.CONVERGED;
        }
        if (status == 404 && operation.isDelete() && item.error() == null) {
            return ItemOutcome.CONVERGED;
        }
        log.error(
                "Bulk item failed: [{}] [{}] {}: {}",
                item.operationType(),
                item.id(),
                item.error() != null ? item.error().type() : status,
                item.error() != null ? Objects.toString(item.error().reason(), "") : "");
        return ItemOutcome.TERMINAL;
    }

    /**
     * Maps an event to its bulk operation, carrying the aggregate sequence into creates and updates.
     *
     * @param event the event to map
     * @return the bulk operation applying the event to the showcase index
     */
    private BulkOperation toBulkOperation(ProjectionItem event) {
        return switch (event.payload()) {
            case ShowcaseScheduledEvent scheduledEvent ->
                BulkOperation.of(operation -> operation.create(request -> request.id(scheduledEvent.showcaseId())
                        .document(elasticsearchConverter.mapObject(ShowcaseEntity.builder()
                                .showcaseId(scheduledEvent.showcaseId())
                                .title(scheduledEvent.title())
                                .startTime(scheduledEvent.startTime())
                                .duration(scheduledEvent.duration())
                                .status(ShowcaseStatus.SCHEDULED)
                                .scheduledAt(scheduledEvent.scheduledAt())
                                .sequence(event.sequence())
                                .build()))
                        .index(showcaseIndex.getIndexName())
                        .routing(scheduledEvent.showcaseId())));
            case ShowcaseStartedEvent startedEvent ->
                guardUpdate(
                        startedEvent.showcaseId(),
                        event.sequence(),
                        ShowcaseEntity.builder()
                                .duration(startedEvent.duration())
                                .status(ShowcaseStatus.STARTED)
                                .startedAt(startedEvent.startedAt())
                                .build());
            case ShowcaseFinishedEvent finishedEvent ->
                guardUpdate(
                        finishedEvent.showcaseId(),
                        event.sequence(),
                        ShowcaseEntity.builder()
                                .status(ShowcaseStatus.FINISHED)
                                .finishedAt(finishedEvent.finishedAt())
                                .build());
            case ShowcaseRemovedEvent removedEvent ->
                BulkOperation.of(operation -> operation.delete(request -> request.id(removedEvent.showcaseId())
                        .index(showcaseIndex.getIndexName())
                        .routing(removedEvent.showcaseId())));
        };
    }

    /**
     * Builds a guarded partial update for the given showcase.
     *
     * @param showcaseId the showcase identifier
     * @param sequence the aggregate sequence of the event
     * @param document the partial document carrying the event's fields
     * @return the guarded update operation
     */
    private BulkOperation guardUpdate(String showcaseId, long sequence, ShowcaseEntity document) {
        val mapped = elasticsearchConverter.mapObject(document);
        return BulkOperation.of(operation -> operation.update(request -> request.id(showcaseId)
                .index(showcaseIndex.getIndexName())
                .routing(showcaseId)
                .script(guardScript(sequence, mapped))));
    }

    /**
     * Builds the script merging a partial document only when the incoming sequence is greater.
     *
     * @param sequence the aggregate sequence of the event
     * @param document the mapped partial document
     * @return the inline guard script
     */
    private static Script guardScript(long sequence, Object document) {
        return Script.of(script -> script.inline(
                inline -> inline.lang(ScriptLanguage.of(language -> language.builtin(BuiltinScriptLanguage.Painless)))
                        .source(UPDATE_GUARD_SCRIPT)
                        .params(Map.of("sequence", JsonData.of(sequence), "doc", JsonData.of(document)))));
    }
}
