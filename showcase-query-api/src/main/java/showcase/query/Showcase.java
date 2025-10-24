package showcase.query;

import io.swagger.v3.oas.annotations.media.Schema;
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

import java.time.Duration;
import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder(toBuilder = true)
@Jacksonized
@NullUnmarked
@Schema(description = "Details of the showcase.")
@SuppressWarnings("ClassCanBeRecord")
public class Showcase {

    @NonNull
    @Schema(description = "The unique ID.", example = "33gkCN0UNn3Kzr3x7iuDaVT6sZi")
    String showcaseId;

    @NonNull
    @Schema(description = "The unique title.", example = "My Showcase")
    String title;

    @NonNull
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase should be started automatically.")
    Instant startTime;

    @NonNull
    @Schema(
            description = "The duration (in ISO-8601 format) after which the showcase should be finished " +
                                  "automatically.",
            type = "string",
            example = "PT5M30S"
    )
    Duration duration;

    @NonNull
    @Schema(description = "The actual status.")
    ShowcaseStatus status;

    @NonNull
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually scheduled.")
    Instant scheduledAt;

    @Nullable
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually started.")
    Instant startedAt;

    @Nullable
    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually finished.")
    Instant finishedAt;
}
