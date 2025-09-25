package showcase.command;

import lombok.val;
import org.axonframework.commandhandling.CommandExecutionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static showcase.command.RandomCommandTestUtils.aShowcaseCommandErrorDetails;

class ShowcaseCommandExceptionTests {

    @Test
    void constructionWithErrorDetailsOnly_nonNullErrorDetails_createsInstanceWithErrorDetailsAndNoCause() {
        val errorDetails = aShowcaseCommandErrorDetails();

        val e = new ShowcaseCommandException(errorDetails);
        assertThat(e).isInstanceOf(CommandExecutionException.class);
        assertThat(e.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(e.getCause()).isNull();
    }

    @Test
    @SuppressWarnings({ "DataFlowIssue", "ThrowableNotThrown" })
    void constructionWithErrorDetailsOnly_nullErrorDetails_throwsNullPointerException() {
        assertThatThrownBy(() -> new ShowcaseCommandException(null)).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void constructionWithErrorDetailsAndCause_nonNullBoth_createsInstanceWithErrorDetailsAndCause() {
        val errorDetails = aShowcaseCommandErrorDetails();
        val cause = new IllegalStateException();

        val e = new ShowcaseCommandException(errorDetails, cause);
        assertThat(e).isInstanceOf(CommandExecutionException.class);
        assertThat(e.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(e.getCause()).isEqualTo(cause);
    }

    @Test
    @SuppressWarnings({ "DataFlowIssue", "ThrowableNotThrown" })
    void constructionWithErrorDetailsAndCause_nullErrorDetailsOnly_throwsNullPointerException() {
        assertThatThrownBy(() -> new ShowcaseCommandException(null, new IllegalStateException()))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void constructionWithErrorDetailsCause_nullCauseOnly_createsInstanceWithErrorDetailsAndNoCause() {
        val errorDetails = aShowcaseCommandErrorDetails();

        val e = new ShowcaseCommandException(errorDetails, null);
        assertThat(e).isInstanceOf(CommandExecutionException.class);
        assertThat(e.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(e.getCause()).isNull();
    }
}
