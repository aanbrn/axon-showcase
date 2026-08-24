// SPDX-License-Identifier: MIT
package showcase.identifier;

import static com.github.ksuid.Ksuid.newKsuid;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.IdentifierFactory;

/**
 * Axon {@link IdentifierFactory} generating KSUID identifiers.
 */
@Slf4j
public final class KsuidIdentifierFactory extends IdentifierFactory {
    /**
     * Generates a new KSUID identifier.
     *
     * @return a string representation of the generated KSUID
     */
    @Override
    public String generateIdentifier() {
        return newKsuid().toString();
    }
}
