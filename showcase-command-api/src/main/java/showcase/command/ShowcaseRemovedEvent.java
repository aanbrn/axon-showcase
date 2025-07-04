package showcase.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ShowcaseRemovedEvent implements ShowcaseEvent {

    @NonNull
    String showcaseId;
}
