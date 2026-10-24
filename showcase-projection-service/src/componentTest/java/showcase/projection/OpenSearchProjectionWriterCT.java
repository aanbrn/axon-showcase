// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;
import org.axonframework.monitoring.MessageMonitor.MonitorCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.OperationType;
import org.opensearch.data.client.osc.ReactiveOpenSearchClient;
import org.opensearch.data.client.osc.ReactiveOpenSearchTemplate;
import org.opensearch.data.client.osc.ReactiveOpenSearchTemplate.ClientCallback;
import org.reactivestreams.Publisher;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.projection.ShowcaseBatchWriter.ItemOutcome;
import showcase.projection.ShowcaseBatchWriter.ProjectionItem;
import showcase.projection.ShowcaseProjectorProperties.Retry;

@DisplayName("OpenSearch projection writer component tests")
class OpenSearchProjectionWriterCT {

    private final ReactiveOpenSearchTemplate template = mock(ReactiveOpenSearchTemplate.class);

    private final ReactiveOpenSearchClient client = mock(ReactiveOpenSearchClient.class);

    private final AtomicReference<BulkRequest> request = new AtomicReference<>();

    private final ShowcaseProjectorProperties properties = new ShowcaseProjectorProperties();

    private OpenSearchProjectionWriter writer;

