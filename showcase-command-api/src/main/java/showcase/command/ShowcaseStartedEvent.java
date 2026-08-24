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
 * Event emitted when a scheduled showcase has been started.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullUnmarked
@SuppressWarnings("ClassCanBeRecord")
public class ShowcaseStartedEvent implements ShowcaseEvent {
    /**
     * The ID of the started showcase.
     */
    @NonNull
    String showcaseId;

    /**
     * The duration after which the started showcase should be finished automatically.
     */
    @NonNull
    Duration duration;

    /**
     * The date-time when the showcase was started.
     */
    @NonNull
    Instant startedAt;
}
