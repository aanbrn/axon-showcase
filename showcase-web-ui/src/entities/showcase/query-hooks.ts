// SPDX-License-Identifier: MIT
import { type QueryClient } from '@tanstack/react-query';
import { retryUntilCompleted } from '@/shared/retry';
import type { ShowcaseEvent } from '@/entities/showcase-event/types';
import type { ShowcaseStatus } from './types';
import { fetchShowcases } from './api';
import type { Showcase } from './types';
import { SHOWCASES_QUERY_KEY } from './query-keys';

/**
 * Per-showcase reconciliations, latest-wins.
 *
 * <p>Each live event reflects the showcase's newest state. If a reconciliation for the same showcase is already
 * running, its predicate is replaced with the newest one and the running poll is reused, so a saga burst (SCHEDULED →
 * STARTED → FINISHED) coalesces into a single poll loop instead of one per event.
 */
const pending = new Map<string, { predicate: (showcases: Showcase[]) => boolean; promise: Promise<boolean> }>();

/**
 * Polls the read model until the showcase list satisfies a predicate.
 *
 * <p>The command and query sides are eventually consistent: a write completes before the projection has updated
 * OpenSearch. This polls the list (via a forced fetch) until the predicate matches, so the UI reflects the write's
 * effect instead of racing the projection.
 *
 * @param queryClient the TanStack Query client
 * @param predicate returns true when the read model reflects the expected state
 * @returns true when the predicate was satisfied, false when the poll budget was exhausted
 */
export async function waitForReadModel(
  queryClient: QueryClient,
  predicate: (showcases: Showcase[]) => boolean,
): Promise<boolean> {
  return retryUntilCompleted(
    async () => {
      const showcases = await queryClient.query({
        queryKey: SHOWCASES_QUERY_KEY,
        queryFn: () => fetchShowcases(),
      });
      return predicate(showcases) ? 'done' : 'retry';
    },
    { attempts: 5, delayMs: 500 },
  );
}

/**
 * Reconciles the read model to a showcase's expected state, superseding any in-flight reconciliation for it.
 *
 * @param queryClient the TanStack Query client
 * @param showcaseId the id of the showcase to reconcile
 * @param predicate returns true when the read model reflects the expected state
 * @returns true when the state was observed, false when the poll budget was exhausted
 */
function reconcileShowcase(
  queryClient: QueryClient,
  showcaseId: string,
  predicate: (showcases: Showcase[]) => boolean,
): Promise<boolean> {
  const existing = pending.get(showcaseId);
  if (existing) {
    existing.predicate = predicate;
    return existing.promise;
  }
  const entry: { predicate: (showcases: Showcase[]) => boolean; promise: Promise<boolean> } = {
    predicate,
    promise: Promise.resolve(false),
  };
  entry.promise = waitForReadModel(queryClient, (showcases) => {
    const current = pending.get(showcaseId);
    return current ? current.predicate(showcases) : true;
  }).finally(() => {
    if (pending.get(showcaseId) === entry) {
      pending.delete(showcaseId);
    }
  });
  pending.set(showcaseId, entry);
  return entry.promise;
}

/**
 * Waits until the read model reflects a given domain event.
 *
 * <p>Used for events that do not originate from a local mutation, such as saga-triggered lifecycle transitions: the
 * event arrives over SSE, and the list should show its effect once the projection has caught up.
 *
 * @param queryClient the TanStack Query client
 * @param event the event whose effect should be visible in the read model
 * @returns true when the event's effect was observed, false when the poll budget was exhausted
 */
export function waitForEvent(queryClient: QueryClient, event: ShowcaseEvent): Promise<boolean> {
  const predicate = predicateForEvent(event);
  return reconcileShowcase(queryClient, event.showcaseId, predicate);
}

/**
 * Waits until the showcase appears in the read model.
 *
 * @param queryClient the TanStack Query client
 * @param showcaseId the id of the showcase to wait for
 * @returns true when the showcase appeared, false when the poll budget was exhausted
 */
export function waitForShowcasePresence(queryClient: QueryClient, showcaseId: string): Promise<boolean> {
  return reconcileShowcase(queryClient, showcaseId, (showcases) =>
    showcases.some((showcase) => showcase.showcaseId === showcaseId),
  );
}

/**
 * Waits until the showcase reaches the expected status in the read model.
 *
 * @param queryClient the TanStack Query client
 * @param showcaseId the id of the showcase to wait for
 * @param status the expected status
 * @returns true when the status was observed, false when the poll budget was exhausted
 */
export function waitForShowcaseStatus(
  queryClient: QueryClient,
  showcaseId: string,
  status: ShowcaseStatus,
): Promise<boolean> {
  return reconcileShowcase(queryClient, showcaseId, (showcases) =>
    showcases.some((showcase) => showcase.showcaseId === showcaseId && showcase.status === status),
  );
}

/**
 * Waits until the showcase disappears from the read model.
 *
 * @param queryClient the TanStack Query client
 * @param showcaseId the id of the showcase to wait for
 * @returns true when the showcase was removed, false when the poll budget was exhausted
 */
export function waitForShowcaseRemoval(queryClient: QueryClient, showcaseId: string): Promise<boolean> {
  return reconcileShowcase(
    queryClient,
    showcaseId,
    (showcases) => !showcases.some((showcase) => showcase.showcaseId === showcaseId),
  );
}

function predicateForEvent(event: ShowcaseEvent): (showcases: Showcase[]) => boolean {
  switch (event.type) {
    case 'REMOVED':
      return (showcases) => !showcases.some((showcase) => showcase.showcaseId === event.showcaseId);
    case 'SCHEDULED':
      return (showcases) => showcases.some((showcase) => showcase.showcaseId === event.showcaseId);
    case 'STARTED':
      return (showcases) =>
        showcases.some((showcase) => showcase.showcaseId === event.showcaseId && showcase.status === 'STARTED');
    case 'FINISHED':
      return (showcases) =>
        showcases.some((showcase) => showcase.showcaseId === event.showcaseId && showcase.status === 'FINISHED');
  }
}
