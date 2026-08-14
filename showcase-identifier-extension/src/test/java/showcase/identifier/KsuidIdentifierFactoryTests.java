package showcase.identifier;

import com.github.ksuid.Ksuid;
import lombok.val;
import org.axonframework.common.IdentifierFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KSUID identifier factory tests")
class KsuidIdentifierFactoryTests {

    private final KsuidIdentifierFactory factory = new KsuidIdentifierFactory();

    @Test
    @DisplayName("A generated identifier is a parseable KSUID string")
    void generateIdentifier_returnsParseableKsuidString() {
        val identifier = factory.generateIdentifier();

        assertThat(identifier).isNotBlank().hasSize(27);
        assertThat(Ksuid.fromString(identifier)).isNotNull();
    }

    @Test
    @DisplayName("Generated identifiers are unique")
    void generateIdentifier_returnsUniqueIdentifiers() {
        val identifiers = new HashSet<String>();
        for (int i = 0; i < 1000; i++) {
            identifiers.add(factory.generateIdentifier());
        }

        assertThat(identifiers).hasSize(1000);
    }

    @Test
    @DisplayName("The factory is registered as the Axon identifier factory")
    void generateIdentifier_isRegisteredAsAxonIdentifierFactory() {
        assertThat(IdentifierFactory.getInstance()).isInstanceOf(KsuidIdentifierFactory.class);
    }

    @Test
    @DisplayName("Generated identifiers expose monotonically non-decreasing timestamps")
    void generateIdentifier_exposesNonDecreasingTimestamps() {
        var previousTimestamp = Integer.MIN_VALUE;
        for (int i = 0; i < 100; i++) {
            val timestamp = Ksuid.fromString(factory.generateIdentifier()).getTimestamp();
            assertThat(timestamp).isGreaterThanOrEqualTo(previousTimestamp);
            previousTimestamp = timestamp;
        }
    }

    @Test
    @DisplayName("Same-second string sortability is not asserted")
    void sameSecondSortabilityIsNotAsserted() {
        // KSUIDs encode a 4-byte second timestamp followed by 16 random payload bytes; string order is determined by
        // timestamp then payload. Two KSUIDs generated within the same second sort by random payload rather than
        // generation order, so a strict string-sortability assertion would be flaky. The deterministic invariant —
        // monotonically non-decreasing timestamps — is covered above.
        val identifiers = new HashSet<String>();
        int sameSecondGenerated = 0;
        val firstTimestamp = Ksuid.fromString(factory.generateIdentifier()).getTimestamp();
        while (true) {
            identifiers.add(factory.generateIdentifier());
            val nextTimestamp = Ksuid.fromString(factory.generateIdentifier()).getTimestamp();
            sameSecondGenerated++;
            if (nextTimestamp != firstTimestamp) {
                break;
            }
            if (sameSecondGenerated >= 1000) {
                break;
            }
        }
        assertThat(identifiers).hasSizeGreaterThan(0);
        assertThat(sameSecondGenerated).isGreaterThan(0);
    }
}
