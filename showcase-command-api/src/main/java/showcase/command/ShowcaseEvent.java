// SPDX-License-Identifier: MIT
package showcase.command;

import java.io.Serializable;
import org.axonframework.serialization.Revision;

/**
 * An event reflecting a state change of a showcase aggregate.
 */
@Revision("1.0")
public sealed interface ShowcaseEvent extends Serializable
        permits ShowcaseScheduledEvent, ShowcaseStartedEvent, ShowcaseFinishedEvent, ShowcaseRemovedEvent {
    /**
     * Returns the ID of the showcase this event refers to.
     *
     * @return the showcase ID
     */
    String showcaseId();
}
