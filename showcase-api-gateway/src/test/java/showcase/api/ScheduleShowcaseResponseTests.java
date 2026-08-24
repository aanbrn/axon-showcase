// SPDX-License-Identifier: MIT
package showcase.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

import jakarta.validation.Validation;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import showcase.identifier.KSUID;

@DisplayName("Schedule showcase response unit tests")
class ScheduleShowcaseResponseTests {

    @Test
    @DisplayName("A response with a showcase ID creates an instance with the field set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();

        val response = ScheduleShowcaseResponse.builder().showcaseId(showcaseId).build();
        assertThat(response).isNotNull();
        assertThat(response.showcaseId()).isEqualTo(showcaseId);
    }

    @Test
    @DisplayName("A response without a showcase ID throws a null pointer exception")
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> ScheduleShowcaseResponse.builder().build());
    }

    @Test
    @DisplayName("A response with a valid showcase ID has no constraint violations")
    void validation_validShowcaseId_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseResponse.builder()
                            .showcaseId(aShowcaseId())
                            .build()))
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("A response with an invalid showcase ID reports a KSUID constraint violation")
    void validation_invalidShowcaseId_detectsKsuidConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseResponse.builder()
                            .showcaseId(anInvalidShowcaseId())
                            .build()))
                    .hasSize(1)
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
