package showcase.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ShowcaseStartTimeTests {

    @Test
    void validation_futureTime_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseStartTime
                        @SuppressWarnings("unused")
                        final Instant startTime = Instant.now().plusSeconds(1);
                    })
            ).isEmpty();
        }
    }

    @Test
    void validation_nowTime_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseStartTime
                        @SuppressWarnings("unused")
                        final Instant startTime = Instant.now();
                    })
            ).hasSize(1)
             .first()
             .extracting(ConstraintViolation::getMessage)
             .isEqualTo("must be a future time");
        }
    }
}
