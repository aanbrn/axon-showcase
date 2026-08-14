package showcase.query;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseFinishedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseScheduledAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;

@DisplayName("Showcase tests")
class ShowcaseTests {

    @Test
    @DisplayName("A showcase with all params specified creates an instance with all fields set")
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
        assertThat(showcase.showcaseId()).isEqualTo(showcaseId);
        assertThat(showcase.title()).isEqualTo(title);
        assertThat(showcase.startTime()).isEqualTo(startTime);
        assertThat(showcase.duration()).isEqualTo(duration);
        assertThat(showcase.status()).isEqualTo(status);
        assertThat(showcase.scheduledAt()).isEqualTo(scheduledAt);
        assertThat(showcase.startedAt()).isEqualTo(startedAt);
        assertThat(showcase.finishedAt()).isEqualTo(finishedAt);
    }

    @Test
    @DisplayName("A showcase with only required params specified creates an instance with required fields set")
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
        assertThat(showcase.showcaseId()).isEqualTo(showcaseId);
        assertThat(showcase.title()).isEqualTo(title);
        assertThat(showcase.startTime()).isEqualTo(startTime);
        assertThat(showcase.duration()).isEqualTo(duration);
        assertThat(showcase.status()).isEqualTo(status);
        assertThat(showcase.scheduledAt()).isEqualTo(scheduledAt);
        assertThat(showcase.startedAt()).isNull();
        assertThat(showcase.finishedAt()).isNull();
    }

    @Test
    @DisplayName("A showcase without a showcase ID throws a null pointer exception")
    void construction_missingShowcaseId_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatNullPointerException().isThrownBy(
                () -> Showcase.builder()
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build());
    }

    @Test
    @DisplayName("A showcase without a title throws a null pointer exception")
    void construction_missingTitle_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatNullPointerException().isThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build());
    }

    @Test
    @DisplayName("A showcase without a start time throws a null pointer exception")
    void construction_missingStartTime_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(Instant.now()))
                              .build());
    }

    @Test
    @DisplayName("A showcase without a duration throws a null pointer exception")
    void construction_missingDuration_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatNullPointerException().isThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .status(aShowcaseStatus())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build());
    }

    @Test
    @DisplayName("A showcase without a status throws a null pointer exception")
    void construction_missingStatus_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatNullPointerException().isThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build());
    }

    @Test
    @DisplayName("A showcase without a scheduled-at time throws a null pointer exception")
    void construction_missingScheduledAt_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> Showcase.builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .status(aShowcaseStatus())
                              .build());
    }
}
