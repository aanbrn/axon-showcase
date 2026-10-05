// SPDX-License-Identifier: MIT
import { configureStore } from '@reduxjs/toolkit';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, renderHook, waitFor } from '@testing-library/react';
import { type PropsWithChildren } from 'react';
import { Provider } from 'react-redux';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { eventReceived, showcaseEventsReducer } from '@/entities/showcase-event/state';
import * as api from './api';
import { DEFAULT_DEBOUNCE_MS } from './lib/reconciliation';
import type { Showcase } from './types';
import { useShowcaseReconciliation } from './useShowcaseReconciliation';

function showcase(showcaseId: string, status: Showcase['status'] = 'SCHEDULED'): Showcase {
  return {
    showcaseId,
    title: `Showcase ${showcaseId}`,
    startTime: '2026-09-02T10:00:00Z',
    duration: 'PT5M',
    status,
    scheduledAt: '2026-09-02T10:00:00Z',
  };
}

function harness() {
  const store = configureStore({ reducer: { showcaseEvents: showcaseEventsReducer } });
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const wrapper = ({ children }: PropsWithChildren) => (
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    </Provider>
  );
  return { store, wrapper };
}

describe('useShowcaseReconciliation', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('reconciles a burst of live events with a single refetch', async () => {
    const { store, wrapper } = harness();
    const fetchSpy = vi.spyOn(api, 'fetchShowcases').mockResolvedValue([showcase('1', 'STARTED')]);
    renderHook(() => useShowcaseReconciliation(), { wrapper });

    const liveTimestamp = new Date(Date.now() + 1000).toISOString();
    act(() => {
      store.dispatch(eventReceived({ type: 'SCHEDULED', showcaseId: '1', timestamp: liveTimestamp }));
      store.dispatch(eventReceived({ type: 'STARTED', showcaseId: '1', timestamp: liveTimestamp }));
    });

    await waitFor(() => expect(fetchSpy).toHaveBeenCalledTimes(1));
    expect(fetchSpy).toHaveBeenCalledTimes(1);
  });

  it('does not reconcile events replayed before the stream opened', async () => {
    const { store, wrapper } = harness();
    const fetchSpy = vi.spyOn(api, 'fetchShowcases').mockResolvedValue([showcase('1')]);
    renderHook(() => useShowcaseReconciliation(), { wrapper });

    act(() => {
      store.dispatch(
        eventReceived({ type: 'SCHEDULED', showcaseId: '1', timestamp: new Date(Date.now() - 1000).toISOString() }),
      );
    });

    await new Promise((resolve) => setTimeout(resolve, DEFAULT_DEBOUNCE_MS + 100));
    expect(fetchSpy).not.toHaveBeenCalled();
  });
});
