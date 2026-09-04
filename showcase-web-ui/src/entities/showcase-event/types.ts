// SPDX-License-Identifier: MIT
/**
 * The type of showcase domain event.
 *
 * <p>Each lifecycle transition and removal publishes an event of the corresponding type to the event stream.
 */
export type ShowcaseEventType = 'SCHEDULED' | 'STARTED' | 'FINISHED' | 'REMOVED';

/**
 * A domain event delivered over the live event stream.
 *
 * <p>Identifies the event type, the showcase it concerns, and when it occurred, so clients can route it to the correct
 * showcase's timeline.
 */
export interface ShowcaseEvent {
  type: ShowcaseEventType;
  showcaseId: string;
  timestamp: string;
}
