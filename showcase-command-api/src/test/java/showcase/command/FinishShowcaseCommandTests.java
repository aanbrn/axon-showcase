package showcase.command;

import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import showcase.identifier.KSUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

@DisplayName("Finish showcase command tests")
class FinishShowcaseCommandTests {

    @Test
    @DisplayName("A command with all params specified creates an instance with all fields set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();

        val command =
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        assertThat(command).isNotNull();
        assertThat(command.showcaseId()).isEqualTo(showcaseId);
    }

    @Test
    @DisplayName("A command without a showcase ID throws a null pointer exception")
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> FinishShowcaseCommand.builder().build());
    }

    @Test
    @DisplayName("A valid showcase ID detects no constraint violations")
    void validation_validShowcaseId_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    FinishShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build())
            ).isEmpty();
        }
    }

    @Test
    @DisplayName("An invalid showcase ID detects a showcase ID constraint violation")
    void validation_invalidShowcaseId_detectsShowcaseIdConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    FinishShowcaseCommand
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
