package showcase;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.IdentifierFactory;

import static com.github.ksuid.Ksuid.newKsuid;

@Slf4j
public final class KsuidIdentifierFactory extends IdentifierFactory {

    @Override
    public String generateIdentifier() {
        return newKsuid().toString();
    }
}
