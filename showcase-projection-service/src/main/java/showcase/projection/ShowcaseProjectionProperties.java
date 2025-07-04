package showcase.projection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties("showcase.projection")
@Data
@Validated
final class ShowcaseProjectionProperties {

    @Data
    @AllArgsConstructor
    static final class Tag {

        @NotBlank
        private String key;

        @NotBlank
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

    @NotNull
    @Valid
    private Metrics metrics = new Metrics(List.of());

    @NotNull
    @Valid
    private Tracing tracing = new Tracing(false);
}
