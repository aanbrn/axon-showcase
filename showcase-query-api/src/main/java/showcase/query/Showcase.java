// SPDX-License-Identifier: MIT
package showcase.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

/**
 * Read-side projection of a showcase.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder(toBuilder = true)
@Jacksonized
@NullUnmarked
@Schema(description = "Details of a showcase.")
@SuppressWarnings("ClassCanBeRecord")
public class Showcase {
    /**
     * The unique ID of the showcase.
     */
    @NonNull
    @Schema(description = "The unique ID.", example = "33gkCN0UNn3Kzr3x7iuDaVT6sZi")
    String showcaseId;

    /**
     * The unique title of the showcase.
     */
    @NonNull
    @Schema(description = "The unique title.", example = "My Showcase")
    String title;

    /**
     * The date-time when the showcase should be started automatically.
     */
    @NonNull
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase should be started automatically.")
    Instant startTime;

    /**
     * The duration after which the started showcase should be finished automatically.
     */
    @NonNull
    @Schema(
            description =
                    "The duration (in ISO-8601 format) after which the showcase should be finished " + "automatically.",
            type = "string",
            example = "PT5M30S")
    Duration duration;

    /**
     * The current status of the showcase.
     */
    @NonNull
    @Schema(description = "The actual status.")
    ShowcaseStatus status;

    /**
     * The date-time when the showcase was actually scheduled.
     */
    @NonNull
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually scheduled.")
    Instant scheduledAt;

    /**
     * The date-time when the showcase was actually started, if it has been started yet.
     */
    @Nullable
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually started.")
    Instant startedAt;

    /**
     * The date-time when the showcase was actually finished, if it has been finished yet.
     */
    @Nullable
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually finished.")
    Instant finishedAt;
}
