package showcase.query;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the query service.
 */
@ConfigurationProperties("showcase.query")
@Data
@Validated
final class ShowcaseQueryProperties {
    /**
     * Whether to initialize the OpenSearch index on startup.
     */
    private boolean indexInitializationEnabled = true;

    /**
     * Whether to exit the JVM after the index is initialized (used in containers).
     */
    private boolean exitAfterIndexInitialization;

    /**
     * Whether query payloads are validated against bean validation constraints before handling.
     */
    private boolean validationEnabled = true;
}
