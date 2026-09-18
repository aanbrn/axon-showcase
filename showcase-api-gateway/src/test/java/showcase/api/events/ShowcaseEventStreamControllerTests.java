// SPDX-License-Identifier: MIT
package showcase.api.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import showcase.api.ShowcaseApiProperties;

@DisplayName("Showcase event stream controller unit tests")
class ShowcaseEventStreamControllerTests {

    @Test
    @DisplayName("A showcase event is wrapped as a named Server-Sent Event")
    void showcaseEvent_isWrappedAsNamedServerSentEvent() {
        val event = ShowcaseEventDto.builder()
                .type("STARTED")
                .showcaseId("1")
                .timestamp(Instant.parse("2026-09-02T10:05:00Z"))
                .build();
        val controller = controllerFor(Flux.just(event));

        val frames = controller.stream().take(1).collectList().block();
        assertThat(frames).hasSize(1);
        val sse = frames.getFirst();
        assertThat(sse.event()).isEqualTo("showcase");
        val data = sse.data();
        assertThat(data).isNotNull();
        assertThat(data).isEqualTo(event);
    }

    @Test
    @DisplayName("A removed showcase event is mapped with its type preserved")
    void removedShowcaseEvent_isMappedWithTypePreserved() {
        val event = ShowcaseEventDto.builder()
                .type("REMOVED")
                .showcaseId("1")
                .timestamp(Instant.parse("2026-09-02T10:11:00Z"))
                .build();
        val controller = controllerFor(Flux.just(event));

        val frames = controller.stream().take(1).collectList().block();
        assertThat(frames).hasSize(1);
        val sse = frames.getFirst();
        assertThat(sse.event()).isEqualTo("showcase");
        val data = sse.data();
        assertThat(data).isNotNull();
        assertThat(data.type()).isEqualTo("REMOVED");
        assertThat(data.showcaseId()).isEqualTo("1");
    }

    @Test
    @DisplayName("An idle stream emits a keep-alive comment that carries no event")
    void stream_whenIdle_emitsKeepAliveComment() {
        val frame = controllerFor(Flux.never()).stream().blockFirst(Duration.ofSeconds(5));

        assertThat(frame).isNotNull();
        assertThat(frame.comment()).isEqualTo("keep-alive");
        assertThat(frame).matches(keepAlive -> keepAlive.data() == null);
    }

    private static ShowcaseEventStreamController controllerFor(Flux<ShowcaseEventDto> showcaseEventStream) {
        val apiProperties = new ShowcaseApiProperties();
        apiProperties.getEvents().setKeepAliveInterval(Duration.ofMillis(100));
        return new ShowcaseEventStreamController(showcaseEventStream, apiProperties);
    }
}
