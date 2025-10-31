package showcase.projection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties("showcase.projector")
@Data
@Validated
class ShowcaseProjectorProperties {

    @Data
    @AllArgsConstructor
    static class Batch {

        @Min(1)
        @Max(1_000)
        private int maxSize;

        @NotNull
        @DurationMin(millis = 1)
        @DurationMax(millis = 1_000)
        private Duration maxTime;

        @Min(1_000)
        @Max(100_000)
        private int bufferMaxSize;
    }

    @Data
    @AllArgsConstructor
    static class Retry {

        @Min(0)
        private int maxAttempts;

        @NotNull
        @DurationMin(millis = 0)
        @DurationMax(millis = 1_000)
        private Duration minBackoff;
    }

    @Data
    @AllArgsConstructor
    static class Restart {

        @NotNull
        @DurationMin(seconds = 1)
        @DurationMax(seconds = 60)
        private Duration delay;
    }

    @Min(1)
    private int minConcurrency = 1;

    @Min(1)
    private int maxConcurrency = 256;

    @NotNull
    @Valid
    private Batch batch = new Batch(100, Duration.ofMillis(100), 10_000);

    @NotNull
    @Valid
    private Retry retry = new Retry(3, Duration.ofMillis(100));

    @NotNull
    @Valid
    private Restart restart = new Restart(Duration.ofSeconds(10));
}
