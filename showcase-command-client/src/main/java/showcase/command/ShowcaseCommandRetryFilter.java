package showcase.command;

import org.axonframework.common.AxonNonTransientException;

import java.util.function.Predicate;

/**
 * Decides which exceptions should trigger a retry on the command service.
 */
final class ShowcaseCommandRetryFilter implements Predicate<Throwable> {
    /**
     * Returns {@code true} when the exception is retryable, i.e. it is neither a {@link ShowcaseCommandException}
     * nor wrapped by an {@link AxonNonTransientException}.
     *
     * @param t the exception to examine
     * @return {@code true} if the exception should trigger a retry
     */
    @Override
    public boolean test(Throwable t) {
        return !(t instanceof ShowcaseCommandException)
                       && !(t instanceof AxonNonTransientException)
                       && !(t.getCause() instanceof AxonNonTransientException);
    }
}
