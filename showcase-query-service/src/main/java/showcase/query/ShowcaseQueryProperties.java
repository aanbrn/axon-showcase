package showcase.query;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("showcase.query")
@Data
@Validated
final class ShowcaseQueryProperties {

    private boolean indexInitializationEnabled = true;

    private boolean exitAfterIndexInitialization;
}
