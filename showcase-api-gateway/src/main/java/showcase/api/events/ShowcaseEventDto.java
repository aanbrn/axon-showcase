// SPDX-License-Identifier: MIT
package showcase.api.events;

import io.swagger.v3.oas.annotations.media.Schema;
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
import org.jspecify.annotations.NullMarked;

/**
 * The SSE payload describing a showcase domain event streamed to clients.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullMarked
@Schema(description = "A showcase domain event streamed over Server-Sent Events.")
@SuppressWarnings("ClassCanBeRecord")
public class ShowcaseEventDto {
    /**
     * The event type.
     */
    @NonNull
    @Schema(
            description = "The type of the domain event.",
            allowableValues = {"SCHEDULED", "STARTED", "FINISHED", "REMOVED"})
    String type;

    /**
     * The ID of the showcase the event concerns.
     */
    @NonNull
    @Schema(description = "The ID of the showcase the event concerns.")
    String showcaseId;

    /**
     * The time the event occurred.
     */
    @NonNull
    @Schema(description = "The time the event occurred.", type = "string")
    Instant timestamp;
}
