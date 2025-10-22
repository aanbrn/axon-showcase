package showcase.query;

import lombok.NonNull;
import org.axonframework.queryhandling.QueryExecutionException;

import java.util.Optional;

public final class ShowcaseQueryException extends QueryExecutionException {

    public ShowcaseQueryException(@NonNull ShowcaseQueryErrorDetails errorDetails) {
        super(errorDetails.errorMessage(), null, errorDetails);
    }

    public ShowcaseQueryException(@NonNull ShowcaseQueryErrorDetails errorDetails, Throwable cause) {
        super(errorDetails.errorMessage(), cause, errorDetails);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ShowcaseQueryErrorDetails> getDetails() {
        return super.getDetails();
    }

    public ShowcaseQueryErrorDetails getErrorDetails() {
        return getDetails().orElseThrow();
    }
}
