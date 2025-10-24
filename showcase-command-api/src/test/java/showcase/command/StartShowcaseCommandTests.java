package showcase.command;

import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.Test;
import showcase.identifier.KSUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

class StartShowcaseCommandTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();

        val command =
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        assertThat(command).isNotNull();
        assertThat(command.showcaseId()).isEqualTo(showcaseId);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> StartShowcaseCommand.builder().build());
    }

    @Test
    void validation_allFieldsValid_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    StartShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build())
            ).isEmpty();
        }
    }

    @Test
    void validation_invalidShowcaseId_detectsShowcaseIdConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    StartShowcaseCommand
                            .builder()
                            .showcaseId(anInvalidShowcaseId())
                            .build())
            ).hasSize(1)
             .first()
             .satisfies(it -> {
                 assertThat(it.getConstraintDescriptor()).isNotNull();
                 assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(KSUID.class);
                 assertThat(it.getPropertyPath()).isNotNull();
                 assertThat(it.getPropertyPath().toString()).isEqualTo("showcaseId");
             });
        }
    }
}
