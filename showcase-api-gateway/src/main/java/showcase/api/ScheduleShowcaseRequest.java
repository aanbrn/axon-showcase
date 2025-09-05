package showcase.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import showcase.command.ShowcaseDuration;
import showcase.command.ShowcaseStartTime;
import showcase.command.ShowcaseTitle;

import java.time.Duration;
import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Schema(description = "Request payload to schedule a showcase.")
@SuppressWarnings("ClassCanBeRecord")
class ScheduleShowcaseRequest {

    @NotBlank
    @ShowcaseTitle
    @Schema(
            description = "A unique title.",
            example = "My Showcase",
            maxLength = ShowcaseTitle.MAX_LENGTH
    )
    String title;

    @NotNull
    @ShowcaseStartTime
    @Schema(
            description = "A date-time (in ISO-8601 format) when the scheduled showcase should be started " +
                                  "automatically (must be in the future).",
            type = "string"
    )
    Instant startTime;

    @NotNull
    @ShowcaseDuration
    @Schema(
            description = "A duration (in ISO-8601 format) after which the started showcase should be finished " +
                                  "automatically (min: 1 minute, max: 10 minutes).",
            type = "string",
            example = "PT5M30S",
            minimum = "PT" + ShowcaseDuration.MIN_MINUTES + "M",
            maximum = "PT" + ShowcaseDuration.MAX_MINUTES + "M"
    )
    Duration duration;
}
