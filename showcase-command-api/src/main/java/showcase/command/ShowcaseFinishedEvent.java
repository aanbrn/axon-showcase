// SPDX-License-Identifier: MIT
package showcase.command;

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
 * Event emitted when a started showcase has been finished.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullUnmarked
@SuppressWarnings("ClassCanBeRecord")
public class ShowcaseFinishedEvent implements ShowcaseEvent {
    /**
     * The ID of the finished showcase.
     */
    @NonNull
    String showcaseId;

    /**
     * The date-time when the showcase was finished.
     */
    @NonNull
    Instant finishedAt;
}
