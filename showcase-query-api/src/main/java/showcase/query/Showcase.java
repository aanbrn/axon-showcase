package showcase.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Schema(description = "Details of the showcase.")
@SuppressWarnings("ClassCanBeRecord")
public class Showcase {

    @NonNull
    @Schema(description = "The unique ID.", example = "0bb59251-fd63-432c-aead-17e0f1cac36b")
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

    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually started.")
    Instant startedAt;

    @Schema(description = "The date-time (in ISO-8601 format) when the showcase was actually finished.")
    Instant finishedAt;
}
