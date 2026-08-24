// SPDX-License-Identifier: MIT
package showcase.command;

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

/**
 * Event emitted when a showcase has been scheduled.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullUnmarked
@SuppressWarnings("ClassCanBeRecord")
public class ShowcaseScheduledEvent implements ShowcaseEvent {
    /**
     * The ID of the scheduled showcase.
     */
    @NonNull
    String showcaseId;

    /**
     * The title of the scheduled showcase.
     */
    @NonNull
    String title;

    /**
     * The date-time when the showcase is scheduled to start automatically.
     */
    @NonNull
    Instant startTime;

    /**
     * The duration after which the started showcase should be finished automatically.
     */
    @NonNull
    Duration duration;

    /**
     * The date-time when the showcase was scheduled.
     */
    @NonNull
    Instant scheduledAt;
}
