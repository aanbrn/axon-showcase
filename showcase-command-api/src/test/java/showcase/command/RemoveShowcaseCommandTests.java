package showcase.command;

import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

class RemoveShowcaseCommandTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();

        val command =
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        assertThat(command).isNotNull();
        assertThat(command.getShowcaseId()).isEqualTo(showcaseId);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatCode(() -> RemoveShowcaseCommand.builder().build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void validation_validShowcaseId_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    RemoveShowcaseCommand
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
                    FinishShowcaseCommand
                            .builder()
                            .showcaseId(anInvalidShowcaseId())
                            .build())
            ).hasSize(1)
             .first()
             .satisfies(it -> {
                 assertThat(it.getConstraintDescriptor()).isNotNull();
                 assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(ShowcaseId.class);
                 assertThat(it.getPropertyPath()).isNotNull();
                 assertThat(it.getPropertyPath().toString()).isEqualTo("showcaseId");
             });
        }
    }
}
