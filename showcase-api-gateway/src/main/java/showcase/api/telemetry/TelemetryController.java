// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import reactor.core.publisher.Mono;
import showcase.api.ShowcaseApiErrorResolver;

/**
 * REST controller implementing the client-telemetry ingestion API.
 *
 * <p>Recording is in-memory only — the report never reaches the command or query side — so the endpoint stays
 * fire-and-forget.
 */
@RestController
@RequestMapping("/telemetry")
@RequiredArgsConstructor
final class TelemetryController implements TelemetryApi {
    /**
     * The recorder turning a report into metrics.
     */
    private final TelemetryRecorder telemetryRecorder;

    /**
     * Resolves Spring validation exceptions into per-parameter error maps on problem details.
     */
    private final ShowcaseApiErrorResolver errorResolver;

    /**
     * Records a batch of browser-side client telemetry.
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Override
    public Mono<ResponseEntity<Void>> report(@NotNull @Valid @RequestBody ClientTelemetryReport report) {
        telemetryRecorder.record(report);
        return Mono.just(ResponseEntity.noContent().build());
    }

    /**
     * Maps validation failures to a structured problem detail with a body-error breakdown.
     *
     * @param e      the handler method validation exception to resolve
     * @param locale the locale used for error messages
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    @SuppressWarnings("unused")
    private ProblemDetail handleHandlerMethodValidationException(HandlerMethodValidationException e, Locale locale) {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request.");
        errorResolver.resolve(e, locale, problemDetail);
        return problemDetail;
    }
}
