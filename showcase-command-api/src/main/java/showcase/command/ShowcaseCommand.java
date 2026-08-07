package showcase.command;

import org.axonframework.commandhandling.RoutingKey;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.io.Serializable;

/**
 * A command targeting a single showcase aggregate.
 */
public sealed interface ShowcaseCommand
        extends Serializable
        permits ScheduleShowcaseCommand,
                StartShowcaseCommand,
                FinishShowcaseCommand,
                RemoveShowcaseCommand {
    /**
     * Returns the ID of the showcase this command targets.
     *
     * @return the showcase ID
     */
    @TargetAggregateIdentifier
    @RoutingKey
    String showcaseId();
}
