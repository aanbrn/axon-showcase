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
import showcase.api.ShowcaseApiProperties;

/**
 * Exposes the live showcase event stream to clients over Server-Sent Events, keeping an idle stream alive with a
 * periodic keep-alive comment.
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

    /**
     * The gateway properties, providing the keep-alive interval.
     */
    private final ShowcaseApiProperties apiProperties;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public Flux<ServerSentEvent<ShowcaseEventDto>> stream() {
        return showcaseEventStream
                .map(event -> ServerSentEvent.builder(event).event("showcase").build())
                .mergeWith(Flux.interval(apiProperties.getEvents().getKeepAliveInterval())
                        .map(tick -> ServerSentEvent.<ShowcaseEventDto>builder()
                                .comment("keep-alive")
                                .build()));
    }
}
