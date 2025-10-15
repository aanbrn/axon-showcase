package showcase.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import showcase.identifier.KSUID;

import java.util.Set;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FetchShowcaseListQuery {

    public static final int MIN_SIZE = 1;

    public static final int MAX_SIZE = 1_000;

    public static final int DEFAULT_SIZE = 20;

    String title;

    @Singular(ignoreNullCollections = true)
    Set<ShowcaseStatus> statuses;

    @KSUID
    String afterId;

    @Min(MIN_SIZE)
    @Max(MAX_SIZE)
    @Builder.Default
    int size = DEFAULT_SIZE;
}
