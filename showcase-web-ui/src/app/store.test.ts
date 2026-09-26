// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import { store } from './store';

describe('store', () => {
  it('composes the entity-owned state slices', () => {
    expect(store.getState()).toEqual({
      showcaseEvents: { liveEvents: [] },
      showcaseSelection: { selectedId: null },
    });
  });
});
