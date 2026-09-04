// SPDX-License-Identifier: MIT
import type { TimelineEntry } from '@/entities/showcase/lib/timelineEntries';
import { formatTime } from '@/shared/format';

/**
 * Renders a showcase's history timeline.
 *
 * <p>Displays the given entries as a vertical list of labeled markers with their times, showing a placeholder when
 * there is nothing to render yet.
 */
export function Timeline(props: { entries: TimelineEntry[] }) {
  if (props.entries.length === 0) {
    return <p>No timeline entries yet.</p>;
  }
  return (
    <ol className="timeline">
      {props.entries.map((entry) => (
        <li key={entry.id}>
          <span className="label">{entry.label}</span>
          {entry.time && <span className="time">{formatTime(entry.time)}</span>}
        </li>
      ))}
    </ol>
  );
}