    @BeforeEach
    void setUp() {
        properties.setRetry(new Retry(2, Duration.ofMillis(1)));
        when(template.getIndexCoordinatesFor(ShowcaseEntity.class)).thenReturn(IndexCoordinates.of("showcases"));
        writer = new OpenSearchProjectionWriter(template, mappingConverter(), properties);
        doAnswer(invocation -> {
                    ClientCallback<Publisher<BulkResponse>> callback = invocation.getArgument(0);
                    return callback.doWithClient(client);
                })
                .when(template)
                .execute(ArgumentMatchers.<ClientCallback<Publisher<BulkResponse>>>any());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("A round is executed with an immediate refresh and each applied item is classified as applied")
    void execute_refreshesImmediatelyAndAppliesItems() {
        stubBulkResponse(item(OperationType.Create, 201), item(OperationType.Update, 200));

        StepVerifier.create(writer.execute(List.of(scheduled(), started())))
                .expectNext(List.of(ItemOutcome.APPLIED, ItemOutcome.APPLIED))
                .verifyComplete();

        val executed = request.get();
        assertThat(executed.refresh()).isEqualTo(Refresh.True);
        assertThat(executed.operations()).hasSize(2);
        assertThat((Map<String, Object>)
                        executed.operations().getFirst().create().document())
                .containsEntry("sequence", 0L);
        assertThat(executed.operations().get(1).isUpdate()).isTrue();
        assertThat(executed.operations().get(1).update().id()).isEqualTo("showcase-1");
    }

    @Test
    @DisplayName("A load rejection is retryable and a create conflict is convergence")
    void execute_classifiesRetryableAndConverged() {
        stubBulkResponse(item(OperationType.Create, 429), item(OperationType.Create, 409));

        StepVerifier.create(writer.execute(List.of(scheduled(), scheduledOther())))
                .expectNext(List.of(ItemOutcome.RETRYABLE, ItemOutcome.CONVERGED))
                .verifyComplete();
    }

    @Test
    @DisplayName("A terminal status is not retried and an error-bearing delete 404 is terminal")
    void execute_classifiesTerminalStatuses() {
        stubBulkResponse(item(OperationType.Create, 400), item(OperationType.Update, 404));

        StepVerifier.create(writer.execute(List.of(scheduled(), started())))
                .expectNext(List.of(ItemOutcome.TERMINAL, ItemOutcome.TERMINAL))
                .verifyComplete();

        stubBulkResponse(BulkResponseItem.of(item -> item.index("showcases")
                .id("showcase-1")
                .operationType(OperationType.Delete)
                .status(404)
                .error(error -> error.type("index_not_found_exception"))));

        StepVerifier.create(writer.execute(List.of(removed())))
                .expectNext(List.of(ItemOutcome.TERMINAL))
                .verifyComplete();
    }

    @Test
    @DisplayName("A delete of a missing document is convergence, not a failed item")
    void execute_classifiesErrorlessDelete404AsConverged() {
        stubBulkResponse(item(OperationType.Delete, 404));

        StepVerifier.create(writer.execute(List.of(removed())))
                .expectNext(List.of(ItemOutcome.CONVERGED))
                .verifyComplete();
    }

    @Test
    @DisplayName("A removed event is written as an unversioned delete")
    void execute_writesAnUnversionedDelete() {
        stubBulkResponse(item(OperationType.Delete, 200));

        StepVerifier.create(writer.execute(List.of(removed())))
                .expectNext(List.of(ItemOutcome.APPLIED))
                .verifyComplete();

        val operation = request.get().operations().getFirst();
        assertThat(operation.isDelete()).isTrue();
        assertThat(operation.delete().version()).isNull();
    }

    @Test
    @DisplayName("A transient call failure is retried before the response is classified")
    void execute_retriesTransientCallFailures() {
        val attempts = new AtomicInteger();
        doAnswer(invocation -> {
                    if (attempts.incrementAndGet() == 1) {
                        return Mono.error(new TransientDataAccessResourceException("transient"));
                    }
                    ClientCallback<Publisher<BulkResponse>> callback = invocation.getArgument(0);
                    return callback.doWithClient(client);
                })
                .when(template)
                .execute(ArgumentMatchers.<ClientCallback<Publisher<BulkResponse>>>any());
        stubBulkResponse(item(OperationType.Create, 201), item(OperationType.Update, 200));

        StepVerifier.create(writer.execute(List.of(scheduled(), started())))
                .expectNext(List.of(ItemOutcome.APPLIED, ItemOutcome.APPLIED))
                .verifyComplete();

        assertThat(attempts).hasValue(2);
    }

    @Test
    @DisplayName("A non-transient call failure is not retried")
    void execute_doesNotRetryOtherFailures() {
        val attempts = new AtomicInteger();
        doAnswer(invocation -> {
                    attempts.incrementAndGet();
                    return Mono.error(new IllegalStateException("permanent"));
                })
                .when(template)
                .execute(ArgumentMatchers.<ClientCallback<Publisher<BulkResponse>>>any());

        StepVerifier.create(writer.execute(List.of(scheduled(), started()))).verifyError(IllegalStateException.class);

        assertThat(attempts).hasValue(1);
    }

    private void stubBulkResponse(BulkResponseItem... items) {
        doAnswer(invocation -> {
                    request.set(invocation.getArgument(0));
                    return Mono.just(BulkResponse.of(
                            builder -> builder.took(1L).errors(true).items(List.of(items))));
                })
                .when(client)
                .bulk(any(BulkRequest.class));
    }

    private static ProjectionItem scheduled() {
        return new ProjectionItem(
                0L,
                ShowcaseScheduledEvent.builder()
                        .showcaseId("showcase-1")
                        .title("Title")
                        .startTime(Instant.parse("2026-10-24T09:00:00Z"))
                        .duration(Duration.ofMinutes(60))
                        .scheduledAt(Instant.parse("2026-10-23T09:00:00Z"))
                        .build(),
                mock(MonitorCallback.class));
    }

    private static ProjectionItem scheduledOther() {
        return new ProjectionItem(
                0L,
                ShowcaseScheduledEvent.builder()
                        .showcaseId("showcase-2")
                        .title("Other title")
                        .startTime(Instant.parse("2026-10-24T10:00:00Z"))
                        .duration(Duration.ofMinutes(30))
                        .scheduledAt(Instant.parse("2026-10-23T10:00:00Z"))
                        .build(),
                mock(MonitorCallback.class));
    }

    private static ProjectionItem removed() {
        return new ProjectionItem(
                2L,
                ShowcaseRemovedEvent.builder()
                        .showcaseId("showcase-1")
                        .removedAt(Instant.parse("2026-10-24T12:00:00Z"))
                        .build(),
                mock(MonitorCallback.class));
    }

    private static ProjectionItem started() {
        return new ProjectionItem(
                1L,
                ShowcaseStartedEvent.builder()
                        .showcaseId("showcase-1")
                        .duration(Duration.ofMinutes(60))
                        .startedAt(Instant.parse("2026-10-24T09:00:00Z"))
                        .build(),
                mock(MonitorCallback.class));
    }

    private static BulkResponseItem item(OperationType operationType, int status) {
        return BulkResponseItem.of(item -> item.index("showcases")
                .id("showcase-1")
                .operationType(operationType)
                .status(status));
    }

    private static MappingElasticsearchConverter mappingConverter() {
        val converter = new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
        converter.afterPropertiesSet();
        return converter;
    }
}
