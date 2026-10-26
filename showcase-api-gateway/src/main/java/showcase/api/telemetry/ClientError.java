// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.Nullable;

/**
 * An uncaught JavaScript error or unhandled rejection observed in the browser.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@Schema(description = "An uncaught JavaScript error or unhandled rejection observed in the browser.")
@SuppressWarnings("ClassCanBeRecord")
public class ClientError {
    /**
     * The error's name (e.g. {@code TypeError}), when known.
     */
    @Nullable
    @Size(max = 100)
    @Schema(description = "The error's name.")
    String type;

    /**
     * The error's message, when known.
     */
    @Nullable
    @Size(max = 500)
    @Schema(description = "The error's message.")
    String message;

    /**
     * The source file the error was raised from, when known.
     */
    @Nullable
    @Size(max = 500)
    @Schema(description = "The source file the error was raised from.")
    String source;

    /**
     * The source line the error was raised at, when known.
     */
    @Nullable
    @Schema(description = "The source line the error was raised at.")
    Integer line;

    /**
     * The source column the error was raised at, when known.
     */
    @Nullable
    @Schema(description = "The source column the error was raised at.")
    Integer column;
}
