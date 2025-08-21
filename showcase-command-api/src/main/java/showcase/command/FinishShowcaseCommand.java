package showcase.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import showcase.ULID;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SuppressWarnings("ClassCanBeRecord")
public class FinishShowcaseCommand implements ShowcaseCommand {

    @NonNull
    @ULID
    String showcaseId;
}
