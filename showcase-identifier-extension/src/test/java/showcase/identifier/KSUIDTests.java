package showcase.identifier;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.ksuid.Ksuid.newKsuid;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KSUID constraint tests")
class KSUIDTests {

    @Test
    @DisplayName("A valid KSUID detects no constraint violation")
    void validation_valid_detectsNoConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @KSUID
                @SuppressWarnings("unused")
                private final String showcaseId = newKsuid().toString();
            })).isEmpty();
        }
    }

    @Test
    @DisplayName("An invalid KSUID detects a constraint violation")
    void validation_invalid_detectsConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(new Object() {
                @KSUID
                @SuppressWarnings("unused")
                private final String showcaseId = randomUUID().toString();
            })).hasSize(1)
               .first()
               .extracting(ConstraintViolation::getMessage)
               .isEqualTo("must be a valid KSUID (K-Sortable Unique IDentifier).");
        }
    }
}
