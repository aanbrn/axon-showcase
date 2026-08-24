// SPDX-License-Identifier: MIT
package showcase.query;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROTOBUF_VALUE;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcase;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.primitives.Ints;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.SerializedMetaData;
import org.axonframework.serialization.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import reactor.blockhound.BlockHound;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = NONE)
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "showcase.query.api-url", registerSpringBean = true))
@DisplayName("Showcase query client integration tests")
class ShowcaseQueryClientIT {

    @SpringBootApplication
    static class TestApp {}

    @Autowired
    private ShowcaseQueryOperations showcaseQueryOperations;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    @Qualifier("messageSerializer")
    private Serializer messageSerializer;

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @Test
    @DisplayName("Fetching the list with an OK response succeeds")
    void fetchList_okResponse_succeeds() throws Exception {
        val query = FetchShowcaseListQuery.builder().build();
        val showcases = showcases();

        wireMockServer.stubFor(post("/streaming-query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(okJson(objectMapper.writeValueAsString(showcases))));

        showcaseQueryOperations
                .fetchList(query)
                .as(StepVerifier::create)
                .expectNextSequence(showcases)
                .verifyComplete();

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/streaming-query")));
    }

    @Test
    @DisplayName("Fetching the list propagates metadata from the reactive context into the request")
    void fetchList_propagatesMetadataFromContext() throws Exception {
        val query = FetchShowcaseListQuery.builder().build();
        val showcases = showcases();

        wireMockServer.stubFor(post("/streaming-query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(okJson(objectMapper.writeValueAsString(showcases))));

        showcaseQueryOperations
                .fetchList(query)
                .contextWrite(ctx -> ctx.put(MetaData.class, MetaData.with("trace-id", "trace-1")))
                .as(StepVerifier::create)
                .expectNextSequence(showcases)
                .verifyComplete();

        val request = wireMockServer.getAllServeEvents().stream()
                .filter(event -> event.getRequest().getUrl().equals("/streaming-query"))
                .findFirst()
                .orElseThrow()
                .getRequest()
                .getBody();
        val queryRequest = QueryRequest.parseFrom(request);
        val metaData = messageSerializer.<byte[], MetaData>deserialize(
                new SerializedMetaData<>(queryRequest.getSerializedMetaData().toByteArray(), byte[].class));
        assertThat(metaData).containsEntry("trace-id", "trace-1");
    }

    @Test
    @DisplayName("Fetching by ID with an OK response succeeds")
    void fetchById_okResponse_succeeds() throws Exception {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery.builder()
                .showcaseId(showcase.showcaseId())
                .build();

        wireMockServer.stubFor(post("/query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(okJson(objectMapper.writeValueAsString(showcase))));

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .expectNext(showcase)
                .verifyComplete();

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/query")));
    }

    @Test
    @DisplayName("Fetching by ID with a not-found response fails with a not-found error")
    void fetchById_notFoundResponse_failsWithNotFoundError() throws Exception {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();

        wireMockServer.stubFor(post("/query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(aResponse()
                        .withStatus(HTTP_NOT_FOUND)
                        .withHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(
                                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "No showcase with given ID")))));

        showcaseQueryOperations.fetchById(query).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isExactlyInstanceOf(ShowcaseQueryException.class)
                .asInstanceOf(type(ShowcaseQueryException.class))
                .extracting(ShowcaseQueryException::getErrorDetails)
                .asInstanceOf(type(ShowcaseQueryErrorDetails.class))
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseQueryErrorCode.NOT_FOUND);
                    assertThat(errorDetails.errorMessage()).isEqualTo("No showcase with given ID");
                    assertThat(errorDetails.metaData()).isEmpty();
                }));

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/query")));
    }

