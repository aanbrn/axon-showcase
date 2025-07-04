package showcase.command;

import org.axonframework.commandhandling.RoutingKey;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.io.Serializable;

public interface ShowcaseCommand extends Serializable {

    @TargetAggregateIdentifier
    @RoutingKey
    String getShowcaseId();
}
