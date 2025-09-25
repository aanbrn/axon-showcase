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
import java.util.Map;

import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME;

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

    @NotNull
    @Valid
    private Map<@NotBlank String, @NotNull @Valid Cache> caches = Map.of(
            FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME, new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5)),
            FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME, new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5)));
}
