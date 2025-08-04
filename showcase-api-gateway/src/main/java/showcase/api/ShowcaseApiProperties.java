package showcase.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("showcase.api")
@Data
@Validated
final class ShowcaseApiProperties {

    @Data
    @AllArgsConstructor
    static final class Cache {

        @Min(0)
        private long maximumSize;

        @NotNull
        @DurationMin(nanos = 0)
        private Duration expiresAfterAccess;

        @NotNull
        @DurationMin(nanos = 0)
        private Duration expiresAfterWrite;
    }

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
    private Map<@NotBlank String, @NotNull @Valid Cache> caches = Map.of(
            ShowcaseApiController.FETCH_ALL_CACHE_NAME,
            new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5)),
            ShowcaseApiController.FETCH_BY_ID_CACHE_NAME,
            new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5)));

    @NotNull
    @Valid
    private Metrics metrics = new Metrics(List.of());

    @NotNull
    @Valid
    private Tracing tracing = new Tracing(false);
}
