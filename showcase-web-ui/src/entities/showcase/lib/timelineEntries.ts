// SPDX-License-Identifier: MIT
import type { Showcase } from '../types';
import type { ShowcaseEvent } from '@/entities/showcase-event/types';

/** A single marker in a showcase's history timeline. */
export interface TimelineEntry {
  id: string;
  label: string;
  time?: string;
}

/**
 * Builds the timeline entries from a showcase's read-model timestamps.
 *
 * <p>Emits a marker for each recorded lifecycle step (scheduled, started, finished) that the read model has
 * observed. This is the history baseline; live events are layered on top by {@link mergeTimeline}.
 */
function toTimeline(showcase: Showcase): TimelineEntry[] {
  const entries: TimelineEntry[] = [];
  if (showcase.scheduledAt) {
    entries.push({ id: 'scheduled', label: 'Scheduled', time: showcase.scheduledAt });
  }
  if (showcase.startedAt) {
    entries.push({ id: 'started', label: 'Started', time: showcase.startedAt });
  }
  if (showcase.finishedAt) {
    entries.push({ id: 'finished', label: 'Finished', time: showcase.finishedAt });
  }
  return entries;
}

/**
 * Converts live domain events into timeline entries.
 *
 * <p>The label is derived from the event type (e.g. "STARTED" becomes "Started") and the entry id combines the type
 * and timestamp so identical events remain distinct.
 */
function liveEventEntries(events: ShowcaseEvent[]): TimelineEntry[] {
  return events.map((event) => ({
    id: `${event.type}-${event.timestamp}`,
    label: event.type.charAt(0) + event.type.slice(1).toLowerCase(),
    time: event.timestamp,
  }));
}

/**
 * Removes timeline entries that carry the same label and time.
 *
 * <p>Guards against the same lifecycle moment appearing both in the read-model history and as a live event.
 */
function dedupeTimeline(entries: TimelineEntry[]): TimelineEntry[] {
  const seen = new Set<string>();
  return entries.filter((entry) => {
    const key = `${entry.label}-${entry.time ?? ''}`;
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

/**
 * Combines a showcase's read-model history with its live events into a deduplicated timeline.
 *
 * @param showcase the showcase to render
 * @param events the live events received over the SSE stream
 * @returns the combined, deduplicated timeline entries
 */
export function mergeTimeline(showcase: Showcase, events: ShowcaseEvent[]): TimelineEntry[] {
  const relevant = events.filter((event) => event.showcaseId === showcase.showcaseId);
  return dedupeTimeline([...toTimeline(showcase), ...liveEventEntries(relevant)]);
}
