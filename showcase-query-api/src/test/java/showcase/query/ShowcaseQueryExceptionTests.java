package showcase.query;

import lombok.val;
import org.axonframework.queryhandling.QueryExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static showcase.query.RandomQueryTestUtils.aShowcaseQueryErrorDetails;

@DisplayName("Showcase query exception tests")
class ShowcaseQueryExceptionTests {

    @Test
    @DisplayName("An exception with error details creates an instance with error details and no cause")
    void construction_errorDetailsOnly_createsInstanceWithErrorDetailsAndNoCause() {
        val errorDetails = aShowcaseQueryErrorDetails();

        val ex = new ShowcaseQueryException(errorDetails);
        assertThat(ex).isInstanceOf(QueryExecutionException.class);
        assertThat(ex.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("An exception with error details and a cause creates an instance with both")
    void construction_errorDetailsAndCause_createsInstanceWithErrorDetailsAndCause() {
        val errorDetails = aShowcaseQueryErrorDetails();
        val cause = new IllegalStateException();

        val ex = new ShowcaseQueryException(errorDetails, cause);
        assertThat(ex).isInstanceOf(QueryExecutionException.class);
        assertThat(ex.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("An exception with non-null error details and a null cause creates an instance with no cause")
    void construction_nonNullErrorDetailsAndNullCause_createsInstanceWithErrorDetailsAndNoCause() {
        val errorDetails = aShowcaseQueryErrorDetails();

        val ex = new ShowcaseQueryException(errorDetails, null);
        assertThat(ex).isInstanceOf(QueryExecutionException.class);
        assertThat(ex.getErrorDetails()).isEqualTo(errorDetails);
        assertThat(ex.getCause()).isNull();
    }
}
