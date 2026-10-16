/**
 * Re-runs an operation that can fail transiently, a bounded number of times.
 *
 * The logic holds no Gradle state, so it is testable without invoking the tool it wraps and a task action capturing it
 * stays configuration-cache serializable.
 */
internal object BoundedRetry {

    /** The attempts a repository setup step makes before its failure is reported. */
    const val DEFAULT_ATTEMPTS = 3

    /** The pause between attempts, long enough for a transient repository failure to clear. */
    const val DEFAULT_PAUSE_MILLIS = 2_000L

    /**
     * Returns [operation]'s result, re-running it when it throws until [attempts] attempts have been made, pausing
     * [pauseMillis] between them and reporting each transient failure through [onTransientFailure]. The last attempt's
     * failure propagates, so a persistent failure is still reported.
     */
    fun <T> execute(
        attempts: Int = DEFAULT_ATTEMPTS,
        pauseMillis: Long = DEFAULT_PAUSE_MILLIS,
        onTransientFailure: (Exception) -> Unit = {},
        operation: () -> T,
    ): T {
        require(attempts >= 1) { "BoundedRetry needs at least one attempt" }
        repeat(attempts - 1) {
            try {
                return operation()
            } catch (e: Exception) {
                onTransientFailure(e)
                Thread.sleep(pauseMillis)
            }
        }
        return operation()
    }
}
