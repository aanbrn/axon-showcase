// SPDX-License-Identifier: MIT
import type { Showcase } from '@/entities/showcase/types';
import type { TimelineEntry } from '@/entities/showcase/lib/timelineEntries';
import { Timeline } from './Timeline';

/**
 * Renders a selected showcase's detail panel.
 *
 * <p>Shows the lifecycle actions appropriate for the showcase's status (Start when scheduled, Finish when started,
 * Remove always) and the combined history + live-event timeline.
 */
export function ShowcaseDetail(props: {
  showcase: Showcase;
  timeline: TimelineEntry[];
  onStart: () => void;
  onFinish: () => void;
  onRemove: () => void;
}) {
  return (
    <>
      <h2>{props.showcase.title}</h2>
      <div className="actions">
        {props.showcase.status === 'SCHEDULED' && (
          <button type="button" onClick={props.onStart}>
            Start
          </button>
        )}
        {props.showcase.status === 'STARTED' && (
          <button type="button" onClick={props.onFinish}>
            Finish
          </button>
        )}
        <button type="button" className="danger" onClick={props.onRemove}>
          Remove
        </button>
      </div>
      <Timeline entries={props.timeline} />
    </>
  );
}
