// SPDX-License-Identifier: MIT
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { type PropsWithChildren } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import * as api from './api';
import { retryUntilCompleted } from '@/shared/retry';
import { waitForShowcasePresence } from '@/entities/showcase/query-hooks';
import { useCreateShowcase } from './useCreateShowcase';

vi.mock('@/shared/retry', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/shared/retry')>();
  return { ...actual, retryUntilCompleted: vi.fn() };
});

vi.mock('@/entities/showcase/query-hooks', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/entities/showcase/query-hooks')>();
  return { ...actual, waitForShowcasePresence: vi.fn() };
});

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  return function Wrapper({ children }: PropsWithChildren) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

const request = { title: 'Demo', startTime: '2026-09-02T10:00', duration: 'PT5M' };

describe('useCreateShowcase', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('reports scheduled when the first attempt schedules the showcase', async () => {
    vi.spyOn(api, 'scheduleShowcase').mockResolvedValue({ status: 'scheduled', showcaseId: 'abc' });
    const { result } = renderHook(() => useCreateShowcase(), { wrapper: createWrapper() });

    result.current.mutate(request);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual({ status: 'scheduled', showcaseId: 'abc' });
    expect(retryUntilCompleted).not.toHaveBeenCalled();
    expect(waitForShowcasePresence).not.toHaveBeenCalled();
  });

  it('reports scheduled when the retry confirms the showcase', async () => {
    vi.spyOn(api, 'scheduleShowcase')
      .mockResolvedValueOnce({ status: 'pending', idempotencyKey: 'key-1' })
      .mockResolvedValueOnce({ status: 'scheduled', showcaseId: 'abc' });
    vi.mocked(retryUntilCompleted).mockImplementation(async (attempt) => (await attempt()) === 'done');
    const { result } = renderHook(() => useCreateShowcase(), { wrapper: createWrapper() });

    result.current.mutate(request);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual({ status: 'scheduled', showcaseId: 'abc' });
    expect(api.scheduleShowcase).toHaveBeenCalledTimes(2);
    expect(waitForShowcasePresence).not.toHaveBeenCalled();
  });

  it('reports unknown when the retry budget is exhausted', async () => {
    vi.spyOn(api, 'scheduleShowcase').mockResolvedValue({ status: 'pending', idempotencyKey: 'key-1' });
    vi.mocked(retryUntilCompleted).mockResolvedValue(false);
    const { result } = renderHook(() => useCreateShowcase(), { wrapper: createWrapper() });

    result.current.mutate(request);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual({ status: 'unknown' });
    expect(waitForShowcasePresence).not.toHaveBeenCalled();
  });
});
