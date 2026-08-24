// SPDX-License-Identifier: MIT
package showcase.query;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the query client.
 */
@ConfigurationProperties("showcase.query")
@Data
@Validated
@NullUnmarked
final class ShowcaseQueryClientProperties {
    /**
     * The base URL of the showcase query service, must be a non-empty HTTP URL.
     */
    @NotEmpty
    @URL
    private String apiUrl;
}
