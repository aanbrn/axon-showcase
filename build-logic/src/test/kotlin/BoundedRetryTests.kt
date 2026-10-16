import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Bounded retry")
class BoundedRetryTests {

    @Test
    @DisplayName("An operation that fails transiently succeeds on a later attempt")
    fun transientFailureIsRetried() {
        var attempts = 0
        val transients = mutableListOf<String>()

        val result =
            BoundedRetry.execute(
                attempts = 3,
                pauseMillis = 0,
                onTransientFailure = { transients += it.message.orEmpty() },
            ) {
                attempts++
                check(attempts == 3) { "attempt $attempts failed" }
                "ok"
            }

        assertThat(result).isEqualTo("ok")
        assertThat(attempts).isEqualTo(3)
        assertThat(transients).containsExactly("attempt 1 failed", "attempt 2 failed")
    }

    @Test
    @DisplayName("An operation that always fails reports its last failure after the bounded attempts")
    fun persistentFailureIsReported() {
        var attempts = 0

        assertThatThrownBy {
                BoundedRetry.execute(attempts = 3, pauseMillis = 0) {
                    attempts++
                    throw IllegalStateException("attempt $attempts failed")
                }
            }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("attempt 3 failed")
        assertThat(attempts).isEqualTo(3)
    }

    @Test
    @DisplayName("A single attempt runs the operation once and never retries it")
    fun singleAttemptIsNotRetried() {
        var attempts = 0

        BoundedRetry.execute(attempts = 1, pauseMillis = 0) { attempts++ }

        assertThat(attempts).isEqualTo(1)
    }

    @Test
    @DisplayName("Fewer than one attempt is rejected")
    fun atLeastOneAttemptIsRequired() {
        assertThatThrownBy { BoundedRetry.execute(attempts = 0, pauseMillis = 0) { "ok" } }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
