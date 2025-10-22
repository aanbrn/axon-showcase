package showcase.command;

import org.axonframework.commandhandling.RoutingKey;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.io.Serializable;

public sealed interface ShowcaseCommand
        extends Serializable
        permits ScheduleShowcaseCommand,
                StartShowcaseCommand,
                FinishShowcaseCommand,
                RemoveShowcaseCommand {

    @TargetAggregateIdentifier
    @RoutingKey
    String showcaseId();
}
