// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

/**
 * The client-telemetry ingestion API.
 *
 * <p>The standalone web UI reports browser-side Core Web Vitals and JavaScript errors here, so a visitor's experience
 * is observable without the UI reaching into the write or read sides.
 */
@Tag(name = "Client Telemetry")
@SuppressWarnings("unused")
interface TelemetryApi {
    /**
     * Records a batch of browser-side client telemetry.
     *
     * @param report the telemetry report
     * @return {@code 204 No Content} once the report is recorded
     */
    @Operation(
            description = "Records a batch of browser-side Core Web Vitals and JavaScript errors.",
            method = "POST",
            requestBody =
                    @RequestBody(
                            description = "The client telemetry to record.",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE),
                            required = true),
            responses = @ApiResponse(responseCode = "204", description = "The client telemetry has been recorded."))
    Mono<ResponseEntity<Void>> report(@NotNull @Valid ClientTelemetryReport report);
}
