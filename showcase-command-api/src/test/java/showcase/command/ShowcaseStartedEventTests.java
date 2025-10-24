package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;

class ShowcaseStartedEventTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();
        val duration = aShowcaseDuration();
        val startedAt = Instant.now();

        val event =
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(startedAt)
                        .build();
        assertThat(event).isNotNull();
        assertThat(event.showcaseId()).isEqualTo(showcaseId);
        assertThat(event.duration()).isEqualTo(duration);
        assertThat(event.startedAt()).isEqualTo(startedAt);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseStartedEvent
                              .builder()
                              .duration(aShowcaseDuration())
                              .startedAt(Instant.now())
                              .build());
    }

    @Test
    void construction_missingDuration_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseStartedEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .startedAt(Instant.now())
                              .build());
    }

    @Test
    void construction_missingStartedAt_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseStartedEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .duration(aShowcaseDuration())
                              .build());
    }
}
