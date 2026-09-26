// SPDX-License-Identifier: MIT
import { configureStore } from '@reduxjs/toolkit';
import { showcaseEventsReducer } from '@/entities/showcase-event';
import { showcaseSelectionReducer } from '@/entities/showcase';

/**
 * The Redux store.
 *
 * <p>Composes the entity-owned state slices — the received live events and the showcase selection. The app layer owns
 * only the composition, so no lower layer imports this module.
 */
export const store = configureStore({
  reducer: {
    showcaseEvents: showcaseEventsReducer,
    showcaseSelection: showcaseSelectionReducer,
  },
});
