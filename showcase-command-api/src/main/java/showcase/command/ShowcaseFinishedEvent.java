package showcase.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ShowcaseFinishedEvent implements ShowcaseEvent {

    @NonNull
    String showcaseId;

    @NonNull
    Instant finishedAt;
}
