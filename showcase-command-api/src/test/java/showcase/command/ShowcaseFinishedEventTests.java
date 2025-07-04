package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
        assertThat(event.getShowcaseId()).isEqualTo(showcaseId);
        assertThat(event.getFinishedAt()).isEqualTo(finishedAt);
    }

    @Test
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseFinishedEvent
                              .builder()
                              .finishedAt(Instant.now())
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingFinishedAt_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseFinishedEvent
                              .builder()
                              .showcaseId(aShowcaseId())
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }
}
