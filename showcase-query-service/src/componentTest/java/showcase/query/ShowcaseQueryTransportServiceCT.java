// SPDX-License-Identifier: MIT
package showcase.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.GenericQueryResponseMessage;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryBusSpanFactory;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
@DisplayName("Showcase query transport service component tests")
class ShowcaseQueryTransportServiceCT {

    private static final Metadata.Key<byte[]> FIELD_ERRORS_KEY =
            Metadata.Key.of("field-errors-bin", Metadata.BINARY_BYTE_MARSHALLER);

    private final QueryMessageMapper messageMapper = new QueryMessageMapper(JacksonSerializer.defaultSerializer());

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private QueryBus queryBus;

    @Mock
    private QueryBusSpanFactory spanFactory;

    private ShowcaseQueryTransportService service;

    @BeforeEach
    void setUp() {
        lenient()
                .doAnswer(invocation -> invocation.getArgument(0))
                .when(spanFactory)
                .propagateContext(any());
        service = new ShowcaseQueryTransportService(queryBus, messageMapper, spanFactory, objectMapper);
    }

    @Test
    @DisplayName("A valid query streams each response and completes")
    void dispatch_validQuery_streamsEachResponseAndCompletes() {
        val showcase = RandomQueryTestUtils.aShowcase();
        doReturn(Flux.just(new GenericQueryResponseMessage<>(showcase)))
                .when(queryBus)
                .streamingQuery(any());

        val observer = dispatch(FetchShowcaseListQuery.builder().build(), Showcase.class);

        assertThat(observer.completed).isTrue();
        assertThat(observer.error).isNull();
        assertThat(observer.responses).hasSize(1);
        assertThat(messageMapper.payloadFromResponse(observer.responses.getFirst(), Showcase.class))
                .isEqualTo(showcase);
    }

    @Test
    @DisplayName("A request referencing an unknown expected response type fails with INVALID_ARGUMENT")
    void dispatch_unknownResponseType_failsWithInvalidArgument() {
        val request = messageMapper
                .messageToRequest(new GenericStreamingQueryMessage<>(
                        FetchShowcaseListQuery.builder().build(), Showcase.class))
                .toBuilder()
                .setResponseType("showcase.query.DoesNotExist")
                .build();

        val observer = new CapturingObserver();
        service.dispatch(request, observer);

        assertThat(statusOf(observer)).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(descriptionOf(observer)).isEqualTo("Unknown expected response type");
    }

    @Test
    @DisplayName("A by-ID query for a missing showcase fails with NOT_FOUND and its message")
    void dispatch_missingShowcase_failsWithNotFound() {
        doReturn(Flux.error(new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                        .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                        .errorMessage("No showcase with given ID")
                        .build())))
                .when(queryBus)
                .streamingQuery(any());

        val observer =
                dispatch(FetchShowcaseByIdQuery.builder().showcaseId("unknown").build(), Showcase.class);

        assertThat(statusOf(observer)).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(descriptionOf(observer)).isEqualTo("No showcase with given ID");
    }

    @Test
    @DisplayName("An invalid query fails with INVALID_ARGUMENT and a field errors trailer")
    void dispatch_invalidQuery_failsWithInvalidArgumentAndFieldErrorsTrailer() throws Exception {
        val fieldErrors = MetaData.with("showcaseId", List.of("must be a valid KSUID"));
        doReturn(Flux.error(new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                        .errorCode(ShowcaseQueryErrorCode.INVALID_QUERY)
                        .errorMessage("Given query is not valid")
                        .metaData(fieldErrors)
                        .build())))
                .when(queryBus)
                .streamingQuery(any());

        val observer =
                dispatch(FetchShowcaseByIdQuery.builder().showcaseId("unknown").build(), Showcase.class);

        assertThat(statusOf(observer)).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(descriptionOf(observer)).isEqualTo("Given query is not valid");
        val trailers = ((StatusRuntimeException) observer.error).getTrailers();
        assertThat(trailers).isNotNull();
        assertThat(objectMapper.readValue(
                        trailers.get(FIELD_ERRORS_KEY), new TypeReference<Map<String, List<String>>>() {}))
                .isEqualTo(Map.of("showcaseId", List.of("must be a valid KSUID")));
    }

    @Test
    @DisplayName("A data access failure fails with UNAVAILABLE and its message")
    void dispatch_dataAccessFailure_failsWithUnavailable() {
        doReturn(Flux.error(new DataAccessResourceFailureException("database is down")))
                .when(queryBus)
                .streamingQuery(any());

        val observer = dispatch(FetchShowcaseListQuery.builder().build(), Showcase.class);

        assertThat(statusOf(observer)).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(descriptionOf(observer)).isEqualTo("database is down");
    }

    @Test
    @DisplayName("A timeout fails with DEADLINE_EXCEEDED and a fixed message")
    void dispatch_timeout_failsWithDeadlineExceeded() {
        doReturn(Flux.error(new TimeoutException("took too long")))
                .when(queryBus)
                .streamingQuery(any());

        val observer = dispatch(FetchShowcaseListQuery.builder().build(), Showcase.class);

        assertThat(statusOf(observer)).isEqualTo(Status.Code.DEADLINE_EXCEEDED);
        assertThat(descriptionOf(observer)).isEqualTo("Operation timeout exceeded");
    }

    @Test
    @DisplayName("An unknown failure fails with INTERNAL and its message")
    void dispatch_unknownFailure_failsWithInternal() {
        doReturn(Flux.error(new IllegalStateException("boom"))).when(queryBus).streamingQuery(any());

        val observer = dispatch(FetchShowcaseListQuery.builder().build(), Showcase.class);

        assertThat(statusOf(observer)).isEqualTo(Status.Code.INTERNAL);
        assertThat(descriptionOf(observer)).isEqualTo("boom");
    }

    @Test
    @DisplayName("A dispatched query propagates its tracing context")
    void dispatch_validQuery_propagatesTracingContext() {
        doReturn(Flux.empty()).when(queryBus).streamingQuery(any());

        dispatch(FetchShowcaseListQuery.builder().build(), Showcase.class);

        verify(spanFactory).propagateContext(any());
    }

    private <R> CapturingObserver dispatch(Object query, Class<R> responseType) {
        val request = messageMapper.messageToRequest(new GenericStreamingQueryMessage<>(query, responseType));
        val observer = new CapturingObserver();
        service.dispatch(request, observer);
        return observer;
    }

    private static Status.Code statusOf(CapturingObserver observer) {
        return ((StatusRuntimeException) observer.error).getStatus().getCode();
    }

    private static String descriptionOf(CapturingObserver observer) {
        return ((StatusRuntimeException) observer.error).getStatus().getDescription();
    }

    /**
     * Captures the signals a {@link StreamObserver} receives.
     */
    private static final class CapturingObserver implements StreamObserver<QueryResponse> {

        private final List<QueryResponse> responses = new ArrayList<>();

        private Throwable error;

        private boolean completed;

        @Override
        public void onNext(QueryResponse response) {
            responses.add(response);
        }

        @Override
        public void onError(Throwable throwable) {
            error = throwable;
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }
}
