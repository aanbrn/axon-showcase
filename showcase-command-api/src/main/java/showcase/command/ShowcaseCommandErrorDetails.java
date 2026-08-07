package showcase.command;

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

import java.io.Serializable;

/**
 * Structured details describing a failed showcase command.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullUnmarked
public class ShowcaseCommandErrorDetails implements Serializable {
    /**
     * The error code categorizing the failure.
     */
    @NonNull
    ShowcaseCommandErrorCode errorCode;

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
