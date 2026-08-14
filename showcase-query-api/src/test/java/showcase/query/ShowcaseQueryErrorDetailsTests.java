package showcase.query;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static showcase.query.RandomQueryTestUtils.aShowcaseQueryErrorCode;
import static showcase.query.RandomQueryTestUtils.aShowcaseQueryErrorMessage;

@DisplayName("Showcase query error details tests")
class ShowcaseQueryErrorDetailsTests {

    @Test
    @DisplayName("Error details with all params specified create an instance with all fields set")
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val errorCode = aShowcaseQueryErrorCode();
        val errorMessage = aShowcaseQueryErrorMessage();

        val errorDetails =
                ShowcaseQueryErrorDetails
                        .builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build();
        assertThat(errorDetails).isNotNull();
        assertThat(errorDetails.errorCode()).isEqualTo(errorCode);
        assertThat(errorDetails.errorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("Error details without an error code throw a null pointer exception")
    void construction_missingErrorCode_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseQueryErrorDetails
                              .builder()
                              .errorMessage(aShowcaseQueryErrorMessage())
                              .build());
    }

    @Test
    @DisplayName("Error details without an error message throw a null pointer exception")
    void construction_missingErrorMessage_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(
                () -> ShowcaseQueryErrorDetails
                              .builder()
                              .errorCode(aShowcaseQueryErrorCode())
                              .build());
    }
}
