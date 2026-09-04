// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import { eventReceived, showcaseSelected, store } from './store';

describe('store', () => {
  it('has the initial ui state', () => {
    expect(store.getState()).toEqual({
      ui: { selectedId: null, liveEvents: [] },
    });
  });

  it('selects a showcase', () => {
    store.dispatch(showcaseSelected('abc-123'));
    expect(store.getState().ui.selectedId).toBe('abc-123');
  });

  it('accumulates live events', () => {
    store.dispatch(eventReceived({ type: 'SCHEDULED', showcaseId: 'abc', timestamp: '2026-09-02T10:00:00Z' }));
    store.dispatch(eventReceived({ type: 'STARTED', showcaseId: 'abc', timestamp: '2026-09-02T10:05:00Z' }));
    expect(store.getState().ui.liveEvents).toEqual([
      { type: 'SCHEDULED', showcaseId: 'abc', timestamp: '2026-09-02T10:00:00Z' },
      { type: 'STARTED', showcaseId: 'abc', timestamp: '2026-09-02T10:05:00Z' },
    ]);
  });
});
