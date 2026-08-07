package showcase.command;

import org.axonframework.serialization.Revision;

import java.io.Serializable;

/**
 * An event reflecting a state change of a showcase aggregate.
 */
@Revision("1.0")
public sealed interface ShowcaseEvent
        extends Serializable
        permits ShowcaseScheduledEvent,
                ShowcaseStartedEvent,
                ShowcaseFinishedEvent,
                ShowcaseRemovedEvent {
    /**
     * Returns the ID of the showcase this event refers to.
     *
     * @return the showcase ID
     */
    String showcaseId();
}
