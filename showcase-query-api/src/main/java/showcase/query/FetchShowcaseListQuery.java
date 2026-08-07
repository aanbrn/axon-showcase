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

/**
 * Query to fetch a paginated list of showcases, optionally filtered by title and status.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
public class FetchShowcaseListQuery {
    /**
     * The minimum allowed page size.
     */
    public static final int MIN_SIZE = 1;

    /**
     * The maximum allowed page size.
     */
    public static final int MAX_SIZE = 1_000;

    /**
     * The default page size.
     */
    public static final int DEFAULT_SIZE = 20;

    /**
     * The title to filter by with full-text matching, if any.
     */
    @Nullable
    String title;

    /**
     * The statuses to filter by, if any.
     */
    @Singular(ignoreNullCollections = true)
    Set<ShowcaseStatus> statuses;

    /**
     * The ID of the showcase after which to fetch, for pagination.
     */
    @Nullable
    @KSUID
    String afterId;

    /**
     * The number of showcases to fetch.
     */
    @Min(MIN_SIZE)
    @Max(MAX_SIZE)
    @Builder.Default
    int size = DEFAULT_SIZE;
}
