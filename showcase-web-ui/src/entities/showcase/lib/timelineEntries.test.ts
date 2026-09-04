// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import { mergeTimeline } from './timelineEntries';
import type { Showcase } from '@/entities/showcase/types';
import type { ShowcaseEvent } from '@/entities/showcase-event/types';

function makeShowcase(overrides: Partial<Showcase> = {}): Showcase {
  return {
    showcaseId: '1',
    title: 'Demo',
    startTime: '2026-09-02T10:00:00Z',
    duration: 'PT5M',
    status: 'SCHEDULED',
    scheduledAt: '2026-09-02T10:00:00Z',
    ...overrides,
  };
}

function makeEvent(overrides: Partial<ShowcaseEvent> = {}): ShowcaseEvent {
  return {
    type: 'STARTED',
    showcaseId: '1',
    timestamp: '2026-09-02T10:05:00Z',
    ...overrides,
  };
}

describe('mergeTimeline', () => {
  it('renders only the timestamps present on the read model', () => {
    const timeline = mergeTimeline(makeShowcase(), []);
    expect(timeline.map((entry) => entry.label)).toEqual(['Scheduled']);
  });

  it('includes start and finish markers when present', () => {
    const timeline = mergeTimeline(
      makeShowcase({
        status: 'FINISHED',
        startedAt: '2026-09-02T10:05:00Z',
        finishedAt: '2026-09-02T10:10:00Z',
      }),
      [],
    );
    expect(timeline.map((entry) => entry.label)).toEqual(['Scheduled', 'Started', 'Finished']);
  });

  it('appends live events to the read-model history for the same showcase', () => {
    const events = [makeEvent({ type: 'STARTED', timestamp: '2026-09-02T10:05:00Z' })];
    const timeline = mergeTimeline(makeShowcase(), events);
    expect(timeline.map((entry) => entry.label)).toEqual(['Scheduled', 'Started']);
  });

  it('dedupes a live event that the refreshed read model already reflects', () => {
    const events = [makeEvent({ type: 'STARTED', timestamp: '2026-09-02T10:05:00Z' })];
    const showcase = makeShowcase({
      status: 'STARTED',
      startedAt: '2026-09-02T10:05:00Z',
    });
    const timeline = mergeTimeline(showcase, events);
    expect(timeline.map((entry) => entry.label)).toEqual(['Scheduled', 'Started']);
  });

  it('ignores live events for other showcases', () => {
    const events = [makeEvent({ showcaseId: 'other' })];
    const timeline = mergeTimeline(makeShowcase(), events);
    expect(timeline.map((entry) => entry.label)).toEqual(['Scheduled']);
  });
});
