// SPDX-License-Identifier: MIT
package showcase.api.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;

@DisplayName("Showcase event mapper unit tests")
class ShowcaseEventMapperTests {

    private final ShowcaseEventMapper mapper = new ShowcaseEventMapperImpl();

    @Test
    @DisplayName("A scheduled event maps to its DTO with the SCHEDULED type")
    void scheduledEvent_mapsToDtoWithScheduledType() {
        val event = ShowcaseScheduledEvent.builder()
                .showcaseId("1")
                .title("Demo")
                .startTime(Instant.parse("2026-09-02T10:00:00Z"))
                .duration(Duration.ofMinutes(5))
                .scheduledAt(Instant.parse("2026-09-02T09:00:00Z"))
                .build();

        assertThat(mapper.toDto(event)).satisfies(dto -> {
            assertThat(dto.type()).isEqualTo("SCHEDULED");
            assertThat(dto.showcaseId()).isEqualTo("1");
            assertThat(dto.timestamp()).isEqualTo(Instant.parse("2026-09-02T09:00:00Z"));
        });
    }

    @Test
    @DisplayName("A started event maps to its DTO with the STARTED type")
    void startedEvent_mapsToDtoWithStartedType() {
        val event = ShowcaseStartedEvent.builder()
                .showcaseId("2")
                .duration(Duration.ofMinutes(5))
                .startedAt(Instant.parse("2026-09-02T10:05:00Z"))
                .build();

        assertThat(mapper.toDto(event)).satisfies(dto -> {
            assertThat(dto.type()).isEqualTo("STARTED");
            assertThat(dto.showcaseId()).isEqualTo("2");
            assertThat(dto.timestamp()).isEqualTo(Instant.parse("2026-09-02T10:05:00Z"));
        });
    }

    @Test
    @DisplayName("A finished event maps to its DTO with the FINISHED type")
    void finishedEvent_mapsToDtoWithFinishedType() {
        val event = ShowcaseFinishedEvent.builder()
                .showcaseId("3")
                .finishedAt(Instant.parse("2026-09-02T10:10:00Z"))
                .build();

        assertThat(mapper.toDto(event)).satisfies(dto -> {
            assertThat(dto.type()).isEqualTo("FINISHED");
            assertThat(dto.showcaseId()).isEqualTo("3");
            assertThat(dto.timestamp()).isEqualTo(Instant.parse("2026-09-02T10:10:00Z"));
        });
    }

    @Test
    @DisplayName("A removed event maps to its DTO with the REMOVED type")
    void removedEvent_mapsToDtoWithRemovedType() {
        val event = ShowcaseRemovedEvent.builder()
                .showcaseId("4")
                .removedAt(Instant.parse("2026-09-02T10:11:00Z"))
                .build();

        assertThat(mapper.toDto(event)).satisfies(dto -> {
            assertThat(dto.type()).isEqualTo("REMOVED");
            assertThat(dto.showcaseId()).isEqualTo("4");
            assertThat(dto.timestamp()).isEqualTo(Instant.parse("2026-09-02T10:11:00Z"));
        });
    }
}
