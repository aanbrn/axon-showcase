// SPDX-License-Identifier: MIT
package showcase.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;

import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Showcase removed event tests")
class ShowcaseRemovedEventTests {

    @Test
    @DisplayName("An event with all params specified creates an instance with all fields set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();

        val event = ShowcaseRemovedEvent.builder()
                .showcaseId(showcaseId)
                .removedAt(Instant.now())
                .build();
        assertThat(event).isNotNull();
        assertThat(event.showcaseId()).isEqualTo(showcaseId);
    }

    @Test
    @DisplayName("An event without a showcase ID throws a null pointer exception")
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() ->
                        ShowcaseRemovedEvent.builder().removedAt(Instant.now()).build());
    }

    @Test
    @DisplayName("An event without a removed-at time throws a null pointer exception")
    void construction_missingRemovedAt_throwsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() ->
                        ShowcaseRemovedEvent.builder().showcaseId(aShowcaseId()).build());
    }
}
