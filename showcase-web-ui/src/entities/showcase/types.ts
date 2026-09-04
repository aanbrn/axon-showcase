// SPDX-License-Identifier: MIT
/**
 * The lifecycle state of a showcase.
 *
 * <p>Progresses from scheduled to started to finished; a scheduled or started showcase can also be removed.
 */
export type ShowcaseStatus = 'SCHEDULED' | 'STARTED' | 'FINISHED';

/**
 * The read-model representation of a showcase.
 *
 * <p>Projected from the write side's domain events; the timestamps reflect when each lifecycle step was recorded by the
 * projection.
 */
export interface Showcase {
  showcaseId: string;
  title: string;
  /** The date-time when the showcase is scheduled to start. */
  startTime: string;
  /** The showcase duration as an ISO-8601 duration (e.g. "PT5M"). */
  duration: string;
  status: ShowcaseStatus;
  /** When the showcase was scheduled. */
  scheduledAt: string;
  /** When the showcase actually started, once it has. */
  startedAt?: string | null;
  /** When the showcase actually finished, once it has. */
  finishedAt?: string | null;
}

/** The payload to schedule a new showcase. */
export interface ScheduleShowcaseRequest {
  title: string;
  /** The date-time when the showcase should start, as an ISO-8601 instant. */
  startTime: string;
  /** The duration as an ISO-8601 duration (e.g. "PT5M"). */
  duration: string;
}
