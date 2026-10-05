// SPDX-License-Identifier: MIT
import { useEffect, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useLiveEvents } from '@/entities/showcase-event/@x/showcase';
import { fetchShowcases } from './api';
import { SHOWCASES_QUERY_KEY } from './query-keys';
import { predicateForEvent } from './lib/eventPredicate';
import { ReconciliationController } from './lib/reconciliation';

/**
 * Reconciles the showcase list with the read model as live events arrive.
 *
 * <p>Consumes the events received over the live stream and drives a single debounced, coalesced refetch of the list
 * until every pending event's effect is visible, so the list does not briefly show stale state. Events that occurred
 * before the stream was opened (history replayed on connect) do not trigger a reconciliation.
 */
export function useShowcaseReconciliation(): void {
  const queryClient = useQueryClient();
  const liveEvents = useLiveEvents();
  const [controller] = useState(
    () =>
      new ReconciliationController({
        refresh: () => queryClient.query({ queryKey: SHOWCASES_QUERY_KEY, queryFn: () => fetchShowcases() }),
      }),
  );
  const connectedAt = useRef(new Date());
  const processedCount = useRef(0);

  useEffect(() => {
    const received = liveEvents.slice(processedCount.current);
    processedCount.current = liveEvents.length;
    for (const event of received) {
      if (new Date(event.timestamp) > connectedAt.current) {
        controller.reconcile(event.showcaseId, predicateForEvent(event));
      }
    }
  }, [liveEvents, controller]);
}
