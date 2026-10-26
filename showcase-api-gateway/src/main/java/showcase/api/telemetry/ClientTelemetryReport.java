// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

/**
 * A batch of browser-side Core Web Vitals and JavaScript errors reported by the web UI.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@Schema(description = "A batch of browser-side Core Web Vitals and JavaScript errors reported by the web UI.")
@SuppressWarnings("ClassCanBeRecord")
public class ClientTelemetryReport {
    /**
     * The route the report was produced on.
     */
    @NotBlank
    @Size(max = 200)
    @Pattern(regexp = "^/.*")
    @Schema(description = "The route the report was produced on.")
    String path;

    /**
     * The Core Web Vitals measured on this page; an omitted or {@code null} list is treated as empty.
     */
    @Size(max = 10)
    @Schema(description = "The Core Web Vitals measured on this page.")
    @Singular(ignoreNullCollections = true)
    List<@NotNull @Valid ClientVital> vitals;

    /**
     * The JavaScript errors observed on this page; an omitted or {@code null} list is treated as empty.
     */
    @Size(max = 20)
    @Schema(description = "The JavaScript errors observed on this page.")
    @Singular(ignoreNullCollections = true)
    List<@NotNull @Valid ClientError> errors;
}
