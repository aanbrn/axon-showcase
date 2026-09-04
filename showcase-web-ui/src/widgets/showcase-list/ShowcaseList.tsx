// SPDX-License-Identifier: MIT
import type { Showcase } from '@/entities/showcase/types';
import { contextualTime } from '@/entities/showcase/lib/showcaseTime';
import { formatDateTime, formatDuration } from '@/shared/format';

/**
 * Renders the showcase list.
 *
 * <p>Each row shows the showcase title, its status badge, duration, and a status-aware timestamp (scheduled start,
 * expected finish, or actual finish). The selected showcase is highlighted, and clicking a row invokes {@code
 * onSelect}.
 */
export function ShowcaseList(props: {
  showcases: Showcase[];
  selectedId: string | null;
  onSelect: (showcaseId: string) => void;
}) {
  return (
    <section className="list">
      <h2>Showcases</h2>
      {props.showcases.length === 0 && <p>No showcases yet.</p>}
      <ul>
        {props.showcases.map((showcase) => {
          const timeInfo = contextualTime(showcase);
          return (
            <li key={showcase.showcaseId} className={props.selectedId === showcase.showcaseId ? 'active' : ''}>
              <button type="button" onClick={() => props.onSelect(showcase.showcaseId)}>
                <span className="title">{showcase.title}</span>
                <span className={`status ${showcase.status.toLowerCase()}`}>{showcase.status}</span>
                {timeInfo && (
                  <span className="time">
                    {timeInfo.label}: {formatDateTime(timeInfo.time)} · {formatDuration(showcase.duration)}
                  </span>
                )}
              </button>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
