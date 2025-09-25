package showcase.query;

import lombok.val;
import org.axonframework.queryhandling.QueryExecutionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static showcase.query.RandomQueryTestUtils.aShowcaseQueryErrorDetails;

class ShowcaseQueryExceptionTests {

    @Test
    void construction_errorDetailsOnly_createsInstanceWithErrorDetailsAndNoCause() {
        val errorDetails = aShowcaseQueryErrorDetails();

        val ex = new ShowcaseQueryException(errorDetails);
        assertThat(ex).isInstanceOf(QueryExecutionException.class);
        assertThat(ex.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @SuppressWarnings({ "DataFlowIssue", "ThrowableNotThrown" })
    void construction_nullErrorDetailsOnly_throwsNullPointerException() {
        assertThatThrownBy(() -> new ShowcaseQueryException(null)).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_errorDetailsAndCause_createsInstanceWithErrorDetailsAndCause() {
        val errorDetails = aShowcaseQueryErrorDetails();
        val cause = new IllegalStateException();

        val ex = new ShowcaseQueryException(errorDetails, cause);
        assertThat(ex).isInstanceOf(QueryExecutionException.class);
        assertThat(ex.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @SuppressWarnings({ "DataFlowIssue", "ThrowableNotThrown" })
    void construction_nullErrorDetailsAndNonNullCause_throwsNullPointerException() {
        assertThatThrownBy(() -> new ShowcaseQueryException(null, new IllegalStateException()))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void construction_nonNullErrorDetailsAndNullCause_createsInstanceWithErrorDetailsAndNoCause() {
        val errorDetails = aShowcaseQueryErrorDetails();

        val ex = new ShowcaseQueryException(errorDetails, null);
        assertThat(ex).isInstanceOf(QueryExecutionException.class);
        assertThat(ex.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(ex.getCause()).isNull();
    }
}
