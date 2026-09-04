// SPDX-License-Identifier: MIT
package showcase.api.events;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Exposes the live showcase event stream to clients over Server-Sent Events.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@NullMarked
final class ShowcaseEventStreamController implements ShowcaseEventStreamApi {
    /**
     * The live showcase event stream.
     */
    private final Flux<ShowcaseEventDto> showcaseEventStream;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public Flux<ServerSentEvent<ShowcaseEventDto>> stream() {
        return showcaseEventStream.map(
                event -> ServerSentEvent.builder(event).event("showcase").build());
    }
}
