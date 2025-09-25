package showcase.query;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseFinishedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseScheduledAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;

class ShowcaseTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();
        val status = aShowcaseStatus();
        val scheduledAt = aShowcaseScheduledAt(scheduleTime);
        val startedAt = aShowcaseStartedAt(startTime);
        val finishedAt = aShowcaseFinishedAt(startedAt, duration);

        val showcase =
                Showcase.builder()
                        .showcaseId(showcaseId)
                        .title(title)
                        .startTime(startTime)
                        .duration(duration)
                        .status(status)
                        .scheduledAt(scheduledAt)
                        .startedAt(startedAt)
                        .finishedAt(finishedAt)
                        .build();
        assertThat(showcase).isNotNull();
        assertThat(showcase.getShowcaseId()).isEqualTo(showcaseId);
        assertThat(showcase.getTitle()).isEqualTo(title);
        assertThat(showcase.getStartTime()).isEqualTo(startTime);
        assertThat(showcase.getDuration()).isEqualTo(duration);
        assertThat(showcase.getStatus()).isEqualTo(status);
        assertThat(showcase.getScheduledAt()).isEqualTo(scheduledAt);
        assertThat(showcase.getStartedAt()).isEqualTo(startedAt);
        assertThat(showcase.getFinishedAt()).isEqualTo(finishedAt);
    }

    @Test
    void construction_onlyRequiredParamsSpecified_createsInstanceWithRequiredFieldsSet() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();
        val status = aShowcaseStatus();
        val scheduledAt = aShowcaseScheduledAt(scheduleTime);

        val showcase =
                Showcase.builder()
                        .showcaseId(showcaseId)
                        .title(title)
                        .startTime(startTime)
                        .duration(duration)
                        .status(status)
                        .scheduledAt(scheduledAt)
                        .build();
        assertThat(showcase).isNotNull();
        assertThat(showcase.getShowcaseId()).isEqualTo(showcaseId);
        assertThat(showcase.getTitle()).isEqualTo(title);
        assertThat(showcase.getStartTime()).isEqualTo(startTime);
        assertThat(showcase.getDuration()).isEqualTo(duration);
        assertThat(showcase.getStatus()).isEqualTo(status);
        assertThat(showcase.getScheduledAt()).isEqualTo(scheduledAt);
        assertThat(showcase.getStartedAt()).isNull();
        assertThat(showcase.getFinishedAt()).isNull();
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatThrownBy(
                () -> Showcase.builder()
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingTitle_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingStartTime_throwsNullPointerException() {
        assertThatThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(Instant.now()))
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingDuration_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingStatus_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingScheduledAt_throwsNullPointerException() {
        assertThatThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }
}
