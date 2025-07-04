package showcase.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

class ShowcaseIdTests {

    @Test
    void validation_validUuidV4_detectsNoConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @ShowcaseId
                @SuppressWarnings("unused")
                private final String showcaseId = aShowcaseId();
            })).isEmpty();
        }
    }

    @Test
    void validation_invalidUuidV4_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @ShowcaseId
                @SuppressWarnings("unused")
                private final String showcaseId = anInvalidShowcaseId();
            })).hasSize(1)
               .first()
               .extracting(ConstraintViolation::getMessage)
               .isEqualTo("must be a valid version 4 UUID in lower case");
        }
    }

    @Test
    void validation_nilUuidV4_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @ShowcaseId
                @SuppressWarnings("unused")
                private final String showcaseId = "00000000-0000-0000-0000-000000000000";
            })).hasSize(1)
               .first()
               .extracting(ConstraintViolation::getMessage)
               .isEqualTo("must be a valid version 4 UUID in lower case");
        }
    }

    @Test
    void validation_validUuidV4InUpperCase_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @ShowcaseId
                @SuppressWarnings("unused")
                private final String showcaseId = aShowcaseId().toUpperCase();
            })).hasSize(1)
               .first()
               .extracting(ConstraintViolation::getMessage)
               .isEqualTo("must be a valid version 4 UUID in lower case");
        }
    }
}
