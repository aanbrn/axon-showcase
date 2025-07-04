package showcase.command;

import lombok.NonNull;
import lombok.val;
import org.axonframework.commandhandling.CommandExecutionException;

import java.util.Optional;

public final class ShowcaseCommandException extends CommandExecutionException {

    public ShowcaseCommandException(@NonNull ShowcaseCommandErrorDetails errorDetails) {
        super(errorDetails.getErrorMessage(), null, errorDetails);
    }

    public ShowcaseCommandException(@NonNull ShowcaseCommandErrorDetails errorDetails, Throwable cause) {
        super(errorDetails.getErrorMessage(), cause, errorDetails);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ShowcaseCommandErrorDetails> getDetails() {
        return super.getDetails();
    }

    public ShowcaseCommandErrorDetails getErrorDetails() {
        return getDetails().orElseThrow();
    }

    @Override
    public String toString() {
        val errorDetails = getErrorDetails();
        return getClass().getName()
                       + " [errorCode = " + errorDetails.getErrorCode()
                       + ", errorMessage = " + errorDetails.getErrorMessage() + "]";
    }
}
