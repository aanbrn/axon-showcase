package showcase.command;

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

import java.time.Duration;
import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullUnmarked
@SuppressWarnings("ClassCanBeRecord")
public class ShowcaseScheduledEvent implements ShowcaseEvent {

    @NonNull
    String showcaseId;

    @NonNull
    String title;

    @NonNull
    Instant startTime;

    @NonNull
    Duration duration;

    @NonNull
    Instant scheduledAt;
}
