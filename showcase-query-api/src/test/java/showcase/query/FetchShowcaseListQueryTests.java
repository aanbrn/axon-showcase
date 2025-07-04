package showcase.query;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;

class FetchShowcaseListQueryTests {

    @Test
    void construction_noStatusesSpecified_createsInstanceWithEmptyStatusesSet() {
        assertThat(FetchShowcaseListQuery.builder().build()).isNotNull();
    }

    @Test
    void construction_singleStatusSpecified_createsInstanceWithSingleStatusSet() {
        val status = aShowcaseStatus();

        val query =
                FetchShowcaseListQuery
                        .builder()
                        .status(status)
                        .build();
        assertThat(query).isNotNull();
        assertThat(query.getStatuses()).containsExactly(status);
    }

    @Test
    void construction_multipleStatusesSpecified_createsInstanceWithAllStatusesSet() {
        val statuses = List.of(ShowcaseStatus.values());

        val query =
                FetchShowcaseListQuery
                        .builder()
                        .statuses(statuses)
                        .build();
        assertThat(query).isNotNull();
        assertThat(query.getStatuses()).containsExactlyInAnyOrderElementsOf(statuses);
    }

    @Test
    void construction_duplicateStatusesInParamsSpecified_createsInstanceWithDistinctStatusesSet() {
        val statuses =
                IntStream.range(0, 10)
                         .mapToObj(__ -> aShowcaseStatus())
                         .toList();

        val query =
                FetchShowcaseListQuery
                        .builder()
                        .statuses(statuses)
                        .build();
        assertThat(query).isNotNull();
        assertThat(query.getStatuses()).containsExactlyInAnyOrderElementsOf(new HashSet<>(statuses));
    }
}
