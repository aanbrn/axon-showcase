// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;

@DisplayName("Showcase projector bulk requests")
class ShowcaseProjectorTests {

    @Test
    @DisplayName("The bulk request refreshes immediately so a projected write is searchable on completion")
    void bulkRequest_refreshesImmediately() {
        var operations = List.of(
                BulkOperation.of(
                        operation -> operation.delete(request -> request.id("1").index("showcases"))),
                BulkOperation.of(
                        operation -> operation.delete(request -> request.id("2").index("showcases"))));

        var request = ShowcaseProjector.bulkRequest(operations);

        assertThat(request.refresh()).isEqualTo(Refresh.True);
        assertThat(request.operations()).hasSize(2);
    }
}
