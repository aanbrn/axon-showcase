package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;

class ShowcaseFinishedEventTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();
        val finishedAt = Instant.now();

        val event =
                ShowcaseFinishedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .finishedAt(finishedAt)
                        .build();
        assertThat(event).isNotNull();
        assertThat(event.showcaseId()).isEqualTo(showcaseId);
        assertThat(event.finishedAt()).isEqualTo(finishedAt);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseFinishedEvent
                              .builder()
                              .finishedAt(Instant.now())
                              .build());
    }

    @Test
    void construction_missingFinishedAt_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseFinishedEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .build());
    }
}
