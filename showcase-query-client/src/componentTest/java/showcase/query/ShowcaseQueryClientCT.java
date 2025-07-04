package showcase.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import com.google.common.primitives.Ints;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.axonframework.serialization.Serializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_PROTOBUF_VALUE;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcase;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

@SpringBootTest(webEnvironment = NONE)
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "showcase.query.api-url", registerSpringBean = true))
@DirtiesContext
class ShowcaseQueryClientCT {

    @SpringBootApplication
    static class TestApp {
    }

    @RequiredArgsConstructor
    private class RequestBodyQueryMatcher implements ValueMatcher<Request> {

        @NonNull
        private final Object query;

        @Override
        public MatchResult match(Request request) {
            try {
                val queryRequest = QueryRequest.parseFrom(request.getBody());
                val queryMessage = queryMessageRequestMapper.requestToMessage(queryRequest);
                return MatchResult.of(query.equals(queryMessage.getPayload()));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Autowired
    private ShowcaseQueryOperations showcaseQueryOperations;

    @Autowired
    @Qualifier("messageSerializer")
    private Serializer messageSerializer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WireMockServer wireMockServer;

    private QueryMessageRequestMapper queryMessageRequestMapper;

    @BeforeEach
    void setUp() {
        this.queryMessageRequestMapper = new QueryMessageRequestMapper(this.messageSerializer);
    }

    @Test
    void fetchAll_noFiltering_emitsAllShowcases() throws Exception {
        val showcases = showcases();

        val query = FetchShowcaseListQuery.builder().build();

        wireMockServer.stubFor(
                post("/streaming-query")
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                        .andMatching(new RequestBodyQueryMatcher(query))
                        .willReturn(okJson(objectMapper.writeValueAsString(showcases))));

        StepVerifier
                .create(showcaseQueryOperations.fetchAll(query))
                .expectNextSequence(showcases)
                .expectComplete()
                .verify();

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/streaming-query")));
    }

    @Test
    void fetchAll_titleToFilterBy_emitsAllShowcases() throws Exception {
        val showcase = aShowcase();

        val query =
                FetchShowcaseListQuery
                        .builder()
                        .title(showcase.getTitle())
                        .build();

        wireMockServer.stubFor(
                post("/streaming-query")
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                        .andMatching(new RequestBodyQueryMatcher(query))
                        .willReturn(okJson(objectMapper.writeValueAsString(List.of(showcase)))));

        StepVerifier
                .create(showcaseQueryOperations.fetchAll(query))
                .expectNext(showcase)
                .expectComplete()
                .verify();

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/streaming-query")));
    }

    @Test
    void fetchAll_singleStatusToFilterBy_emitsMatchingShowcases() throws Exception {
        val status = aShowcaseStatus();
        val showcases =
                showcases()
                        .stream()
                        .filter(it -> it.getStatus() == status)
                        .toList();

        val query =
                FetchShowcaseListQuery
                        .builder()
                        .status(status)
                        .build();

        wireMockServer.stubFor(
                post("/streaming-query")
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                        .andMatching(new RequestBodyQueryMatcher(query))
                        .willReturn(okJson(objectMapper.writeValueAsString(showcases))));

        StepVerifier
                .create(showcaseQueryOperations.fetchAll(
                        FetchShowcaseListQuery
                                .builder()
                                .status(status)
                                .build()))
                .expectNextSequence(showcases)
                .expectComplete()
                .verify();

        wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/streaming-query")));
    }

    @Test
    void fetchAll_multipleStatusesToFilterBy_emitsMatchingShowcases() throws Exception {
        val status1 = aShowcaseStatus();
        val status2 = aShowcaseStatus(status1);
        val showcases =
                showcases()
                        .stream()
                        .filter(it -> it.getStatus() == status1 || it.getStatus() == status2)
                        .toList();

        val query =
                FetchShowcaseListQuery
                        .builder()
                        .status(status1)
                        .status(status2)
                        .build();

        wireMockServer.stubFor(
                post("/streaming-query")
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                        .andMatching(new RequestBodyQueryMatcher(query))
                        .willReturn(okJson(objectMapper.writeValueAsString(showcases))));

        StepVerifier
                .create(showcaseQueryOperations.fetchAll(
                        FetchShowcaseListQuery
                                .builder()
                                .status(status1)
                                .status(status2)
                                .build()))
                .expectNextSequence(showcases)
                .expectComplete()
                .verify();

        wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/streaming-query")));
    }

    @Test
    void fetchById_existingShowcase_emitsRequestedShowcase() throws Exception {
        val showcase = aShowcase();

        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(showcase.getShowcaseId())
                        .build();

        wireMockServer.stubFor(
                post("/query")
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                        .andMatching(new RequestBodyQueryMatcher(query))
                        .willReturn(okJson(objectMapper.writeValueAsString(showcase))));

        StepVerifier
                .create(showcaseQueryOperations.fetchById(query))
                .expectNext(showcase)
                .expectComplete()
                .verify();

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/query")));
    }

    @Test
    void fetchById_nonExistingShowcase_emitsErrorWithShowcaseQueryExceptionCausedByNotFoundError() {
        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(aShowcaseId())
                        .build();

        wireMockServer.stubFor(
                post("/query")
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                        .andMatching(new RequestBodyQueryMatcher(query))
                        .willReturn(notFound()));

        StepVerifier
                .create(showcaseQueryOperations.fetchById(query))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseQueryException.class)
                                     .asInstanceOf(type(ShowcaseQueryException.class))
                                     .extracting(ShowcaseQueryException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseQueryErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseQueryErrorCode.NOT_FOUND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("No showcase with given ID");
                                     }))
                .verify();

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/query")));
    }

