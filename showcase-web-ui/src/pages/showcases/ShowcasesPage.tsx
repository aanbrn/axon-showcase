// SPDX-License-Identifier: MIT
import { useEffect } from 'react';
import { connectEventStream, useEventReceived, useLiveEvents } from '@/entities/showcase-event';
import {
  mergeTimeline,
  useSelectedShowcaseId,
  useSelectShowcase,
  useShowcases,
  useShowcaseReconciliation,
} from '@/entities/showcase';
import { CreateShowcaseForm, useCreateShowcase } from '@/features/create-showcase';
import { useFinishShowcase, useRemoveShowcase, useStartShowcase } from '@/features/showcase-actions';
import { ShowcaseList } from '@/widgets/showcase-list';
import { ShowcaseDetail } from '@/widgets/showcase-detail';

/**
 * The showcases page.
 *
 * <p>Composes the create form, list, and detail widgets, drives the lifecycle mutations, and subscribes to the live
 * event stream. The showcase entity reconciles the list with the eventually-consistent read model from those events, so
 * the page renders rather than polls.
 */
export function ShowcasesPage() {
  const onEventReceived = useEventReceived();
  const { data: showcases = [], isPending } = useShowcases();
  const liveEvents = useLiveEvents();
  const selectedId = useSelectedShowcaseId();
  const selectShowcase = useSelectShowcase();
  useShowcaseReconciliation();

  const create = useCreateShowcase();
  const start = useStartShowcase();
  const finish = useFinishShowcase();
  const remove = useRemoveShowcase();

  useEffect(() => {
    return connectEventStream((event) => onEventReceived(event));
  }, [onEventReceived]);

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
