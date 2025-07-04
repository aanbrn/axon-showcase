package showcase.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;

class ShowcaseTitleTests {

    @Test
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
