package showcase.query;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;

class FetchShowcaseByIdQueryTests {

    @Test
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
    void construction_missingShowcaseId_throwsNullPointerException() {
        assertThatThrownBy(() -> FetchShowcaseByIdQuery.builder().build())
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
