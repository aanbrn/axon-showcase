// SPDX-License-Identifier: MIT
import type { ShowcaseEvent } from '@/entities/showcase-event/@x/showcase';
import type { ShowcasePredicate } from './reconciliation';

/**
 * Turns a domain event into the read-model state its arrival should be reconciled to.
 *
 * @param event the received domain event
 * @returns a predicate satisfied once the read model reflects the event's effect
 */
export function predicateForEvent(event: ShowcaseEvent): ShowcasePredicate {
  switch (event.type) {
    case 'REMOVED':
      return (showcases) => !showcases.some((showcase) => showcase.showcaseId === event.showcaseId);
    case 'SCHEDULED':
      return (showcases) => showcases.some((showcase) => showcase.showcaseId === event.showcaseId);
    case 'STARTED':
      return (showcases) =>
        showcases.some((showcase) => showcase.showcaseId === event.showcaseId && showcase.status === 'STARTED');
    case 'FINISHED':
      return (showcases) =>
        showcases.some((showcase) => showcase.showcaseId === event.showcaseId && showcase.status === 'FINISHED');
  }
}
