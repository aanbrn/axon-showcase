package showcase.query;

import org.axonframework.queryhandling.QueryExecutionException;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class ShowcaseQueryException extends QueryExecutionException {

    public ShowcaseQueryException(ShowcaseQueryErrorDetails errorDetails) {
        super(errorDetails.errorMessage(), null, errorDetails);
    }

    public ShowcaseQueryException(ShowcaseQueryErrorDetails errorDetails, @Nullable Throwable cause) {
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
