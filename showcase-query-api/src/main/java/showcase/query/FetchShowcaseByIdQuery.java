package showcase.query;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import showcase.command.ShowcaseId;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FetchShowcaseByIdQuery {

    @NonNull
    @ShowcaseId
    String showcaseId;
}
