// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import { showcaseSelected, showcaseSelectionReducer } from './state';

describe('showcase selection state', () => {
  it('starts with nothing selected', () => {
    expect(showcaseSelectionReducer(undefined, { type: 'unknown' }).selectedId).toBeNull();
  });

  it('records the selected showcase', () => {
    const state = showcaseSelectionReducer(undefined, showcaseSelected('abc-123'));

    expect(state.selectedId).toBe('abc-123');
  });
});
