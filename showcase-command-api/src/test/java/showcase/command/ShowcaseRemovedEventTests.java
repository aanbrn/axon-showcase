package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;

class ShowcaseRemovedEventTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();

        val event =
                ShowcaseRemovedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .removedAt(Instant.now())
                        .build();
        assertThat(event).isNotNull();
        assertThat(event.showcaseId()).isEqualTo(showcaseId);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseRemovedEvent.builder().removedAt(Instant.now()).build());
    }

    @Test
    void construction_missingRemovedAt_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseRemovedEvent.builder().showcaseId(aShowcaseId()).build());
    }
}
