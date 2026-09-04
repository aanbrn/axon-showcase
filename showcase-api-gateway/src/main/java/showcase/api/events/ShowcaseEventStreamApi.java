// SPDX-License-Identifier: MIT
package showcase.api.events;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * The live showcase event stream over Server-Sent Events.
 *
 * <p>Streams showcase domain events as they are published to Kafka, so clients can observe the event flow live
 * without reaching into the write side.
 */
@Tag(name = "Showcase Live Events")
@SuppressWarnings("unused")
interface ShowcaseEventStreamApi {
    /**
     * Streams showcase domain events as Server-Sent Events.
     *
     * @return a never-ending stream of showcase events
     */
    @Operation(
            description = "Streams showcase domain events over Server-Sent Events as they occur.",
            method = "GET",
            responses =
                    @ApiResponse(
                            responseCode = "200",
                            description = "The live stream of showcase domain events.",
                            content =
                                    @Content(
                                            mediaType = TEXT_EVENT_STREAM_VALUE,
                                            schema = @Schema(implementation = ShowcaseEventDto.class))))
    Flux<ServerSentEvent<ShowcaseEventDto>> stream();
}
