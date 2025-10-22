package showcase.query;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.axonframework.messaging.MetaData;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Builder
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ShowcaseQueryErrorDetails {

    @NonNull
    ShowcaseQueryErrorCode errorCode;

    @NonNull
    String errorMessage;

    @Builder.Default
    MetaData metaData = MetaData.emptyInstance();
}
