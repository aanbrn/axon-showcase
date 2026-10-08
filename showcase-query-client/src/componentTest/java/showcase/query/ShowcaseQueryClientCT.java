// SPDX-License-Identifier: MIT
package showcase.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcase;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.GenericQueryResponseMessage;
import org.axonframework.serialization.SerializedMetaData;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.blockhound.BlockHound;
import reactor.test.StepVerifier;
import showcase.query.ShowcaseQueryTransportGrpc.ShowcaseQueryTransportImplBase;

@SpringBootTest(webEnvironment = NONE)
@DisplayName("Showcase query client component tests")
class ShowcaseQueryClientCT {

    @SpringBootApplication
    static class TestApp {}

    private static final Metadata.Key<byte[]> FIELD_ERRORS_KEY =
            Metadata.Key.of("field-errors-bin", Metadata.BINARY_BYTE_MARSHALLER);

    private static final FakeQueryService service = new FakeQueryService();

    private static final Server server = startServer();

    @DynamicPropertySource
    static void grpcTarget(DynamicPropertyRegistry registry) {
        registry.add("showcase.query.target", () -> "localhost:" + server.getPort());
    }

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @AfterAll
    static void stopServer() {
        server.shutdownNow();
    }

    @BeforeEach
    void resetService() {
        service.reset();
    }

    @Autowired
    private ShowcaseQueryOperations showcaseQueryOperations;

    @Test
    @DisplayName("Fetching the list with an OK response succeeds")
    void fetchList_okResponse_succeeds() {
        val showcases = showcases();
        service.respondWith(showcases);

        showcaseQueryOperations
                .fetchList(FetchShowcaseListQuery.builder().build())
                .as(StepVerifier::create)
                .expectNextSequence(showcases)
                .verifyComplete();

        assertThat(service.calls()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fetching the list propagates metadata from the reactive context into the request")
    void fetchList_propagatesMetadataFromContext() {
        val showcases = showcases();
        service.respondWith(showcases);

        showcaseQueryOperations
                .fetchList(FetchShowcaseListQuery.builder().build())
                .contextWrite(ctx -> ctx.put(MetaData.class, MetaData.with("trace-id", "trace-1")))
                .as(StepVerifier::create)
                .expectNextSequence(showcases)
                .verifyComplete();

        assertThat(service.lastRequestMetaData()).containsEntry("trace-id", "trace-1");
    }

    @Test
    @DisplayName("Fetching by ID with an OK response succeeds")
    void fetchById_okResponse_succeeds() {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery.builder()
                .showcaseId(showcase.showcaseId())
                .build();
        service.respondWith(showcase);

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .expectNext(showcase)
                .verifyComplete();
    }

    @Test
    @DisplayName("Fetching by ID with a not-found status fails with a not-found error")
    void fetchById_notFoundStatus_failsWithNotFoundError() {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        service.failWith(Status.NOT_FOUND.withDescription("No showcase with given ID"));

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(t -> assertThat(t)
                        .isExactlyInstanceOf(ShowcaseQueryException.class)
                        .asInstanceOf(type(ShowcaseQueryException.class))
                        .extracting(ShowcaseQueryException::getErrorDetails)
                        .asInstanceOf(type(ShowcaseQueryErrorDetails.class))
                        .satisfies(errorDetails -> {
                            assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseQueryErrorCode.NOT_FOUND);
                            assertThat(errorDetails.errorMessage()).isEqualTo("No showcase with given ID");
                            assertThat(errorDetails.metaData()).isEmpty();
                        }));
    }

    @Test
    @DisplayName("Fetching by ID with an invalid-argument status carrying field errors fails with an invalid-query "
            + "error")
    void fetchById_invalidArgumentWithFieldErrors_failsWithInvalidQueryError() {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        service.failWith(Status.INVALID_ARGUMENT.withDescription("Given query is not valid"), fieldErrorsTrailer());

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(t -> assertThat(t)
                        .isExactlyInstanceOf(ShowcaseQueryException.class)
                        .asInstanceOf(type(ShowcaseQueryException.class))
                        .extracting(ShowcaseQueryException::getErrorDetails)
                        .asInstanceOf(type(ShowcaseQueryErrorDetails.class))
                        .satisfies(errorDetails -> {
                            assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseQueryErrorCode.INVALID_QUERY);
                            assertThat(errorDetails.errorMessage()).isEqualTo("Given query is not valid");
                            assertThat(errorDetails.metaData()).containsKey("showcaseId");
                        }));
    }

