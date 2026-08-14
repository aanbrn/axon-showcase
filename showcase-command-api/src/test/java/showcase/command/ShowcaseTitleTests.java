package showcase.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;

@DisplayName("Showcase title tests")
class ShowcaseTitleTests {

    @Test
    @DisplayName("A long enough title detects no constraint violations")
    void validation_longEnoughTitle_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseTitle
                        @SuppressWarnings("unused")
                        final String title = aShowcaseTitle();
                    })
            ).isEmpty();
        }
    }

    @Test
    @DisplayName("A too long title detects a constraint violation")
    void validation_tooLongTitle_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    new Object() {
                        @ShowcaseTitle
                        @SuppressWarnings("unused")
                        final String title = aTooLongShowcaseTitle();
                    })
            ).hasSize(1)
             .first()
             .extracting(ConstraintViolation::getMessage)
             .isEqualTo("must be not longer than %d characters".formatted(ShowcaseTitle.MAX_LENGTH));
        }
    }
}
