package showcase.command;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.axonframework.messaging.MetaData;

import java.io.Serializable;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Builder
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ShowcaseCommandErrorDetails implements Serializable {

    @NonNull
    ShowcaseCommandErrorCode errorCode;

    @NonNull
    String errorMessage;

    @Builder.Default
    MetaData metaData = MetaData.emptyInstance();
}
