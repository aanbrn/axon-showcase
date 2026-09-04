// SPDX-License-Identifier: MIT
package showcase.api.events;

import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;

/**
 * Maps showcase domain events to their SSE DTOs.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@AnnotateWith(NullUnmarked.class)
@SuppressWarnings("MapstructReferenceInspection")
interface ShowcaseEventMapper {
    /**
     * Dispatches a domain event to its per-type DTO mapping.
     *
     * @param event the domain event
     * @return the DTO describing the event
     */
    default ShowcaseEventDto toDto(ShowcaseEvent event) {
        return switch (event) {
            case ShowcaseScheduledEvent scheduledEvent -> scheduledToDto(scheduledEvent);
            case ShowcaseStartedEvent startedEvent -> startedToDto(startedEvent);
            case ShowcaseFinishedEvent finishedEvent -> finishedToDto(finishedEvent);
            case ShowcaseRemovedEvent removedEvent -> removedToDto(removedEvent);
        };
    }
    /**
     * Maps a scheduled event to its DTO.
     *
     * @param event the scheduled event
     * @return the DTO describing the event
     */
    @Mapping(target = "type", constant = "SCHEDULED")
    @Mapping(target = "timestamp", source = "scheduledAt")
    ShowcaseEventDto scheduledToDto(@Nullable ShowcaseScheduledEvent event);

    /**
     * Maps a started event to its DTO.
     *
     * @param event the started event
     * @return the DTO describing the event
     */
    @Mapping(target = "type", constant = "STARTED")
    @Mapping(target = "timestamp", source = "startedAt")
    ShowcaseEventDto startedToDto(@Nullable ShowcaseStartedEvent event);

    /**
     * Maps a finished event to its DTO.
     *
     * @param event the finished event
     * @return the DTO describing the event
     */
    @Mapping(target = "type", constant = "FINISHED")
    @Mapping(target = "timestamp", source = "finishedAt")
    ShowcaseEventDto finishedToDto(@Nullable ShowcaseFinishedEvent event);

    /**
     * Maps a removed event to its DTO.
     *
     * @param event the removed event
     * @return the DTO describing the event
     */
    @Mapping(target = "type", constant = "REMOVED")
    @Mapping(target = "timestamp", source = "removedAt")
    ShowcaseEventDto removedToDto(@Nullable ShowcaseRemovedEvent event);
}
