package showcase.query;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("showcase.query")
@Data
@Validated
final class ShowcaseQueryClientProperties {

    @NotEmpty
    @URL
    private String apiUrl;
}
