// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import type { Showcase } from '../types';
import { contextualTime } from './showcaseTime';

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

describe('contextualTime', () => {
  it('labels the start time for a scheduled showcase', () => {
    expect(contextualTime(makeShowcase())).toEqual({
      label: 'Start at',
      time: '2026-09-02T10:00:00Z',
    });
  });

  it('labels the finish time for a started showcase', () => {
    const started = makeShowcase({ status: 'STARTED' });
    expect(contextualTime(started)).toEqual({
      label: 'Finish at',
      time: '2026-09-02T10:05:00.000Z',
    });
  });

  it('labels the finished time for a finished showcase', () => {
    const finished = makeShowcase({
      status: 'FINISHED',
      finishedAt: '2026-09-02T10:10:00Z',
    });
    expect(contextualTime(finished)).toEqual({
      label: 'Finished at',
      time: '2026-09-02T10:10:00Z',
    });
  });
});
