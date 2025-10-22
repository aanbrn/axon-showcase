package showcase.query;

import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;

class FetchShowcaseListQueryTests {

    @Test
    void construction_defaults_createsInstanceWithDefaultFieldValues() {
        val query = FetchShowcaseListQuery.builder().build();

        assertThat(query).isNotNull();
        assertThat(query.title()).isNull();
        assertThat(query.statuses()).isEmpty();
        assertThat(query.afterId()).isNull();
        assertThat(query.size()).isEqualTo(FetchShowcaseListQuery.DEFAULT_SIZE);
    }

    @Test
    void construction_allFields_createsInstanceWithSpecifiedFieldValues() {
        val title = aShowcaseTitle();
        val status1 = aShowcaseStatus();
        val status2 = aShowcaseStatus(status1);
        val afterId = aShowcaseId();
        val size = RandomUtils.secure().randomInt(
                FetchShowcaseListQuery.MIN_SIZE, FetchShowcaseListQuery.MAX_SIZE + 1);

        val query =
                FetchShowcaseListQuery
                        .builder()
                        .title(title)
                        .status(status1)
                        .status(status2)
                        .afterId(afterId)
                        .size(size)
                        .build();
        assertThat(query).isNotNull();
        assertThat(query.title()).isEqualTo(title);
        assertThat(query.statuses()).containsExactly(status1, status2);
        assertThat(query.afterId()).isEqualTo(afterId);
        assertThat(query.size()).isEqualTo(size);
    }

    @Test
    void construction_duplicateStatuses_createsInstanceWithDistinctStatuses() {
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
        assertThat(query.statuses()).containsExactlyInAnyOrderElementsOf(new HashSet<>(statuses));
    }
}
