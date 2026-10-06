// SPDX-License-Identifier: MIT
package showcase.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import io.grpc.Status;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Showcase query retry filter tests")
class ShowcaseQueryRetryFilterTests {

    private final ShowcaseQueryRetryFilter filter = new ShowcaseQueryRetryFilter();

    static List<Arguments> retryableStatuses() {
        return List.of(
                argumentSet("Unavailable", Status.UNAVAILABLE),
                argumentSet("Deadline exceeded", Status.DEADLINE_EXCEEDED),
                argumentSet("Resource exhausted", Status.RESOURCE_EXHAUSTED),
                argumentSet("Aborted", Status.ABORTED));
    }

    static List<Arguments> nonRetryableStatuses() {
        return List.of(
                argumentSet("Invalid argument", Status.INVALID_ARGUMENT),
                argumentSet("Not found", Status.NOT_FOUND),
                argumentSet("Permission denied", Status.PERMISSION_DENIED),
                argumentSet("Internal", Status.INTERNAL));
    }

    @ParameterizedTest
    @MethodSource("retryableStatuses")
    @DisplayName("Retrying a runtime status exception with a retryable status is allowed")
    void test_retryableStatusRuntimeException_isAllowed(Status status) {
        assertThat(filter.test(status.asRuntimeException())).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonRetryableStatuses")
    @DisplayName("Retrying a runtime status exception with a non-retryable status is not allowed")
    void test_nonRetryableStatusRuntimeException_isNotAllowed(Status status) {
        assertThat(filter.test(status.asRuntimeException())).isFalse();
    }

    @Test
    @DisplayName("Retrying a status exception with a retryable status is allowed")
    void test_retryableStatusException_isAllowed() {
        assertThat(filter.test(Status.UNAVAILABLE.asException())).isTrue();
    }

    @Test
    @DisplayName("Retrying a status exception with a non-retryable status is not allowed")
    void test_nonRetryableStatusException_isNotAllowed() {
        assertThat(filter.test(Status.INVALID_ARGUMENT.asException())).isFalse();
    }

    @Test
    @DisplayName("Retrying a timeout exception is allowed")
    void test_timeoutException_isAllowed() {
        assertThat(filter.test(new TimeoutException("timeout"))).isTrue();
    }

    @Test
    @DisplayName("Retrying an unrelated exception is not allowed")
    void test_unrelatedException_isNotAllowed() {
        assertThat(filter.test(new IllegalArgumentException("boom"))).isFalse();
    }
}
