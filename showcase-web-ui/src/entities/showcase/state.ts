// SPDX-License-Identifier: MIT
import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';

/**
 * The selected showcase.
 *
 * <p>Client-side state owned by the showcase entity: the id of the showcase whose detail the UI shows, or `null` when
 * none is selected.
 */
export interface ShowcaseSelectionState {
  selectedId: string | null;
}

const initialState: ShowcaseSelectionState = {
  selectedId: null,
};

const showcaseSelectionSlice = createSlice({
  name: 'showcaseSelection',
  initialState,
  reducers: {
    showcaseSelected(state, action: PayloadAction<string | null>) {
      state.selectedId = action.payload;
    },
  },
});

/** The reducer composing the selection into the store (the app layer owns the composition). */
export const showcaseSelectionReducer = showcaseSelectionSlice.reducer;
export const { showcaseSelected } = showcaseSelectionSlice.actions;

/**
 * The store shape this slice selects from.
 *
 * <p>Declared structurally so the hook does not import the app layer's store type (a lower layer importing `app` is a
 * boundary violation).
 */
interface StateWithSelection {
  showcaseSelection: ShowcaseSelectionState;
}

/** Selects the currently selected showcase id, or `null` when none is selected. */
export function useSelectedShowcaseId(): string | null {
  return useSelector((state: StateWithSelection) => state.showcaseSelection.selectedId);
}

/** Returns a callback selecting a showcase (or clearing the selection with `null`). */
export function useSelectShowcase(): (id: string | null) => void {
  const dispatch = useDispatch();
  return useCallback((id: string | null) => dispatch(showcaseSelected(id)), [dispatch]);
}
