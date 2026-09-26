// SPDX-License-Identifier: MIT
import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { ShowcaseEvent } from './types';

/**
 * The live events received over the event stream.
 *
 * <p>Client-side state owned by the showcase-event entity: every event the gateway delivers is appended here, so the
 * showcase timeline can merge them with the read model.
 */
export interface ShowcaseEventsState {
  liveEvents: ShowcaseEvent[];
}

const initialState: ShowcaseEventsState = {
  liveEvents: [],
};

const showcaseEventsSlice = createSlice({
  name: 'showcaseEvents',
  initialState,
  reducers: {
    eventReceived(state, action: PayloadAction<ShowcaseEvent>) {
      state.liveEvents.push(action.payload);
    },
  },
});

/** The reducer composing the received events into the store (the app layer owns the composition). */
export const showcaseEventsReducer = showcaseEventsSlice.reducer;
export const { eventReceived } = showcaseEventsSlice.actions;

/**
 * The store shape this slice selects from.
 *
 * <p>Declared structurally so the hook does not import the app layer's store type (a lower layer importing `app` is a
 * boundary violation).
 */
interface StateWithEvents {
  showcaseEvents: ShowcaseEventsState;
}

/** Selects the live events received over the event stream. */
export function useLiveEvents(): ShowcaseEvent[] {
  return useSelector((state: StateWithEvents) => state.showcaseEvents.liveEvents);
}

/** Returns a callback appending a received event to the live events. */
export function useEventReceived(): (event: ShowcaseEvent) => void {
  const dispatch = useDispatch();
  return useCallback((event: ShowcaseEvent) => dispatch(eventReceived(event)), [dispatch]);
}
