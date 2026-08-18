package showcase.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

@DisplayName("Showcase query retry filter tests")
class ShowcaseQueryRetryFilterTests {

    private final ShowcaseQueryRetryFilter filter = new ShowcaseQueryRetryFilter();

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

    static List<Arguments> nonRetryableStatusCodes() {
        return List.of(
                argumentSet("Bad Request", 400),
                argumentSet("Forbidden", 403),
                argumentSet("Not Found", 404),
                argumentSet("Not Implemented", 501)
        );
    }

    @ParameterizedTest
    @MethodSource("retryableStatusCodes")
    @DisplayName("Retrying a response exception with a retryable status code is allowed")
    void test_retryableStatusCode_isAllowed(int statusCode) {
        assertThat(filter.test(responseException(statusCode))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonRetryableStatusCodes")
    @DisplayName("Retrying a response exception with a non-retryable status code is not allowed")
    void test_nonRetryableStatusCode_isNotAllowed(int statusCode) {
        assertThat(filter.test(responseException(statusCode))).isFalse();
    }

    @Test
    @DisplayName("Retrying a timeout exception is allowed")
    void test_timeoutException_isAllowed() {
        assertThat(filter.test(new TimeoutException("timeout"))).isTrue();
    }

    @Test
    @DisplayName("Retrying a WebClient request exception is allowed")
    void test_requestException_isAllowed() {
        assertThat(filter.test(requestException())).isTrue();
    }

    @Test
    @DisplayName("Retrying an unrelated exception is not allowed")
    void test_unrelatedException_isNotAllowed() {
        assertThat(filter.test(new IllegalArgumentException("boom"))).isFalse();
    }

    private static WebClientResponseException responseException(int statusCode) {
        return WebClientResponseException.create(
                statusCode,
                "Status " + statusCode,
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8);
    }

    private static WebClientRequestException requestException() {
        return new WebClientRequestException(
                new IllegalStateException("boom"),
                HttpMethod.GET,
                URI.create("http://localhost/streaming-query"),
                HttpHeaders.EMPTY);
    }
}
