// SPDX-License-Identifier: MIT
import { useMutation } from '@tanstack/react-query';
import { retryUntilCompleted } from '@/shared/retry';
import type { ScheduleShowcaseRequest } from '@/entities/showcase/types';
import { scheduleShowcase, type ScheduleShowcaseOutcome } from './api';

/**
 * Schedules a showcase, retrying with the idempotency key while the write stays pending.
 *
 * <p>If the first attempt returns 202 Accepted, re-sends the same request with the idempotency key until it is
 * confirmed or the retry budget is exhausted.
 *
 * @param request the showcase to schedule
 * @returns "scheduled" with the id, or "unknown" when the retry budget was exhausted
 */
async function scheduleWithRetry(request: ScheduleShowcaseRequest): Promise<ScheduleShowcaseOutcome> {
  const first = await scheduleShowcase(request);
  if (first.status === 'scheduled') {
    return { status: 'scheduled', showcaseId: first.showcaseId };
  }
  const key = first.idempotencyKey;
  if (!key) {
    return { status: 'unknown' };
  }
  let scheduledShowcaseId: string | undefined;
  const confirmed = await retryUntilCompleted(async () => {
    const outcome = await scheduleShowcase(request, key);
    if (outcome.status === 'scheduled') {
      scheduledShowcaseId = outcome.showcaseId;
      return 'done';
    }
    return 'retry';
  });
  return confirmed && scheduledShowcaseId
    ? { status: 'scheduled', showcaseId: scheduledShowcaseId }
    : { status: 'unknown' };
}

/**
 * Mutation that schedules a showcase.
 *
 * <p>The write-side confirmation only; the list is reconciled by the live event stream (SSE) when the showcase's
 * SCHEDULED event arrives, which also covers saga-triggered transitions.
 */
export function useCreateShowcase() {
  return useMutation({
    mutationFn: scheduleWithRetry,
  });
}
