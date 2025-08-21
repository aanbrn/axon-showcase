package showcase;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.Test;
import ulid4j.Ulid;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.test.RandomTestUtils.anAlphabeticString;

class ULIDTests {

    @Test
    void validation_valid_detectsNoConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @ULID
                @SuppressWarnings("unused")
                private final String showcaseId = new Ulid().create();
            })).isEmpty();
        }
    }

    @Test
    void validation_invalid_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @ULID
                @SuppressWarnings("unused")
                private final String showcaseId = anAlphabeticString(26);
            })).hasSize(1)
               .first()
               .extracting(ConstraintViolation::getMessage)
               .isEqualTo("must be a valid ULID");
        }
    }
}
