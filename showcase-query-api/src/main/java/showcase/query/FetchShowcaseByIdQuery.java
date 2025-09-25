package showcase.query;

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
public class FetchShowcaseByIdQuery {

    @NonNull
    @KSUID
    String showcaseId;
}
