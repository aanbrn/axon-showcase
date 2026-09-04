// SPDX-License-Identifier: MIT
import { useMutation } from '@tanstack/react-query';
import { retryUntilCompleted } from '@/shared/retry';
import {
  finishShowcase,
  removeShowcase,
  startShowcase,
  type ShowcaseActionOutcome,
  type ShowcaseActionResult,
} from './api';

/**
 * Runs a lifecycle action, retrying while the write stays pending.
 *
 * <p>If the first attempt returns 202 Accepted, re-invokes the action until it is confirmed or the retry budget is
 * exhausted.
 *
 * @param action the operation to re-run
 * @returns "done" when the action completed, "unknown" when the retry budget was exhausted
 */
async function actionWithRetry(action: () => Promise<ShowcaseActionResult>): Promise<ShowcaseActionOutcome> {
  const first = await action();
  if (first.status === 'done') {
    return { status: 'done' };
  }
  const confirmed = await retryUntilCompleted(async () => {
    const outcome = await action();
    return outcome.status === 'done' ? 'done' : 'retry';
  });
  return confirmed ? { status: 'done' } : { status: 'unknown' };
}

/**
 * Mutation that starts a showcase.
 *
 * <p>The write-side confirmation only; the list is reconciled by the live event stream (SSE) when the STARTED event
 * arrives.
 */
export function useStartShowcase() {
  return useMutation({
    mutationFn: (showcaseId: string) => actionWithRetry(() => startShowcase(showcaseId)),
  });
}

/**
 * Mutation that finishes a showcase.
 *
 * <p>The write-side confirmation only; the list is reconciled by the live event stream (SSE) when the FINISHED event
 * arrives.
 */
export function useFinishShowcase() {
  return useMutation({
    mutationFn: (showcaseId: string) => actionWithRetry(() => finishShowcase(showcaseId)),
  });
}

/**
 * Mutation that removes a showcase.
 *
 * <p>The write-side confirmation only; the list is reconciled by the live event stream (SSE) when the REMOVED event
 * arrives.
 */
export function useRemoveShowcase() {
  return useMutation({
    mutationFn: (showcaseId: string) => actionWithRetry(() => removeShowcase(showcaseId)),
  });
}
