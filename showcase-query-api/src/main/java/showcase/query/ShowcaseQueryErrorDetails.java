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
import org.axonframework.messaging.MetaData;
import org.jspecify.annotations.NullUnmarked;

/**
 * Structured details describing a failed showcase query.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Accessors(fluent = true)
@Builder
@Jacksonized
@NullUnmarked
public class ShowcaseQueryErrorDetails {
    /**
     * The error code categorizing the failure.
     */
    @NonNull
    ShowcaseQueryErrorCode errorCode;

    /**
     * The human-readable error message.
     */
    @NonNull
    String errorMessage;

    /**
     * Additional metadata associated with the error, empty by default.
     */
    @Builder.Default
    MetaData metaData = MetaData.emptyInstance();
}
