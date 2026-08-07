package showcase.query;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NullUnmarked;
import showcase.identifier.KSUID;

/**
 * Query to fetch a single showcase by its ID.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullUnmarked
@SuppressWarnings("ClassCanBeRecord")
public class FetchShowcaseByIdQuery {
    /**
     * The ID of the showcase to fetch.
     */
    @NonNull
    @KSUID
    String showcaseId;
}
