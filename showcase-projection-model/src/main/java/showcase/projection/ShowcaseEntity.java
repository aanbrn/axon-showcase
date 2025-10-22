package showcase.projection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
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
@Builder(toBuilder = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ShowcaseEntity {

    @Id
    @Field(type = FieldType.Keyword)
    String showcaseId;

    @Field(type = FieldType.Text)
    String title;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    Instant startTime;

    Duration duration;

    @Field(type = FieldType.Keyword)
    ShowcaseStatus status;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    Instant scheduledAt;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    Instant startedAt;

    @Field(type = FieldType.Date_Nanos, format = DateFormat.strict_date_optional_time_nanos)
    Instant finishedAt;
}
