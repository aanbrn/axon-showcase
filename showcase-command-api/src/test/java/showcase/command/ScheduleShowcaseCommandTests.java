// SPDX-License-Identifier: MIT
package showcase.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import showcase.identifier.KSUID;

@DisplayName("Schedule showcase command tests")
class ScheduleShowcaseCommandTests {

    @Test
    @DisplayName("A command with all params specified creates an instance with all fields set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        val command = ScheduleShowcaseCommand.builder()
                .showcaseId(showcaseId)
                .title(title)
                .startTime(startTime)
                .duration(duration)
                .build();
        assertThat(command).isNotNull();
        assertThat(command.showcaseId()).isEqualTo(showcaseId);
        assertThat(command.title()).isEqualTo(title);
        assertThat(command.startTime()).isEqualTo(startTime);
        assertThat(command.duration()).isEqualTo(duration);
    }

    @Test
    @DisplayName("A command without a showcase ID throws a null pointer exception")
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> ScheduleShowcaseCommand.builder()
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(Instant.now()))
                .duration(aShowcaseDuration())
                .build());
    }

    @Test
    @DisplayName("A command without a title throws a null pointer exception")
    void construction_missingTitle_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> ScheduleShowcaseCommand.builder()
                .showcaseId(aShowcaseId())
                .startTime(aShowcaseStartTime(Instant.now()))
                .duration(aShowcaseDuration())
                .build());
    }

    @Test
    @DisplayName("A command without a start time throws a null pointer exception")
    void construction_missingStartTime_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> ScheduleShowcaseCommand.builder()
                .showcaseId(aShowcaseId())
                .title(aShowcaseTitle())
                .duration(aShowcaseDuration())
                .build());
    }

    @Test
    @DisplayName("A command without a duration throws a null pointer exception")
    void construction_missingDuration_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> ScheduleShowcaseCommand.builder()
                .showcaseId(aShowcaseId())
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(Instant.now()))
                .build());
    }

    @Test
    @DisplayName("A command with all fields valid detects no constraint violations")
    void validation_allFieldsValid_detectsNoConstrainViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseCommand.builder()
                            .showcaseId(aShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build()))
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("An invalid showcase ID detects a showcase ID constraint violation")
    void construction_invalidShowcaseId_detectsShowcaseIdConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseCommand.builder()
                            .showcaseId(anInvalidShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
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

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    @DisplayName("An empty or blank title detects a not-blank constraint violation")
    void validation_emptyOrBlankTitle_detectsNotBlankConstrainViolation(String emptyOrBlankTitle) {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseCommand.builder()
                            .showcaseId(aShowcaseId())
                            .title(emptyOrBlankTitle)
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
    @DisplayName("A too long title detects a showcase title constraint violation")
    void validation_tooLongTitle_detectsShowcaseTitleConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseCommand.builder()
                            .showcaseId(aShowcaseId())
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
    @DisplayName("A non-future start time detects a showcase start time constraint violation")
    void validation_nowStartTime_detectsShowcaseStartTimeConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseCommand.builder()
                            .showcaseId(aShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(Instant.now())
                            .duration(aShowcaseDuration())
                            .build()))
                    .hasSize(1)
                    .first()
                    .satisfies(it -> {
                        assertThat(it.getConstraintDescriptor()).isNotNull();
                        assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(ShowcaseStartTime.class);
                        assertThat(it.getPropertyPath()).isNotNull();
                        assertThat(it.getPropertyPath().toString()).isEqualTo("startTime");
                    });
        }
    }

    @Test
    @DisplayName("A too short duration detects a showcase duration constraint violation")
    void validation_tooShortDuration_detectsShowcaseDurationConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseCommand.builder()
                            .showcaseId(aShowcaseId())
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
    @DisplayName("A too long duration detects a showcase duration constraint violation")
    void validation_tooLongDuration_detectsShowcaseDurationConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(ScheduleShowcaseCommand.builder()
                            .showcaseId(aShowcaseId())
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
