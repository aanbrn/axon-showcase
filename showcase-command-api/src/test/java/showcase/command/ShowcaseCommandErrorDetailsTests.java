package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static showcase.command.RandomCommandTestUtils.aShowcaseCommandErrorCode;
import static showcase.command.RandomCommandTestUtils.aShowcaseCommandErrorMessage;

class ShowcaseCommandErrorDetailsTests {

    @Test
    void construction_allParamsSpecified_createsInstanceWithAllFieldsSet() {
        val errorCode = aShowcaseCommandErrorCode();
        val errorMessage = aShowcaseCommandErrorMessage();

        val errorDetails =
                ShowcaseCommandErrorDetails
                        .builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build();
        assertThat(errorDetails).isNotNull();
        assertThat(errorDetails.errorCode()).isEqualTo(errorCode);
        assertThat(errorDetails.errorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void construction_missingErrorCode_throwsNullPointerException() {
        assertThatThrownBy(
                () -> ShowcaseCommandErrorDetails
                              .builder()
                              .errorMessage(aShowcaseCommandErrorMessage())
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_missingErrorMessage_throwsNullPointerException() {
        assertThatThrownBy(
                () -> ShowcaseCommandErrorDetails
                              .builder()
                              .errorCode(aShowcaseCommandErrorCode())
                              .build()
        ).isExactlyInstanceOf(NullPointerException.class);
    }
}
