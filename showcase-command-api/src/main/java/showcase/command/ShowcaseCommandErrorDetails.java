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

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder
@Jacksonized
@NullUnmarked
public class ShowcaseCommandErrorDetails implements Serializable {

    @NonNull
    ShowcaseCommandErrorCode errorCode;

    @NonNull
    String errorMessage;

    @Builder.Default
    MetaData metaData = MetaData.emptyInstance();
}
