package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseScheduledAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

class ShowcaseScheduledEventTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();
        val scheduledAt = aShowcaseScheduledAt(scheduleTime);

        val event =
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(title)
                        .startTime(startTime)
                        .duration(duration)
                        .scheduledAt(scheduledAt)
                        .build();
        assertThat(event).isNotNull();
        assertThat(event.getShowcaseId()).isEqualTo(showcaseId);
        assertThat(event.getTitle()).isEqualTo(title);
        assertThat(event.getStartTime()).isEqualTo(startTime);
        assertThat(event.getDuration()).isEqualTo(duration);
        assertThat(event.getScheduledAt()).isEqualTo(scheduledAt);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatCode(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingTitle_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatCode(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingStartTime_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .duration(aShowcaseDuration())
                              .scheduledAt(Instant.now())
                              .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingDuration_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatCode(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingScheduledAt_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
