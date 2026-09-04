// SPDX-License-Identifier: MIT
import type { Showcase } from '../types';

/**
 * A labeled timestamp describing a showcase's next relevant moment.
 */
interface ShowcaseTimeInfo {
  label: string;
  time: string;
}

/**
 * Adds an ISO-8601 minute duration (e.g. "PT5M") to a start time.
 *
 * <p>Used to derive a started showcase's expected finish time from its scheduled start and duration. Durations other
 * than whole minutes are not recognized and leave the time unchanged.
 *
 * @param startTime the start time as an ISO-8601 timestamp
 * @param duration the ISO-8601 duration to add
 * @returns the resulting time as an ISO-8601 timestamp
 */
function addDuration(startTime: string, duration: string): string {
  const date = new Date(startTime);
  const match = /^PT(\d+)M$/.exec(duration);
  if (match) {
    date.setMinutes(date.getMinutes() + Number(match[1]));
  }
  return date.toISOString();
}

/**
 * Derives the contextual timestamp to display for a showcase based on its status.
 *
 * <p>Shows the scheduled start time while scheduled, the expected finish time (start plus duration) once started, and
 * the actual finish time once done.
 *
 * @param showcase the showcase to inspect
 * @returns the labeled timestamp, or undefined when no moment applies
 */
export function contextualTime(showcase: Showcase): ShowcaseTimeInfo | undefined {
  switch (showcase.status) {
    case 'SCHEDULED':
      return { label: 'Start at', time: showcase.startTime };
    case 'STARTED':
      return { label: 'Finish at', time: addDuration(showcase.startTime, showcase.duration) };
    case 'FINISHED':
      return showcase.finishedAt ? { label: 'Finished at', time: showcase.finishedAt } : undefined;
  }
}
