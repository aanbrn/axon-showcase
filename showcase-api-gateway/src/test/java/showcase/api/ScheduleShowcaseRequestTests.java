// SPDX-License-Identifier: MIT
package showcase.api;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import showcase.command.ShowcaseDuration;
import showcase.command.ShowcaseTitle;

@DisplayName("Schedule showcase request unit tests")
class ScheduleShowcaseRequestTests {

    @Test
    @DisplayName("A request with all params specified creates an instance with all fields set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        val request = ScheduleShowcaseRequest.builder()
                .title(title)
                .startTime(startTime)
                .duration(duration)
                .build();
        assertThat(request).isNotNull();
        assertThat(request.title()).isEqualTo(title);
        assertThat(request.startTime()).isEqualTo(startTime);
        assertThat(request.duration()).isEqualTo(duration);
    }

    @Test
    @DisplayName("A valid request has no constraint violations")
    void validation_validRequest_detectsNoConstraintViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseRequest.builder()
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build()))
                    .isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("A blank title reports a NotBlank constraint violation")
    void validation_blankTitle_detectsNotBlankConstraintViolation(String blankTitle) {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseRequest.builder()
                            .title(blankTitle)
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build()))
                    .hasSize(1)
                    .first()
                    .satisfies(it -> {
                        assertThat(it.getConstraintDescriptor()).isNotNull();
                        assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(NotBlank.class);
                        assertThat(it.getPropertyPath()).isNotNull();
                        assertThat(it.getPropertyPath().toString()).isEqualTo("title");
                    });
        }
    }

    @Test
    @DisplayName("A too-long title reports a ShowcaseTitle constraint violation")
    void validation_tooLongTitle_detectsShowcaseTitleConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseRequest.builder()
                            .title(aTooLongShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build()))
                    .hasSize(1)
                    .first()
                    .satisfies(it -> {
                        assertThat(it.getConstraintDescriptor()).isNotNull();
                        assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(ShowcaseTitle.class);
                        assertThat(it.getPropertyPath()).isNotNull();
                        assertThat(it.getPropertyPath().toString()).isEqualTo("title");
                    });
        }
    }

    @Test
    @DisplayName("A missing start time reports a NotNull constraint violation")
    void validation_missingStartTime_detectsNotNullConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseRequest.builder()
                            .title(aShowcaseTitle())
                            .startTime(null)
                            .duration(aShowcaseDuration())
                            .build()))
                    .hasSize(1)
                    .first()
                    .satisfies(it -> {
                        assertThat(it.getConstraintDescriptor()).isNotNull();
                        assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(NotNull.class);
                        assertThat(it.getPropertyPath()).isNotNull();
                        assertThat(it.getPropertyPath().toString()).isEqualTo("startTime");
                    });
        }
    }

    @Test
    @DisplayName("A missing duration reports a NotNull constraint violation")
    void validation_missingDuration_detectsNotNullConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseRequest.builder()
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(null)
                            .build()))
                    .hasSize(1)
                    .first()
                    .satisfies(it -> {
                        assertThat(it.getConstraintDescriptor()).isNotNull();
                        assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(NotNull.class);
                        assertThat(it.getPropertyPath()).isNotNull();
                        assertThat(it.getPropertyPath().toString()).isEqualTo("duration");
                    });
        }
    }

    @Test
    @DisplayName("A too-short duration reports a ShowcaseDuration constraint violation")
    void validation_tooShortDuration_detectsShowcaseDurationConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseRequest.builder()
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aTooShortShowcaseDuration())
                            .build()))
                    .hasSize(1)
                    .first()
                    .satisfies(it -> {
                        assertThat(it.getConstraintDescriptor()).isNotNull();
                        assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(ShowcaseDuration.class);
                        assertThat(it.getPropertyPath()).isNotNull();
                        assertThat(it.getPropertyPath().toString()).isEqualTo("duration");
                    });
        }
    }

    @Test
    @DisplayName("A too-long duration reports a ShowcaseDuration constraint violation")
    void validation_tooLongDuration_detectsShowcaseDurationConstraintViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseRequest.builder()
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aTooLongShowcaseDuration())
                            .build()))
                    .hasSize(1)
                    .first()
                    .satisfies(it -> {
                        assertThat(it.getConstraintDescriptor()).isNotNull();
                        assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(ShowcaseDuration.class);
                        assertThat(it.getPropertyPath()).isNotNull();
                        assertThat(it.getPropertyPath().toString()).isEqualTo("duration");
                    });
        }
    }
}
