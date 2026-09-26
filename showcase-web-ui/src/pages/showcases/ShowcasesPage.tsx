// SPDX-License-Identifier: MIT
import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { connectEventStream, useEventReceived, useLiveEvents } from '@/entities/showcase-event';
import {
  mergeTimeline,
  useSelectedShowcaseId,
  useSelectShowcase,
  useShowcases,
  waitForEvent,
} from '@/entities/showcase';
import { CreateShowcaseForm, useCreateShowcase } from '@/features/create-showcase';
import { useFinishShowcase, useRemoveShowcase, useStartShowcase } from '@/features/showcase-actions';
import { ShowcaseList } from '@/widgets/showcase-list';
import { ShowcaseDetail } from '@/widgets/showcase-detail';

/**
 * The showcases page.
 *
 * <p>Composes the create form, list, and detail widgets, drives the lifecycle mutations, subscribes to the live event
 * stream, and reconciles the list with the read model after local writes and saga-triggered events so the UI stays
 * consistent with the eventually-consistent projection.
 */
export function ShowcasesPage() {
  const onEventReceived = useEventReceived();
  const queryClient = useQueryClient();
  const { data: showcases = [], isPending } = useShowcases();
  const liveEvents = useLiveEvents();
  const selectedId = useSelectedShowcaseId();
  const selectShowcase = useSelectShowcase();

  const create = useCreateShowcase();
  const start = useStartShowcase();
  const finish = useFinishShowcase();
  const remove = useRemoveShowcase();

  useEffect(() => {
    const connectedAt = new Date();
    return connectEventStream((event) => {
      onEventReceived(event);
      // The gateway replays recent history on connect; only reconcile events that arrive after the
      // connection was established, so an initial connect does not poll for already-projected history.
      if (new Date(event.timestamp) > connectedAt) {
        void waitForEvent(queryClient, event);
      }
    });
  }, [onEventReceived, queryClient]);

  const selected = showcases.find((showcase) => showcase.showcaseId === selectedId) ?? null;
  const selectedTimeline = selected ? mergeTimeline(selected, liveEvents) : [];

  return (
    <div className="app">
      <header>
        <h1>Showcase</h1>
        <p>CQRS / Event-Sourcing demo</p>
      </header>

      <CreateShowcaseForm onSubmit={(values) => create.mutate(values)} busy={create.isPending} />

      {create.isPending && <div className="notice">The showcase is still being scheduled.</div>}
      {create.data?.status === 'unknown' && <div className="notice">Could not confirm the showcase was scheduled.</div>}
      {create.isError && <div className="error">{create.error?.message ?? 'Failed to create showcase'}</div>}

      {(start.isPending || finish.isPending || remove.isPending) && (
        <div className="notice">The action is still being processed.</div>
      )}
      {start.data?.status === 'unknown' || finish.data?.status === 'unknown' || remove.data?.status === 'unknown' ? (
        <div className="notice">Could not confirm the action completed.</div>
      ) : null}
      {start.isError || finish.isError || remove.isError ? (
        <div className="error">
          {start.error?.message ?? finish.error?.message ?? remove.error?.message ?? 'Action failed'}
        </div>
      ) : null}

      <div className="layout">
        <ShowcaseList showcases={showcases} selectedId={selectedId} onSelect={selectShowcase} />
        <section className="detail">
          {isPending && <p>Loading showcases...</p>}
          {selected ? (
            <ShowcaseDetail
              showcase={selected}
              timeline={selectedTimeline}
              onStart={() => start.mutate(selected.showcaseId)}
              onFinish={() => finish.mutate(selected.showcaseId)}
              onRemove={() => remove.mutate(selected.showcaseId)}
            />
          ) : (
            !isPending && <p>Select a showcase to see its timeline.</p>
          )}
        </section>
      </div>
    </div>
  );
}
