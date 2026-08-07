package showcase.query;

import org.axonframework.queryhandling.QueryExecutionException;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Exception thrown when a showcase query cannot be executed.
 */
public final class ShowcaseQueryException extends QueryExecutionException {
    /**
     * Creates an exception with the given error details.
     *
     * @param errorDetails the details describing the failure
     */
    public ShowcaseQueryException(ShowcaseQueryErrorDetails errorDetails) {
        super(errorDetails.errorMessage(), null, errorDetails);
    }

    /**
     * Creates an exception with the given error details and cause.
     *
     * @param errorDetails the details describing the failure
     * @param cause        the underlying cause, if any
     */
    public ShowcaseQueryException(ShowcaseQueryErrorDetails errorDetails, @Nullable Throwable cause) {
        super(errorDetails.errorMessage(), cause, errorDetails);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ShowcaseQueryErrorDetails> getDetails() {
        return super.getDetails();
    }

    /**
     * Returns the error details carried by this exception.
     *
     * @return the error details
     */
    public ShowcaseQueryErrorDetails getErrorDetails() {
        return getDetails().orElseThrow();
    }
}