    @Test
    @DisplayName("Fetching by ID with an invalid-argument status without field errors fails with a status error")
    void fetchById_invalidArgumentWithoutFieldErrors_failsWithStatusError() {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        service.failWith(Status.INVALID_ARGUMENT.withDescription("Given query is not valid"));

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(StatusRuntimeException.class));
    }

    @Test
    @DisplayName("Fetching by ID with a not-found status without a description fails with a status error")
    void fetchById_notFoundWithoutDescription_failsWithStatusError() {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        service.failWith(Status.NOT_FOUND);

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(StatusRuntimeException.class));
    }

    @Test
    @DisplayName("Fetching by ID with a non-business status fails with a status error")
    void fetchById_nonBusinessStatus_failsWithStatusError() {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        service.failWith(Status.PERMISSION_DENIED.withDescription("forbidden"));

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(StatusRuntimeException.class));
    }

    private static Metadata fieldErrorsTrailer() {
        val trailers = new Metadata();
        trailers.put(FIELD_ERRORS_KEY, "{\"showcaseId\":[\"must be a valid KSUID\"]}".getBytes(StandardCharsets.UTF_8));
        return trailers;
    }

    private static Server startServer() {
        try {
            return ServerBuilder.forPort(0).addService(service).build().start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Nested
    @ActiveProfiles("timelimiter")
    @DisplayName("Time limiter")
    class TimeLimiterBehavior {

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired
        private TimeLimiterRegistry timeLimiterRegistry;

        @Test
        @DisplayName("Fetching the list with a long delay fails with a timeout error")
        void fetchList_longDelay_failsWithTimeoutError() {
            val timeout = timeLimiterRegistry
                    .timeLimiter(SHOWCASE_QUERY_SERVICE)
                    .getTimeLimiterConfig()
                    .getTimeoutDuration()
                    .plusSeconds(1);
            service.respondWithAfter(showcases(), timeout);

            showcaseQueryOperations
                    .fetchList(FetchShowcaseListQuery.builder().build())
                    .as(StepVerifier::create)
                    .verifyTimeout(timeout);
        }

        @Test
        @DisplayName("Fetching by ID with a long delay fails with a timeout error")
        void fetchById_longDelay_failsWithTimeoutError() {
            val timeout = timeLimiterRegistry
                    .timeLimiter(SHOWCASE_QUERY_SERVICE)
                    .getTimeLimiterConfig()
                    .getTimeoutDuration()
                    .plusSeconds(1);
            service.respondWithAfter(List.of(aShowcase()), timeout);

            showcaseQueryOperations
                    .fetchById(FetchShowcaseByIdQuery.builder()
                            .showcaseId(aShowcaseId())
                            .build())
                    .as(StepVerifier::create)
                    .verifyTimeout(timeout);
        }
    }

    @Nested
    @ActiveProfiles("retry")
    @DisplayName("Retry")
    class RetryBehavior {

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired
        private RetryRegistry retryRegistry;

        private int maxAttempts;

        private Duration timeout;

        static List<Arguments> retryableStatuses() {
            return List.of(
                    argumentSet("Unavailable", Status.UNAVAILABLE),
                    argumentSet("Deadline exceeded", Status.DEADLINE_EXCEEDED),
                    argumentSet("Resource exhausted", Status.RESOURCE_EXHAUSTED),
                    argumentSet("Aborted", Status.ABORTED));
        }

        @BeforeEach
        void setUp() {
            val retryConfig = retryRegistry.retry(SHOWCASE_QUERY_SERVICE).getRetryConfig();

            maxAttempts = retryConfig.getMaxAttempts();
            timeout = IntStream.rangeClosed(1, maxAttempts)
                    .mapToLong(i -> retryConfig.getIntervalBiFunction().apply(i, Either.left(null)))
                    .mapToObj(Duration::ofMillis)
                    .reduce(Duration.ZERO, Duration::plus);
        }

        @ParameterizedTest
        @MethodSource("retryableStatuses")
        @DisplayName("Fetching the list with a retryable status retries and fails with that status")
        void fetchList_retryableStatus_retriesAndFailsWithStatus(Status status) {
            service.failWith(status);

            StepVerifier.withVirtualTime(() -> showcaseQueryOperations.fetchList(
                            FetchShowcaseListQuery.builder().build()))
                    .thenAwait(timeout)
                    .verifyErrorSatisfies(t -> assertThat(t)
                            .isInstanceOf(StatusRuntimeException.class)
                            .asInstanceOf(type(StatusRuntimeException.class))
                            .extracting(e -> e.getStatus().getCode())
                            .isEqualTo(status.getCode()));

            assertThat(service.calls()).isEqualTo(maxAttempts);
        }

        @ParameterizedTest
        @MethodSource("retryableStatuses")
        @DisplayName("Fetching by ID with a retryable status retries and fails with that status")
        void fetchById_retryableStatus_retriesAndFailsWithStatus(Status status) {
            service.failWith(status);

            StepVerifier.withVirtualTime(() -> showcaseQueryOperations.fetchById(FetchShowcaseByIdQuery.builder()
                            .showcaseId(aShowcaseId())
                            .build()))
                    .thenAwait(timeout)
                    .verifyErrorSatisfies(t -> assertThat(t)
                            .isInstanceOf(StatusRuntimeException.class)
                            .asInstanceOf(type(StatusRuntimeException.class))
                            .extracting(e -> e.getStatus().getCode())
                            .isEqualTo(status.getCode()));

            assertThat(service.calls()).isEqualTo(maxAttempts);
        }
    }

    @Nested
    @ActiveProfiles("circuitbreaker")
    @DisplayName("Circuit breaker")
    class CircuitBreakerBehavior {

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired
        private CircuitBreakerRegistry circuitBreakerRegistry;

        @Test
        @DisplayName("Fetching the list opens the circuit after repeated failures and then fails fast")
        void fetchList_repeatedFailures_openCircuitAndFailFast() {
            service.failWith(Status.INTERNAL);

            val circuitBreaker = circuitBreakerRegistry.circuitBreaker(SHOWCASE_QUERY_SERVICE);
            val minimumNumberOfCalls = circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls();

            for (var i = 0; i < minimumNumberOfCalls; i++) {
                showcaseQueryOperations
                        .fetchList(FetchShowcaseListQuery.builder().build())
                        .as(StepVerifier::create)
                        .verifyError();
            }

            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

            showcaseQueryOperations
                    .fetchList(FetchShowcaseListQuery.builder().build())
                    .as(StepVerifier::create)
                    .verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(CallNotPermittedException.class));
        }
    }

    /**
     * Serves canned responses or a status error for the query transport, recording the requests it receives.
     */
    static final class FakeQueryService extends ShowcaseQueryTransportImplBase {

        private final Serializer serializer = JacksonSerializer.defaultSerializer();

        private final QueryMessageMapper mapper = new QueryMessageMapper(serializer);

        private final AtomicInteger calls = new AtomicInteger();

        private volatile List<QueryResponse> responses = List.of();

        private volatile StatusRuntimeException error;

        private volatile Duration delay = Duration.ZERO;

        private volatile MetaData lastRequestMetaData;

        void respondWith(Showcase... showcases) {
            respondWith(List.of(showcases));
        }

        void respondWith(List<Showcase> showcases) {
            responses = showcases.stream()
                    .map(showcase -> mapper.messageToResponse(new GenericQueryResponseMessage<>(showcase)))
                    .toList();
        }

        void respondWithAfter(List<Showcase> showcases, Duration delay) {
            respondWith(showcases);
            this.delay = delay;
        }

        void failWith(Status status) {
            error = status.asRuntimeException();
        }

        void failWith(Status status, Metadata trailers) {
            error = status.asRuntimeException(trailers);
        }

        void reset() {
            responses = List.of();
            error = null;
            delay = Duration.ZERO;
            lastRequestMetaData = null;
            calls.set(0);
        }

        MetaData lastRequestMetaData() {
            return lastRequestMetaData;
        }

        int calls() {
            return calls.get();
        }

        @Override
        public void dispatch(QueryRequest request, StreamObserver<QueryResponse> responseObserver) {
            calls.incrementAndGet();
            lastRequestMetaData = serializer.deserialize(
                    new SerializedMetaData<>(request.getSerializedMetaData().toByteArray(), byte[].class));
            val delayMillis = delay.toMillis();
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            val currentError = error;
            if (currentError != null) {
                responseObserver.onError(currentError);
                return;
            }
            responses.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        }
    }
}
