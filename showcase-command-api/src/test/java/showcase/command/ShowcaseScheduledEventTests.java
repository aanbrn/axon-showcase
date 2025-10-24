package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
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
        assertThat(event.showcaseId()).isEqualTo(showcaseId);
        assertThat(event.title()).isEqualTo(title);
        assertThat(event.startTime()).isEqualTo(startTime);
        assertThat(event.duration()).isEqualTo(duration);
        assertThat(event.scheduledAt()).isEqualTo(scheduledAt);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> ShowcaseScheduledEvent
                                          .builder()
                                          .title(aShowcaseTitle())
                                          .startTime(aShowcaseStartTime(scheduleTime))
                                          .duration(aShowcaseDuration())
                                          .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                                          .build());
    }

    @Test
    void construction_missingTitle_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> ShowcaseScheduledEvent
                                          .builder()
                                          .showcaseId(aShowcaseId())
                                          .startTime(aShowcaseStartTime(scheduleTime))
                                          .duration(aShowcaseDuration())
                                          .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                                          .build());
    }

    @Test
    void construction_missingStartTime_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .duration(aShowcaseDuration())
                              .scheduledAt(Instant.now())
                              .build());
    }

    @Test
    void construction_missingDuration_throwsNullPointerException() {
        val scheduleTime = Instant.now();

        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                              .build());
    }

    @Test
    void construction_missingScheduledAt_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build());
    }
}
