// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

/**
 * A Core Web Vital measured in the browser.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@Schema(description = "A Core Web Vital measured in the browser.")
@SuppressWarnings("ClassCanBeRecord")
public class ClientVital {
    /**
     * The vital's name, one of {@code LCP}, {@code INP}, {@code CLS}, {@code FCP}, or {@code TTFB}.
     */
    @NotBlank
    @Pattern(regexp = "LCP|INP|CLS|FCP|TTFB")
    @Schema(description = "The vital's name.")
    String name;

    /**
     * The vital's rating, one of {@code good}, {@code needs-improvement}, or {@code poor}.
     */
    @NotBlank
    @Pattern(regexp = "good|needs-improvement|poor")
    @Schema(description = "The vital's rating.")
    String rating;

    /**
     * The measured value (milliseconds for the time-based vitals, unitless for {@code CLS}).
     */
    @PositiveOrZero
    @Schema(description = "The measured value.")
    double value;
}
