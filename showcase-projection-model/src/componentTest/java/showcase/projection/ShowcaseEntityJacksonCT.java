// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Showcase entity Jackson component tests")
class ShowcaseEntityJacksonCT {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @ParameterizedTest
    @MethodSource("lifecycleStates")
    @DisplayName("Round-tripping a showcase entity in any lifecycle state preserves every field")
    void lifecycleStateRoundTrip(ShowcaseEntity original) throws Exception {
        val restored = objectMapper.readValue(objectMapper.writeValueAsString(original), ShowcaseEntity.class);

        assertThat(restored).isEqualTo(original);
    }

    static List<Arguments> lifecycleStates() {
        val startTime = Instant.parse("2026-08-14T09:30:00.123456789Z");
        val duration = Duration.ofMinutes(95);
        val scheduledAt = Instant.parse("2026-08-14T08:00:00Z");
        val startedAt = Instant.parse("2026-08-14T09:30:00.123456789Z");
        val finishedAt = Instant.parse("2026-08-14T11:05:00Z");
        val showcaseId = "23R4A8S6J1B2K3N4";
        val title = "The Night of the Kite";

        return List.of(
                argumentSet(
                        "Scheduled showcase",
                        ShowcaseEntity.builder()
                                .showcaseId(showcaseId)
                                .title(title)
                                .startTime(startTime)
                                .duration(duration)
                                .status(ShowcaseStatus.SCHEDULED)
                                .scheduledAt(scheduledAt)
                                .build()),
                argumentSet(
                        "Started showcase",
                        ShowcaseEntity.builder()
                                .showcaseId(showcaseId)
                                .title(title)
                                .startTime(startTime)
                                .duration(duration)
                                .status(ShowcaseStatus.STARTED)
                                .scheduledAt(scheduledAt)
                                .startedAt(startedAt)
                                .build()),
                argumentSet(
                        "Finished showcase",
                        ShowcaseEntity.builder()
                                .showcaseId(showcaseId)
                                .title(title)
                                .startTime(startTime)
                                .duration(duration)
                                .status(ShowcaseStatus.FINISHED)
                                .scheduledAt(scheduledAt)
                                .startedAt(startedAt)
                                .finishedAt(finishedAt)
                                .build()));
    }

    @Test
    @DisplayName("Round-tripping a showcase entity preserves the nanosecond precision of its instants")
    void preservesNanosecondPrecision() throws Exception {
        val startTime = Instant.now().truncatedTo(ChronoUnit.NANOS);
        val startedAt = startTime.plusSeconds(15).plusNanos(987_654_321);
        val original = ShowcaseEntity.builder()
                .showcaseId("2BL8J7F4A1S0Q3R9")
                .title("The Last Lantern")
                .startTime(startTime)
                .duration(Duration.ofMinutes(30))
                .status(ShowcaseStatus.SCHEDULED)
                .scheduledAt(startTime.minus(90, ChronoUnit.MINUTES))
                .startedAt(startedAt)
                .build();

        val restored = objectMapper.readValue(objectMapper.writeValueAsString(original), ShowcaseEntity.class);

        assertThat(restored.startTime()).isEqualTo(startTime);
        assertThat(restored.startedAt()).isEqualTo(startedAt);
    }

    @Test
    @DisplayName("Round-tripping a showcase entity with unset fields preserves the nulls")
    void preservesNulls() throws Exception {
        val original = ShowcaseEntity.builder()
                .showcaseId("5MK2T7W9C4D0N1E8")
                .title(null)
                .status(null)
                .build();

        val restored = objectMapper.readValue(objectMapper.writeValueAsString(original), ShowcaseEntity.class);

        assertThat(restored).isEqualTo(original);
        assertThat(restored.title()).isNull();
        assertThat(restored.startTime()).isNull();
        assertThat(restored.duration()).isNull();
        assertThat(restored.status()).isNull();
        assertThat(restored.scheduledAt()).isNull();
        assertThat(restored.startedAt()).isNull();
        assertThat(restored.finishedAt()).isNull();
    }
}
