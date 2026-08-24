// SPDX-License-Identifier: MIT
package showcase.command;

import jakarta.validation.constraints.NotBlank;
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
import showcase.identifier.KSUID;

/**
 * Command to schedule a new showcase.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder(toBuilder = true)
@Jacksonized
@NullUnmarked
@SuppressWarnings("ClassCanBeRecord")
public class ScheduleShowcaseCommand implements ShowcaseCommand {
    /**
     * The ID of the showcase to schedule.
     */
    @NonNull
    @KSUID
    String showcaseId;

    /**
     * The unique title of the showcase.
     */
    @NonNull
    @NotBlank
    @ShowcaseTitle
    String title;

    /**
     * The date-time when the showcase should start automatically.
     */
    @NonNull
    @ShowcaseStartTime
    Instant startTime;

    /**
     * The duration after which the started showcase should be finished automatically.
     */
    @NonNull
    @ShowcaseDuration
    Duration duration;
}
