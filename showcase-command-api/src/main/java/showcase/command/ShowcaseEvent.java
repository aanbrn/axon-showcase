package showcase.command;

import org.axonframework.serialization.Revision;

import java.io.Serializable;

@Revision("1.0")
public sealed interface ShowcaseEvent
        extends Serializable
        permits ShowcaseScheduledEvent,
                ShowcaseStartedEvent,
                ShowcaseFinishedEvent,
                ShowcaseRemovedEvent {

    String getShowcaseId();
}
