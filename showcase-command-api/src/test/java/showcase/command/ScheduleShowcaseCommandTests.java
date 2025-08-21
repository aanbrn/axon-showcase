package showcase.command;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import showcase.ULID;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

class ScheduleShowcaseCommandTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        val command =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(title)
                        .startTime(startTime)
                        .duration(duration)
                        .build();
        assertThat(command).isNotNull();
        assertThat(command.getShowcaseId()).isEqualTo(showcaseId);
        assertThat(command.getTitle()).isEqualTo(title);
        assertThat(command.getStartTime()).isEqualTo(startTime);
        assertThat(command.getDuration()).isEqualTo(duration);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatCode(
                () -> ScheduleShowcaseCommand
                              .builder()
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingTitle_throwsNullPointerException() {
        assertThatCode(
                () -> ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(aShowcaseId())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingStartTime_throwsNullPointerException() {
        assertThatCode(
                () -> ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .duration(aShowcaseDuration())
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingDuration_throwsNullPointerException() {
        assertThatCode(
                () -> ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void validation_allFieldsValid_detectsNoConstrainViolations() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build()
            )).isEmpty();
        }
    }

    @Test
    void construction_invalidShowcaseId_detectsShowcaseIdConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(anInvalidShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build())
            ).hasSize(1)
             .first()
             .satisfies(it -> {
                 assertThat(it.getConstraintDescriptor()).isNotNull();
                 assertThat(it.getConstraintDescriptor().getAnnotation()).isInstanceOf(ULID.class);
                 assertThat(it.getPropertyPath()).isNotNull();
                 assertThat(it.getPropertyPath().toString()).isEqualTo("showcaseId");
             });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "   ", "\t", "\n" })
    void validation_emptyOrBlankTitle_detectsNotBlankConstrainViolation(String emptyOrBlankTitle) {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .title(emptyOrBlankTitle)
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build())
            ).hasSize(1)
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
    void validation_tooLongTitle_detectsShowcaseTitleConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .title(aTooLongShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build())
            ).hasSize(1)
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
    void validation_nowStartTime_detectsShowcaseStartTimeConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(Instant.now())
                            .duration(aShowcaseDuration())
                            .build())
            ).hasSize(1)
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
    void validation_tooShortDuration_detectsShowcaseDurationConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aTooShortShowcaseDuration())
                            .build())
            ).hasSize(1)
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
    void validation_tooLongDuration_detectsShowcaseDurationConstrainViolation() {
        try (val validatorFactory = Validation.buildDefaultValidatorFactory()) {
            val validator = validatorFactory.getValidator();
            assertThat(validator.validate(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(aShowcaseId())
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aTooLongShowcaseDuration())
                            .build())
            ).hasSize(1)
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
