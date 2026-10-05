// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import type { Showcase } from '../types';
import { predicateForEvent } from './eventPredicate';

function showcase(showcaseId: string, status: Showcase['status']): Showcase {
  return {
    showcaseId,
    title: `Showcase ${showcaseId}`,
    startTime: '2026-09-02T10:00:00Z',
    duration: 'PT5M',
    status,
    scheduledAt: '2026-09-02T10:00:00Z',
  };
}

describe('predicateForEvent', () => {
  it('matches the appearance of a scheduled showcase', () => {
    const predicate = predicateForEvent({ type: 'SCHEDULED', showcaseId: '1', timestamp: '2026-09-02T10:00:00Z' });

    expect(predicate([showcase('1', 'SCHEDULED')])).toBe(true);
    expect(predicate([])).toBe(false);
  });

  it('matches a showcase that has started', () => {
    const predicate = predicateForEvent({ type: 'STARTED', showcaseId: '1', timestamp: '2026-09-02T10:05:00Z' });

    expect(predicate([showcase('1', 'STARTED')])).toBe(true);
    expect(predicate([showcase('1', 'SCHEDULED')])).toBe(false);
  });

  it('matches a showcase that has finished', () => {
    const predicate = predicateForEvent({ type: 'FINISHED', showcaseId: '1', timestamp: '2026-09-02T10:10:00Z' });

    expect(predicate([showcase('1', 'FINISHED')])).toBe(true);
    expect(predicate([showcase('1', 'STARTED')])).toBe(false);
  });

  it('matches the removal of a showcase', () => {
    const predicate = predicateForEvent({ type: 'REMOVED', showcaseId: '1', timestamp: '2026-09-02T10:10:00Z' });

    expect(predicate([])).toBe(true);
    expect(predicate([showcase('1', 'STARTED')])).toBe(false);
  });
});
