package showcase.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.axonframework.messaging.MetaData;

import java.io.Serializable;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ShowcaseCommandErrorDetails implements Serializable {

    @NonNull
    ShowcaseCommandErrorCode errorCode;

    @NonNull
    String errorMessage;

    @Builder.Default
    MetaData metaData = MetaData.emptyInstance();
}
