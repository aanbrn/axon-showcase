package showcase.query;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;

@DisplayName("Fetch showcase by ID query tests")
class FetchShowcaseByIdQueryTests {

    @Test
    @DisplayName("A query with all params specified creates an instance with all fields set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val showcaseId = aShowcaseId();

        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        assertThat(query).isNotNull();
        assertThat(query.showcaseId()).isEqualTo(showcaseId);
    }

    @Test
    @DisplayName("A query without a showcase ID throws a null pointer exception")
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> FetchShowcaseByIdQuery.builder().build());
    }
}