    @Nested
    @ActiveProfiles("timelimiter")
    @DirtiesContext
    class TimeLimiter {

        @Autowired
        private WireMockServer wireMockServer;

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired(required = false)
        private TimeLimiterRegistry timeLimiterRegistry;

        @Test
        void fetchAll_timeout_emitsTimeoutError() {
            assumeTrue(timeLimiterRegistry != null, "TimeLimiter is disabled");

            val query = FetchShowcaseListQuery.builder().build();

            wireMockServer.stubFor(
                    post("/streaming-query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .andMatching(new RequestBodyQueryMatcher(query))
                            .willReturn(ok().withFixedDelay(Ints.checkedCast(
                                    timeLimiterRegistry
                                            .timeLimiter(SHOWCASE_QUERY_SERVICE)
                                            .getTimeLimiterConfig()
                                            .getTimeoutDuration()
                                            .multipliedBy(2)
                                            .toMillis()))));

            StepVerifier
                    .create(showcaseQueryOperations.fetchAll(query))
                    .expectError(TimeoutException.class)
                    .verify();
        }

        @Test
        void fetchById_timeout_emitsTimeoutError() {
            assumeTrue(timeLimiterRegistry != null, "TimeLimiter is disabled");

            val query =
                    FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build();

            wireMockServer.stubFor(
                    post("/query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .andMatching(new RequestBodyQueryMatcher(query))
                            .willReturn(ok().withFixedDelay(Ints.checkedCast(
                                    timeLimiterRegistry
                                            .timeLimiter(SHOWCASE_QUERY_SERVICE)
                                            .getTimeLimiterConfig()
                                            .getTimeoutDuration()
                                            .multipliedBy(2)
                                            .toMillis()))));

            StepVerifier
                    .create(showcaseQueryOperations.fetchById(query))
                    .expectError(TimeoutException.class)
                    .verify();
        }
    }

    @Nested
    @ActiveProfiles("retry")
    @DirtiesContext
    class Retry {

        @Autowired
        private WireMockServer wireMockServer;

        @Autowired
        private ShowcaseQueryOperations showcaseQueryOperations;

        @Autowired(required = false)
        private RetryRegistry retryRegistry;

        static List<Arguments> retryableStatusCodes() {
            return List.of(
                    argumentSet("Request Timeout", 408),
                    argumentSet("Too Early", 425),
                    argumentSet("Too Many Requests", 429),
                    argumentSet("Internal Server Error", 500),
                    argumentSet("Bad Gateway", 502),
                    argumentSet("Service Unavailable", 503),
                    argumentSet("Gateway Timeout", 504),
                    argumentSet("Timeout Occurred", 524)
            );
        }

        @ParameterizedTest
        @MethodSource("retryableStatusCodes")
        void fetchAll_retry_retriesAndEmitsError(int statusCode) {
            assumeTrue(retryRegistry != null, "Retry is disabled");

            val query = FetchShowcaseListQuery.builder().build();

            wireMockServer.resetAll();
            wireMockServer.stubFor(
                    post("/streaming-query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .andMatching(new RequestBodyQueryMatcher(query))
                            .willReturn(aResponse().withStatus(statusCode)));

            StepVerifier
                    .create(showcaseQueryOperations.fetchAll(query))
                    .expectErrorSatisfies(
                            t -> assertThat(t)
                                         .isInstanceOf(WebClientResponseException.class)
                                         .asInstanceOf(type(WebClientResponseException.class))
                                         .extracting(WebClientResponseException::getStatusCode)
                                         .asInstanceOf(type(HttpStatusCode.class))
                                         .extracting(HttpStatusCode::value)
                                         .isEqualTo(statusCode))
                    .verify();

            wireMockServer
                    .verify(retryRegistry.retry(SHOWCASE_QUERY_SERVICE).getRetryConfig().getMaxAttempts(),
                            postRequestedFor(urlEqualTo("/streaming-query")));
        }

        @ParameterizedTest
        @MethodSource("retryableStatusCodes")
        void fetchById_retry_retriesAndEmitsError(int statusCode) {
            assumeTrue(retryRegistry != null, "Retry is disabled");

            val query =
                    FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build();

            wireMockServer.resetAll();
            wireMockServer.stubFor(
                    post("/query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .andMatching(new RequestBodyQueryMatcher(query))
                            .willReturn(aResponse().withStatus(statusCode)));

            StepVerifier
                    .create(showcaseQueryOperations.fetchById(query))
                    .expectErrorSatisfies(
                            t -> assertThat(t)
                                         .isInstanceOf(WebClientResponseException.class)
                                         .asInstanceOf(type(WebClientResponseException.class))
                                         .extracting(WebClientResponseException::getStatusCode)
                                         .asInstanceOf(type(HttpStatusCode.class))
                                         .extracting(HttpStatusCode::value)
                                         .isEqualTo(statusCode))
                    .verify();

            wireMockServer
                    .verify(retryRegistry.retry(SHOWCASE_QUERY_SERVICE).getRetryConfig().getMaxAttempts(),
                            postRequestedFor(urlEqualTo("/query")));
        }
    }
}
