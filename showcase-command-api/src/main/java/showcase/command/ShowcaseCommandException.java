package showcase.command;

import org.axonframework.commandhandling.CommandExecutionException;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class ShowcaseCommandException extends CommandExecutionException {

    public ShowcaseCommandException(ShowcaseCommandErrorDetails errorDetails) {
        super(errorDetails.errorMessage(), null, errorDetails);
    }

    public ShowcaseCommandException(ShowcaseCommandErrorDetails errorDetails, @Nullable Throwable cause) {
        super(errorDetails.errorMessage(), cause, errorDetails);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ShowcaseCommandErrorDetails> getDetails() {
        return super.getDetails();
    }

    public ShowcaseCommandErrorDetails getErrorDetails() {
        return getDetails().orElseThrow();
    }
}
