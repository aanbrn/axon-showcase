package showcase.command;

import jakarta.validation.constraints.NotBlank;
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
public class ScheduleShowcaseCommand implements ShowcaseCommand {

    @NonNull
    @ShowcaseId
    String showcaseId;

    @NonNull
    @NotBlank
    @ShowcaseTitle
    String title;

    @NonNull
    @ShowcaseStartTime
    Instant startTime;

    @NonNull
    @ShowcaseDuration
    Duration duration;
}
