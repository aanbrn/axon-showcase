package showcase.command;

import jakarta.validation.constraints.NotBlank;
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
import showcase.identifier.KSUID;

import java.time.Duration;
import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder(toBuilder = true)
@Jacksonized
@NullUnmarked
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
