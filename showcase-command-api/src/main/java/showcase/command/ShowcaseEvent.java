package showcase.command;

import org.axonframework.serialization.Revision;

import java.io.Serializable;

@Revision("1.0")
public interface ShowcaseEvent extends Serializable {

    String getShowcaseId();
}
