// SPDX-License-Identifier: MIT
package showcase.loadtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Baseline reference tests")
class BaselineReferenceTests {

    private static final String REFERENCE = """
            # The load-test baseline reference: the plateau's response times per read and write request.
            # A baseline run writes it; the below-knee profiles derive their thresholds from it as
            # max(floor, factor x baseline), and ignore it when its target is not theirs.
            target=http://axon-showcase-api
            recordedAt=2026-09-27T12:12:46.593961Z
            factor=5
            floorMeanMs=50
            floorP95Ms=100
            floorP99Ms=200
            FetchShowcases.meanMs=7
            FetchShowcases.p95Ms=10
            FetchShowcases.p99Ms=15
            FetchShowcase.meanMs=5
            FetchShowcase.p95Ms=8
            FetchShowcase.p99Ms=14
            """;

    @Test
    @DisplayName("Parsing a reference extracts its target, policy, and per-request figures in file order")
    void parse_extractsTargetPolicyAndFiguresInFileOrder() {
        val reference = BaselineReference.parse(REFERENCE);

        assertThat(reference.target()).isEqualTo("http://axon-showcase-api");
        assertThat(reference.recordedAt()).isEqualTo(Instant.parse("2026-09-27T12:12:46.593961Z"));
        assertThat(reference.factor()).isEqualTo(5);
        assertThat(reference.floorMeanMs()).isEqualTo(50);
        assertThat(reference.floorP95Ms()).isEqualTo(100);
        assertThat(reference.floorP99Ms()).isEqualTo(200);
        assertThat(reference.requests().keySet()).containsExactly("FetchShowcases", "FetchShowcase");
        assertThat(reference.requests().get("FetchShowcases").meanMs()).isEqualTo(7);
        assertThat(reference.requests().get("FetchShowcases").p95Ms()).isEqualTo(10);
        assertThat(reference.requests().get("FetchShowcases").p99Ms()).isEqualTo(15);
        assertThat(reference.requests().get("FetchShowcase").meanMs()).isEqualTo(5);
        assertThat(reference.requests().get("FetchShowcase").p95Ms()).isEqualTo(8);
        assertThat(reference.requests().get("FetchShowcase").p99Ms()).isEqualTo(14);
    }

    @Test
    @DisplayName("Rendering a parsed reference reproduces the recorded reference text")
    void render_reproducesTheRecordedReferenceText() {
        assertThat(BaselineReference.parse(REFERENCE).render()).isEqualTo(REFERENCE);
    }

    @Test
    @DisplayName("A reference that omits a request figure parses with that figure absent")
    void parse_aMissingFigure_leavesItAbsent() {
        val reference = BaselineReference.parse("""
                target=http://axon-showcase-api
                FetchShowcases.meanMs=7
                FetchShowcases.p95Ms=10
                """);

        assertThat(reference.requests().get("FetchShowcases").meanMs()).isEqualTo(7);
        assertThat(reference.requests().get("FetchShowcases").p95Ms()).isEqualTo(10);
        assertThat(reference.requests().get("FetchShowcases").p99Ms()).isNull();
    }
}
