package showcase.query;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import showcase.query.PageRequest.Sort;
import showcase.query.PageRequest.Sort.Direction;

import java.util.Set;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FetchShowcaseListQuery {

    String title;

    @Singular(ignoreNullCollections = true)
    Set<ShowcaseStatus> statuses;

    @NonNull
    @Valid
    @Builder.Default
    PageRequest pageRequest = PageRequest.withSort(Sort.by(Direction.DESC, "startTime"));
}
