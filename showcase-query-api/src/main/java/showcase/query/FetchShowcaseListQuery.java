package showcase.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.Nullable;
import showcase.identifier.KSUID;

import java.util.Set;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
public class FetchShowcaseListQuery {

    public static final int MIN_SIZE = 1;

    public static final int MAX_SIZE = 1_000;

    public static final int DEFAULT_SIZE = 20;

    @Nullable
    String title;

    @Singular(ignoreNullCollections = true)
    Set<ShowcaseStatus> statuses;

    @Nullable
    @KSUID
    String afterId;

    @Min(MIN_SIZE)
    @Max(MAX_SIZE)
    @Builder.Default
    int size = DEFAULT_SIZE;
}
