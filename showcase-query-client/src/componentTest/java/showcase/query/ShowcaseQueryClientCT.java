package showcase.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.primitives.Ints;
import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import reactor.blockhound.BlockHound;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

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

@SpringBootTest(webEnvironment = NONE)
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "showcase.query.api-url", registerSpringBean = true))
@DirtiesContext
class ShowcaseQueryClientCT {

    @SpringBootApplication
    static class TestApp {
    }

    @Autowired
    private ShowcaseQueryOperations showcaseQueryOperations;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WireMockServer wireMockServer;

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @Test
    void fetchList_okResponse_succeeds() throws Exception {
        val query = FetchShowcaseListQuery.builder().build();
        val showcases = showcases();

        wireMockServer.stubFor(
                post("/streaming-query")
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
    void fetchById_okResponse_succeeds() throws Exception {
        val showcase = aShowcase();
        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(showcase.getShowcaseId())
                        .build();

        wireMockServer.stubFor(
                post("/query")
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
    void fetchById_notFoundResponse_failsWithNotFoundError() throws Exception {
        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(aShowcaseId())
                        .build();

        wireMockServer.stubFor(
                post("/query")
                        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                        .willReturn(aResponse()
                                            .withStatus(HTTP_NOT_FOUND)
                                            .withHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
                                            .withBody(objectMapper.writeValueAsString(
                                                    ProblemDetail.forStatusAndDetail(
                                                            HttpStatus.NOT_FOUND,
                                                            "No showcase with given ID")))));

        showcaseQueryOperations
                .fetchById(query)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
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
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }));

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

        @Autowired
        private TimeLimiterRegistry timeLimiterRegistry;

        private Duration timeout;

        @BeforeEach
        void setUp() {
            timeout =
                    timeLimiterRegistry
                            .timeLimiter(SHOWCASE_QUERY_SERVICE)
                            .getTimeLimiterConfig()
                            .getTimeoutDuration()
                            .plusSeconds(1);
        }

        @Test
        void fetchList_longDelay_failsWithTimeoutError() {
            val query = FetchShowcaseListQuery.builder().build();

            wireMockServer.stubFor(
                    post("/streaming-query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .willReturn(ok().withFixedDelay(Ints.checkedCast(timeout.toMillis()))));

            showcaseQueryOperations
                    .fetchList(query)
                    .as(StepVerifier::create)
                    .verifyTimeout(timeout);
        }

        @Test
        void fetchById_longDelay_failsWithTimeoutError() {
            val query =
                    FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build();

            wireMockServer.stubFor(
                    post("/query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .willReturn(ok().withFixedDelay(Ints.checkedCast(timeout.toMillis()))));

            showcaseQueryOperations
                    .fetchById(query)
                    .as(StepVerifier::create)
                    .verifyTimeout(timeout);
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
                    argumentSet("Timeout Occurred", 524)
            );
        }

        @BeforeEach
        void setUp() {
            val retryConfig = retryRegistry.retry(SHOWCASE_QUERY_SERVICE).getRetryConfig();

            maxAttempts = retryConfig.getMaxAttempts();
            timeout = IntStream.rangeClosed(1, maxAttempts)
                               .mapToLong(i -> retryConfig.getIntervalBiFunction()
                                                          .apply(i, Either.left(null)))
                               .mapToObj(Duration::ofMillis)
                               .reduce(Duration.ZERO, Duration::plus);
        }

        @ParameterizedTest
        @MethodSource("retryableStatusCodes")
        void fetchList_retryableStatusCode_retriesAndFailsWithStatusCode(int statusCode) {
            val query = FetchShowcaseListQuery.builder().build();

            wireMockServer.stubFor(
                    post("/streaming-query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .willReturn(aResponse().withStatus(statusCode)));

            showcaseQueryOperations
                    .fetchList(query)
                    .as(it -> StepVerifier.withVirtualTime(() -> it))
                    .thenAwait(timeout)
                    .verifyErrorSatisfies(
                            t -> assertThat(t)
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
        void fetchById_retryableStatusCode_retriesAndFailsWithStatusCode(int statusCode) {
            val query =
                    FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build();

            wireMockServer.stubFor(
                    post("/query")
                            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_PROTOBUF_VALUE))
                            .willReturn(aResponse().withStatus(statusCode)));

            showcaseQueryOperations
                    .fetchById(query)
                    .as(it -> StepVerifier.withVirtualTime(() -> it))
                    .thenAwait(timeout)
                    .verifyErrorSatisfies(
                            t -> assertThat(t)
                                         .isInstanceOf(WebClientResponseException.class)
                                         .asInstanceOf(type(WebClientResponseException.class))
                                         .extracting(WebClientResponseException::getStatusCode)
                                         .asInstanceOf(type(HttpStatusCode.class))
                                         .extracting(HttpStatusCode::value)
                                         .isEqualTo(statusCode));

            wireMockServer.verify(maxAttempts, postRequestedFor(urlEqualTo("/query")));
        }
    }
}
