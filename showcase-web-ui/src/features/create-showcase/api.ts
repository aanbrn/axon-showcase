// SPDX-License-Identifier: MIT
import { handle, isPending, request } from '@/shared/api';
import type { ScheduleShowcaseRequest } from '@/entities/showcase/types';

const IDEMPOTENCY_KEY_HEADER = 'Idempotency-Key';

/**
 * The raw result of a schedule request.
 *
 * <p>The write either completes synchronously ("scheduled") or is accepted for asynchronous processing ("pending") and
 * must be retried with the returned idempotency key.
 */
type ScheduleShowcaseResult =
  { status: 'scheduled'; showcaseId: string } | { status: 'pending'; idempotencyKey: string | undefined };

/**
 * The reconciled outcome of scheduling a showcase.
 *
 * <p>"unknown" represents a write that never confirmed within the retry budget, distinct from a confirmed schedule so
 * the UI can tell them apart.
 */
export type ScheduleShowcaseOutcome = { status: 'scheduled'; showcaseId: string } | { status: 'unknown' };

/**
 * Schedules a new showcase via the gateway.
 *
 * <p>Re-sending the same request with the idempotency key makes the write safe to retry while it stays pending: the
 * command side deduplicates on the key.
 *
 * @param requestBody the showcase to schedule
 * @param idempotencyKey the key that makes the request idempotent when retried
 * @returns "scheduled" with the new id, or "pending" with the key to poll with
 */
export async function scheduleShowcase(
  requestBody: ScheduleShowcaseRequest,
  idempotencyKey?: string,
): Promise<ScheduleShowcaseResult> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (idempotencyKey) {
    headers[IDEMPOTENCY_KEY_HEADER] = idempotencyKey;
  }
  const response = await request('/showcases', {
    method: 'POST',
    headers,
    body: JSON.stringify(requestBody),
  });
  if (isPending(response)) {
    return {
      status: 'pending',
      idempotencyKey: response.headers.get(IDEMPOTENCY_KEY_HEADER) ?? idempotencyKey,
    };
  }
  const body = await handle<{ showcaseId: string }>(response);
  return { status: 'scheduled', showcaseId: body.showcaseId };
}
