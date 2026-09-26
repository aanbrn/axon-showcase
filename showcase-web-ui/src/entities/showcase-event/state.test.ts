// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import { eventReceived, showcaseEventsReducer } from './state';

const scheduled = { type: 'SCHEDULED', showcaseId: 'abc', timestamp: '2026-09-02T10:00:00Z' } as const;
const started = { type: 'STARTED', showcaseId: 'abc', timestamp: '2026-09-02T10:05:00Z' } as const;

describe('showcase events state', () => {
  it('starts with no live events', () => {
    expect(showcaseEventsReducer(undefined, { type: 'unknown' }).liveEvents).toEqual([]);
  });

  it('accumulates received events in arrival order', () => {
    const once = showcaseEventsReducer(undefined, eventReceived(scheduled));
    const twice = showcaseEventsReducer(once, eventReceived(started));

    expect(twice.liveEvents).toEqual([scheduled, started]);
  });
});
