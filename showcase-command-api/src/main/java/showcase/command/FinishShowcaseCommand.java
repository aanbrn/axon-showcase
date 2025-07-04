package showcase.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FinishShowcaseCommand implements ShowcaseCommand {

    @NonNull
    @ShowcaseId
    String showcaseId;
}
