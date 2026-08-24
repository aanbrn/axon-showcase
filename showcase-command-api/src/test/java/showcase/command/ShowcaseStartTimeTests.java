// SPDX-License-Identifier: MIT
package showcase.command;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Showcase start time tests")
class ShowcaseStartTimeTests {

    @Test
    @DisplayName("A future time detects no constraint violations")
    void validation_futureTime_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                        @ShowcaseStartTime
                        @SuppressWarnings("unused")
                        final Instant startTime = Instant.now().plusSeconds(1);
                    }))
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("A non-future time detects a constraint violation")
    void validation_nowTime_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                        @ShowcaseStartTime
                        @SuppressWarnings("unused")
                        final Instant startTime = Instant.now();
                    }))
                    .hasSize(1)
                    .first()
                    .extracting(ConstraintViolation::getMessage)
                    .isEqualTo("must be a future time");
        }
    }
}
