package showcase.projection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.Setting.SortOrder;

import java.time.Duration;
import java.time.Instant;

@Document(indexName = "showcases")
@Setting(sortFields = "showcaseId", sortOrders = SortOrder.desc)
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder(toBuilder = true)
@Jacksonized
public class ShowcaseEntity {

    @Id
    @Field(type = FieldType.Keyword)
    @Nullable
    String showcaseId;

    @Field(type = FieldType.Text)
    @Nullable
    String title;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant startTime;

    @Nullable
    Duration duration;

    @Field(type = FieldType.Keyword)
    @Nullable
    ShowcaseStatus status;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant scheduledAt;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant startedAt;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant finishedAt;
}
