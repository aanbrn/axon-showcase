package showcase.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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
