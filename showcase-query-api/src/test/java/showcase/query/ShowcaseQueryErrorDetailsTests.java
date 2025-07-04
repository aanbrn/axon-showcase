package showcase.query;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static showcase.query.RandomQueryTestUtils.aShowcaseQueryErrorCode;
import static showcase.query.RandomQueryTestUtils.aShowcaseQueryErrorMessage;

class ShowcaseQueryErrorDetailsTests {

    @Test
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
        assertThat(errorDetails.getErrorCode()).isEqualTo(errorCode);
        assertThat(errorDetails.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void construction_missingErrorCode_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseQueryErrorDetails
                        .builder()
                        .errorMessage(aShowcaseQueryErrorMessage())
                        .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingErrorMessage_throwsNullPointerException() {
        assertThatCode(
                () -> ShowcaseQueryErrorDetails
                        .builder()
                        .errorCode(aShowcaseQueryErrorCode())
                        .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }
}
