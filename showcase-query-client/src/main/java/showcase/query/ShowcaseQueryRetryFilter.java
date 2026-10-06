// SPDX-License-Identifier: MIT
package showcase.query;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Decides which exceptions should trigger a retry on the query service.
 */
final class ShowcaseQueryRetryFilter implements Predicate<Throwable> {
    /**
     * Returns {@code true} when the exception is retryable, matching retryable gRPC statuses and timeouts.
     *
     * @param t the exception to examine
     * @return {@code true} if the exception should trigger a retry
     */
    @Override
    public boolean test(Throwable t) {
        if (t instanceof StatusRuntimeException e) {
            return isRetryable(e.getStatus().getCode());
        }
        if (t instanceof StatusException e) {
            return isRetryable(e.getStatus().getCode());
        }
        return t instanceof TimeoutException;
    }

    /**
     * Returns whether a gRPC status code is retryable.
     *
     * @param code the status code
     * @return true when the code is retryable
     */
    private boolean isRetryable(Status.Code code) {
        return switch (code) {
            case UNAVAILABLE, DEADLINE_EXCEEDED, RESOURCE_EXHAUSTED, ABORTED -> true;
            default -> false;
        };
    }
}
