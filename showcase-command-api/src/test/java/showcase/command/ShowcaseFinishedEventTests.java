// SPDX-License-Identifier: MIT
package showcase.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;

import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Showcase finished event tests")
class ShowcaseFinishedEventTests {

    @Test
    @DisplayName("An event with all params specified creates an instance with all fields set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();
        val finishedAt = Instant.now();

        val event = ShowcaseFinishedEvent.builder()
                .showcaseId(showcaseId)
                .finishedAt(finishedAt)
                .build();
        assertThat(event).isNotNull();
        assertThat(event.showcaseId()).isEqualTo(showcaseId);
        assertThat(event.finishedAt()).isEqualTo(finishedAt);
    }

    @Test
    @DisplayName("An event without a showcase ID throws a null pointer exception")
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> ShowcaseFinishedEvent.builder()
                .finishedAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("An event without a finished-at time throws a null pointer exception")
    void construction_missingFinishedAt_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> ShowcaseFinishedEvent.builder()
                .showcaseId(aShowcaseId())
                .build());
    }
}
