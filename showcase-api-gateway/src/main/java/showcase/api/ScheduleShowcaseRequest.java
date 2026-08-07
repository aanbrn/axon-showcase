package showcase.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import showcase.command.ShowcaseDuration;
import showcase.command.ShowcaseStartTime;
import showcase.command.ShowcaseTitle;

import java.time.Duration;
import java.time.Instant;

/**
 * Request payload to schedule a new showcase.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@Schema(description = "Request payload to schedule a new showcase.")
@SuppressWarnings("ClassCanBeRecord")
class ScheduleShowcaseRequest {
    /**
     * A unique title for the showcase.
     */
    @NotBlank
    @ShowcaseTitle
    @Schema(
            description = "A unique title for the showcase.",
            example = "My Showcase",
            maxLength = ShowcaseTitle.MAX_LENGTH
    )
    String title;

    /**
     * The date-time when the showcase should start automatically (must be in the future).
     */
    @NotNull
    @ShowcaseStartTime
    @Schema(
            description = "The date-time (in ISO-8601 format) when the showcase should start automatically (must be " +
                                  "in the future).",
            type = "string"
    )
    Instant startTime;

    /**
     * The duration after which the started showcase should be finished automatically (min: 1 minute, max: 10 minutes).
     */
    @NotNull
    @ShowcaseDuration
    @Schema(
            description = "The duration (in ISO-8601 format) after which the started showcase should be finished " +
                                  "automatically (min: 1 minute, max: 10 minutes).",
            type = "string",
            example = "PT5M30S",
            minimum = "PT" + ShowcaseDuration.MIN_MINUTES + "M",
            maximum = "PT" + ShowcaseDuration.MAX_MINUTES + "M"
    )
    Duration duration;
}
