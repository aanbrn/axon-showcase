package showcase.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import showcase.ULID;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Schema(description = "Response payload on schedule a showcase.")
@SuppressWarnings("ClassCanBeRecord")
class ScheduleShowcaseResponse {

    @NotBlank
    @ULID
    @Schema(
            description = "The ID of the scheduled showcase.",
            example = "01K364AM7WRYFKTHMNE8ABAW3Q"
    )
    String showcaseId;
}
