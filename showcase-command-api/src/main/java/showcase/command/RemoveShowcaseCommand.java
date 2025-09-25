package showcase.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import showcase.KSUID;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SuppressWarnings("ClassCanBeRecord")
public class RemoveShowcaseCommand implements ShowcaseCommand {

    @NonNull
    @KSUID
    String showcaseId;
}
