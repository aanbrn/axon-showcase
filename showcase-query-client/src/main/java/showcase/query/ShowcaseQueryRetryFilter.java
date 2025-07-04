package showcase.query;

import lombok.NonNull;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

final class ShowcaseQueryRetryFilter implements Predicate<Throwable> {

    @Override
    public boolean test(@NonNull Throwable t) {
        if (t instanceof WebClientResponseException e) {
            return switch (e.getStatusCode().value()) {
                case 408, // Request Timeout
                     425, // Too Early
                     429, // Too Many Requests
                     500, // Internal Server Error
                     502, // Bad Gateway
                     503, // Service Unavailable
                     504, // Gateway Timeout
                     524  // Timeout Occurred
                        -> true;
                default -> false;
            };
        }
        return t instanceof TimeoutException || t instanceof WebClientRequestException;
    }
}
