package showcase.command;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Builder
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@SuppressWarnings("ClassCanBeRecord")
public class ShowcaseFinishedEvent implements ShowcaseEvent {

    @NonNull
    String showcaseId;

    @NonNull
    Instant finishedAt;
}
