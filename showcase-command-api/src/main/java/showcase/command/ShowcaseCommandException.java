package showcase.command;

import org.axonframework.commandhandling.CommandExecutionException;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Exception thrown when a showcase command cannot be executed.
 */
public final class ShowcaseCommandException extends CommandExecutionException {
    /**
     * Creates an exception with the given error details.
     *
     * @param errorDetails the details describing the failure
     */
    public ShowcaseCommandException(ShowcaseCommandErrorDetails errorDetails) {
        super(errorDetails.errorMessage(), null, errorDetails);
    }

    /**
     * Creates an exception with the given error details and cause.
     *
     * @param errorDetails the details describing the failure
     * @param cause        the underlying cause, if any
     */
    public ShowcaseCommandException(ShowcaseCommandErrorDetails errorDetails, @Nullable Throwable cause) {
        super(errorDetails.errorMessage(), cause, errorDetails);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ShowcaseCommandErrorDetails> getDetails() {
        return super.getDetails();
    }

    /**
     * Returns the error details carried by this exception.
     *
     * @return the error details
     */
    public ShowcaseCommandErrorDetails getErrorDetails() {
        return getDetails().orElseThrow();
    }
}
