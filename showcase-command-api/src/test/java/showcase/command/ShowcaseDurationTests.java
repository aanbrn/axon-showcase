package showcase.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ShowcaseDurationTests {

    @Test
    void validation_minimalDuration_detectsNoConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseDuration
                        @SuppressWarnings("unused")
                        final Duration duration = Duration.ofMinutes(ShowcaseDuration.MIN_MINUTES);
                    })
            ).isEmpty();
        }
    }

    @Test
    void validation_maximalDuration_detectsNoConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseDuration
                        @SuppressWarnings("unused")
                        final Duration duration = Duration.ofMinutes(ShowcaseDuration.MAX_MINUTES);
                    })
            ).isEmpty();
        }
    }

    @Test
    void validation_tooShortDuration_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseDuration
                        @SuppressWarnings("unused")
                        final Duration duration = Duration.ofMinutes(ShowcaseDuration.MIN_MINUTES).minusSeconds(1);
                    })
            ).hasSize(1)
             .first()
             .extracting(ConstraintViolation::getMessage)
             .isEqualTo("must be in range %d to %d minutes inclusively".formatted(
                     ShowcaseDuration.MIN_MINUTES, ShowcaseDuration.MAX_MINUTES));
        }
    }

    @Test
    void validation_tooLongDuration_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseDuration
                        @SuppressWarnings("unused")
                        final Duration duration = Duration.ofMinutes(ShowcaseDuration.MAX_MINUTES).plusSeconds(1);
                    })
            ).hasSize(1)
             .first()
             .extracting(ConstraintViolation::getMessage)
             .isEqualTo("must be in range %d to %d minutes inclusively".formatted(
                     ShowcaseDuration.MIN_MINUTES, ShowcaseDuration.MAX_MINUTES));
        }
    }
}
