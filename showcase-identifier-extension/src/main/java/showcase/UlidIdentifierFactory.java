package showcase;

import org.axonframework.common.IdentifierFactory;
import ulid4j.Ulid;

public final class UlidIdentifierFactory extends IdentifierFactory {

    private final Ulid ulid = new Ulid();

    @Override
    public String generateIdentifier() {
        return ulid.next();
    }
}
