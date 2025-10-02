package showcase.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Schema(description = "Response payload on schedule a showcase.")
@SuppressWarnings("ClassCanBeRecord")
class ScheduleShowcaseResponse {

    @NotBlank
    @Schema(
            description = "The ID of the scheduled showcase.",
            example = "33gkCN0UNn3Kzr3x7iuDaVT6sZi"
    )
    String showcaseId;
}
