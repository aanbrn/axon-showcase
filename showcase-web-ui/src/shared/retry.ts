// SPDX-License-Identifier: MIT
/**
 * The number of attempts before a retry loop gives up.
 *
 * <p>With the default delay this bounds a retry loop at roughly thirty seconds of retrying.
 */
const RETRY_ATTEMPTS = 15;
/** The delay between retry attempts, in milliseconds. */
const RETRY_DELAY_MS = 2000;

/**
 * Configuration for a retry loop: how many attempts and how long to wait between them.
 */
interface RetryOptions {
  attempts: number;
  delayMs: number;
}

/**
 * The default retry configuration, used when a call site does not override anything.
 */
const defaultRetryOptions: RetryOptions = {
  attempts: RETRY_ATTEMPTS,
  delayMs: RETRY_DELAY_MS,
};

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Re-invokes an attempt until it reports "done" or the retry budget is exhausted.
 *
 * <p>The attempt returns "retry" to be invoked again after the configured delay, or "done" to stop immediately. An
 * attempt that throws propagates the error rather than counting against the budget.
 *
 * @param attempt the operation to re-run; returns "done" when it succeeded, "retry" otherwise
 * @param options overrides for the retry budget (attempts and delay)
 * @returns true when an attempt reported "done", false when the budget was exhausted
 */
export async function retryUntilCompleted(
  attempt: () => Promise<'retry' | 'done'>,
  options: Partial<RetryOptions> = {},
): Promise<boolean> {
  const retry = { ...defaultRetryOptions, ...options };
  for (let i = 0; i < retry.attempts; i++) {
    if (i > 0) {
      await sleep(retry.delayMs);
    }
    const outcome = await attempt();
    if (outcome === 'done') {
      return true;
    }
  }
  return false;
}
