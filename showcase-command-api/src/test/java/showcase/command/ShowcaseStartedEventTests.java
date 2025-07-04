package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
        assertThat(event.getShowcaseId()).isEqualTo(showcaseId);
        assertThat(event.getDuration()).isEqualTo(duration);
        assertThat(event.getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseStartedEvent
                        .builder()
                        .duration(aShowcaseDuration())
                        .startedAt(Instant.now())
                        .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingDuration_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseStartedEvent
                        .builder()
                        .showcaseId(aShowcaseId())
                        .startedAt(Instant.now())
                        .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingStartedAt_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseStartedEvent
                        .builder()
                        .showcaseId(aShowcaseId())
                        .duration(aShowcaseDuration())
                        .build())
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
