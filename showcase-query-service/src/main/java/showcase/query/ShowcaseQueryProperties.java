package showcase.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static showcase.query.ShowcaseQueryConstants.SHOWCASES_CACHE_NAME;

@ConfigurationProperties("showcase.query")
@Data
@Validated
final class ShowcaseQueryProperties {

    @Data
    @AllArgsConstructor
    static final class Cache {

        @NotNull
        @DurationMin(nanos = 0)
        private Duration timeToLive;
    }

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

    @NotNull
    @Valid
    private Map<@NotBlank String, @NotNull @Valid Cache> caches =
            Map.of(SHOWCASES_CACHE_NAME, new Cache(Duration.ofMinutes(10)));

    @Valid
    private Metrics metrics = new Metrics(List.of());

    @Valid
    private Tracing tracing = new Tracing(false);
}
