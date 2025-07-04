package showcase.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties("showcase.query")
@Data
@Validated
final class ShowcaseQueryProperties {

    @Data
    @AllArgsConstructor
    static final class Tag {

        @NotEmpty
        private String key;

        @NotEmpty
        private String value;
    }

    @Data
    @AllArgsConstructor
    static final class Metrics {

        @NotNull
        private List<Tag> tags;
    }

    @Data
    @AllArgsConstructor
    static final class Tracing {

        private boolean logging;
    }

    private boolean indexInitializationEnabled = true;

    private boolean exitAfterIndexInitialization;

    @Valid
    private Metrics metrics = new Metrics(List.of());

    @Valid
    private Tracing tracing = new Tracing(false);
}
