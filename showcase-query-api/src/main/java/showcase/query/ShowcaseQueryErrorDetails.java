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

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Accessors(fluent = true)
@Builder
@Jacksonized
@NullUnmarked
public class ShowcaseQueryErrorDetails {

    @NonNull
    ShowcaseQueryErrorCode errorCode;

    @NonNull
    String errorMessage;

    @Builder.Default
    MetaData metaData = MetaData.emptyInstance();
}
