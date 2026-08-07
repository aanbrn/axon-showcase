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

/**
 * Elasticsearch document backing the read-side showcase projection.
 */
@Document(indexName = "showcases")
@Setting(sortFields = "showcaseId", sortOrders = SortOrder.desc)
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@EqualsAndHashCode(cacheStrategy = CacheStrategy.LAZY)
@Builder(toBuilder = true)
@Jacksonized
public class ShowcaseEntity {
    /**
     * The unique ID of the showcase.
     */
    @Id
    @Field(type = FieldType.Keyword)
    @Nullable
    String showcaseId;

    /**
     * The unique title of the showcase.
     */
    @Field(type = FieldType.Text)
    @Nullable
    String title;

    /**
     * The date-time when the showcase should be started automatically.
     */
    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant startTime;

    /**
     * The duration after which the started showcase should be finished automatically.
     */
    @Nullable
    Duration duration;

    /**
     * The current status of the showcase.
     */
    @Field(type = FieldType.Keyword)
    @Nullable
    ShowcaseStatus status;

    /**
     * The date-time when the showcase was actually scheduled.
     */
    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant scheduledAt;

    /**
     * The date-time when the showcase was actually started, if it has been started yet.
     */
    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant startedAt;

    /**
     * The date-time when the showcase was actually finished, if it has been finished yet.
     */
    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    @Nullable
    Instant finishedAt;
}