    @Test
    @DisplayName("Fetching by ID with a bad-request response carrying field errors fails with an invalid-query error")
    void fetchById_badRequestWithFieldErrors_failsWithInvalidQueryError() throws Exception {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Given query is not valid");
        problem.setProperty("fieldErrors", Map.of("showcaseId", List.of("must be a valid KSUID")));

        wireMockServer.stubFor(post("/query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(problem))));

        showcaseQueryOperations.fetchById(query).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
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
    @DisplayName("Fetching by ID with a bad-request response without field errors fails with a response exception")
    void fetchById_badRequestWithoutFieldErrors_failsWithResponseException() throws Exception {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Given query is not valid");

        wireMockServer.stubFor(post("/query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(problem))));

        showcaseQueryOperations.fetchById(query).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isInstanceOf(WebClientResponseException.class));
    }

    @Test
    @DisplayName("Fetching by ID with a not-found response without a detail fails with a response exception")
    void fetchById_notFoundWithoutDetail_failsWithResponseException() throws Exception {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        val problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        wireMockServer.stubFor(post("/query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(problem))));

        showcaseQueryOperations.fetchById(query).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isInstanceOf(WebClientResponseException.class));
    }

    @Test
    @DisplayName("Fetching by ID with a problem-detail error of an unmapped status fails with a response exception")
    void fetchById_problemJsonUnmappedStatus_failsWithResponseException() throws Exception {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "forbidden");

        wireMockServer.stubFor(post("/query")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.FORBIDDEN.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(problem))));

        showcaseQueryOperations.fetchById(query).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isInstanceOf(WebClientResponseException.class));
    }

    @Nested
    @ActiveProfiles("timelimiter")
    @DisplayName("Time limiter")
    class TimeLimiterBehavior {

        @Autowired
        private WireMockServer wireMockServer;

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired
        private TimeLimiterRegistry timeLimiterRegistry;

        private Duration timeout;

        @BeforeEach
        void setUp() {
            timeout = timeLimiterRegistry
                    .timeLimiter(SHOWCASE_QUERY_SERVICE)
                    .getTimeLimiterConfig()
                    .getTimeoutDuration()
                    .plusSeconds(1);
        }

        @Test
        @DisplayName("Fetching the list with a long delay fails with a timeout error")
        void fetchList_longDelay_failsWithTimeoutError() {
            val query = FetchShowcaseListQuery.builder().build();

            wireMockServer.stubFor(post("/streaming-query")
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                    .willReturn(ok().withFixedDelay(Ints.checkedCast(timeout.toMillis()))));

            showcaseQueryOperations.fetchList(query).as(StepVerifier::create).verifyTimeout(timeout);
        }

        @Test
        @DisplayName("Fetching by ID with a long delay fails with a timeout error")
        void fetchById_longDelay_failsWithTimeoutError() {
            val query =
                    FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();

            wireMockServer.stubFor(post("/query")
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                    .willReturn(ok().withFixedDelay(Ints.checkedCast(timeout.toMillis()))));

            showcaseQueryOperations.fetchById(query).as(StepVerifier::create).verifyTimeout(timeout);
        }
    }

    @Nested
    @ActiveProfiles("retry")
    @DisplayName("Retry")
    class RetryBehavior {

        @Autowired
        private WireMockServer wireMockServer;

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired
        private RetryRegistry retryRegistry;

        private int maxAttempts;

        private Duration timeout;

        static List<Arguments> retryableStatusCodes() {
            return List.of(
                    argumentSet("Request Timeout", 408),
                    argumentSet("Too Early", 425),
                    argumentSet("Too Many Requests", 429),
                    argumentSet("Internal Server Error", 500),
                    argumentSet("Bad Gateway", 502),
                    argumentSet("Service Unavailable", 503),
                    argumentSet("Gateway Timeout", 504),
                    argumentSet("Timeout Occurred", 524));
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
        @MethodSource("retryableStatusCodes")
        @DisplayName("Fetching the list with a retryable status code retries and fails with that status code")
        void fetchList_retryableStatusCode_retriesAndFailsWithStatusCode(int statusCode) {
            val query = FetchShowcaseListQuery.builder().build();

            wireMockServer.stubFor(post("/streaming-query")
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                    .willReturn(aResponse().withStatus(statusCode)));

            StepVerifier.withVirtualTime(() -> showcaseQueryOperations.fetchList(query))
                    .thenAwait(timeout)
                    .verifyErrorSatisfies(t -> assertThat(t)
                            .isInstanceOf(WebClientResponseException.class)
                            .asInstanceOf(type(WebClientResponseException.class))
                            .extracting(WebClientResponseException::getStatusCode)
                            .asInstanceOf(type(HttpStatusCode.class))
                            .extracting(HttpStatusCode::value)
                            .isEqualTo(statusCode));

            wireMockServer.verify(maxAttempts, postRequestedFor(urlEqualTo("/streaming-query")));
        }

        @ParameterizedTest
        @MethodSource("retryableStatusCodes")
        @DisplayName("Fetching by ID with a retryable status code retries and fails with that status code")
        void fetchById_retryableStatusCode_retriesAndFailsWithStatusCode(int statusCode) {
            val query =
                    FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();

            wireMockServer.stubFor(post("/query")
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                    .willReturn(aResponse().withStatus(statusCode)));

            StepVerifier.withVirtualTime(() -> showcaseQueryOperations.fetchById(query))
                    .thenAwait(timeout)
                    .verifyErrorSatisfies(t -> assertThat(t)
                            .isInstanceOf(WebClientResponseException.class)
                            .asInstanceOf(type(WebClientResponseException.class))
                            .extracting(WebClientResponseException::getStatusCode)
                            .asInstanceOf(type(HttpStatusCode.class))
                            .extracting(HttpStatusCode::value)
                            .isEqualTo(statusCode));

            wireMockServer.verify(maxAttempts, postRequestedFor(urlEqualTo("/query")));
        }
    }

    @Nested
    @ActiveProfiles("circuitbreaker")
    @DisplayName("Circuit breaker")
    class CircuitBreakerBehavior {

        @Autowired
        private WireMockServer wireMockServer;

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired
        private CircuitBreakerRegistry circuitBreakerRegistry;

        @Test
        @DisplayName("Fetching the list opens the circuit after repeated failures and then fails fast")
        void fetchList_repeatedFailures_openCircuitAndFailFast() {
            val query = FetchShowcaseListQuery.builder().build();

            wireMockServer.stubFor(post("/streaming-query")
                    .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                    .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

            val circuitBreaker = circuitBreakerRegistry.circuitBreaker(SHOWCASE_QUERY_SERVICE);
            val minimumNumberOfCalls = circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls();

            for (int i = 0; i < minimumNumberOfCalls; i++) {
                showcaseQueryOperations
                        .fetchList(query)
                        .as(StepVerifier::create)
                        .verifyError();
            }

            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

            showcaseQueryOperations.fetchList(query).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                    .isInstanceOf(CallNotPermittedException.class));
        }
    }
}
