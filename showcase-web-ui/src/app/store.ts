// SPDX-License-Identifier: MIT
import { configureStore, createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { useDispatch, useSelector } from 'react-redux';
import type { ShowcaseEvent } from '@/entities/showcase-event/types';

/**
 * Client-side UI state.
 *
 * <p>Holds the currently selected showcase and the live events received over the SSE stream. Server state (the showcase
 * list) is managed by TanStack Query, not here.
 */
interface UiState {
  selectedId: string | null;
  liveEvents: ShowcaseEvent[];
}

const initialState: UiState = {
  selectedId: null,
  liveEvents: [],
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    showcaseSelected(state, action: PayloadAction<string | null>) {
      state.selectedId = action.payload;
    },
    eventReceived(state, action: PayloadAction<ShowcaseEvent>) {
      state.liveEvents.push(action.payload);
    },
  },
});

export const { showcaseSelected, eventReceived } = uiSlice.actions;

/**
 * The Redux store.
 *
 * <p>Combines the UI slice; exposes the typed {@link RootState} and {@link AppDispatch} along with the typed hooks for
 * consuming them.
 */
export const store = configureStore({
  reducer: {
    ui: uiSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

/** The dispatch hook typed to the app's store. */
export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
/** The selector hook typed to the app's store. */
export const useAppSelector = useSelector.withTypes<RootState>();
