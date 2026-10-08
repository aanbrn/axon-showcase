// SPDX-License-Identifier: MIT
package showcase.api.events;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import showcase.api.ShowcaseApiProperties;
import showcase.command.ShowcaseEvent;

/**
 * Exposes the live showcase event stream to clients over Server-Sent Events, keeping an idle stream alive with a
 * periodic keep-alive comment.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
final class ShowcaseEventStreamController implements ShowcaseEventStreamApi {
    /**
     * The shared showcase domain-event stream.
     */
    private final Flux<ShowcaseEvent> eventReceiver;

    /**
     * The mapper converting domain events to their SSE DTOs.
     */
    private final ShowcaseEventMapper eventMapper;

    /**
     * The gateway properties, providing the keep-alive interval.
     */
    private final ShowcaseApiProperties apiProperties;

    /**
     * Streams the showcase domain events to the client as Server-Sent Events, mapping each to its DTO and interleaving
     * keep-alive comments while no event occurs.
     *
     * @return the SSE stream of showcase events
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public Flux<ServerSentEvent<ShowcaseEventDto>> stream() {
        return eventReceiver
                .map(eventMapper::toDto)
                .map(event -> ServerSentEvent.builder(event).event("showcase").build())
                .mergeWith(Flux.interval(apiProperties.getEvents().getKeepAliveInterval())
                        .map(tick -> ServerSentEvent.<ShowcaseEventDto>builder()
                                .comment("keep-alive")
                                .build()));
    }
}
