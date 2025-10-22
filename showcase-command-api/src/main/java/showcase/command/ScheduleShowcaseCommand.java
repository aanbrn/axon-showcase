package showcase.command;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import showcase.identifier.KSUID;

import java.time.Duration;
import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Builder(toBuilder = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@SuppressWarnings("ClassCanBeRecord")
public class ScheduleShowcaseCommand implements ShowcaseCommand {

    @NonNull
    @KSUID
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
