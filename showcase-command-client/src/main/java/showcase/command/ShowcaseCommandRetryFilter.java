package showcase.command;

import lombok.NonNull;
import org.axonframework.common.AxonNonTransientException;

import java.util.function.Predicate;

final class ShowcaseCommandRetryFilter implements Predicate<Throwable> {

    @Override
    public boolean test(@NonNull Throwable t) {
        return !(t instanceof ShowcaseCommandException)
                       && !(t instanceof AxonNonTransientException)
                       && !(t.getCause() instanceof AxonNonTransientException);
    }
}
