package showcase.command;

import lombok.val;
import org.axonframework.commandhandling.CommandExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.command.RandomCommandTestUtils.aShowcaseCommandErrorDetails;

@DisplayName("Showcase command exception tests")
class ShowcaseCommandExceptionTests {

    @Test
    @DisplayName("An exception with error details creates an instance with error details and no cause")
    void construction_errorDetails_createsInstanceWithErrorDetailsAndNoCause() {
        val errorDetails = aShowcaseCommandErrorDetails();

        val e = new ShowcaseCommandException(errorDetails);
        assertThat(e).isInstanceOf(CommandExecutionException.class);
        assertThat(e.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(e.getCause()).isNull();
    }

    @Test
    @DisplayName("An exception with error details and a cause creates an instance with both")
    void construction_errorDetailsAndCause_createsInstanceWithErrorDetailsAndCause() {
        val errorDetails = aShowcaseCommandErrorDetails();
        val cause = new IllegalStateException();

        val e = new ShowcaseCommandException(errorDetails, cause);
        assertThat(e).isInstanceOf(CommandExecutionException.class);
        assertThat(e.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(e.getCause()).isEqualTo(cause);
    }
}
