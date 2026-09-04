// SPDX-License-Identifier: MIT
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { type PropsWithChildren } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import * as api from './api';
import { retryUntilCompleted } from '@/shared/retry';
import { waitForShowcaseStatus } from '@/entities/showcase/query-hooks';
import { useStartShowcase } from './useShowcaseActions';

vi.mock('@/shared/retry', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/shared/retry')>();
  return { ...actual, retryUntilCompleted: vi.fn() };
});

vi.mock('@/entities/showcase/query-hooks', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/entities/showcase/query-hooks')>();
  return { ...actual, waitForShowcaseStatus: vi.fn() };
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

describe('useStartShowcase', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('reports done when the action completes on the first attempt', async () => {
    vi.spyOn(api, 'startShowcase').mockResolvedValue({ status: 'done' });
    const { result } = renderHook(() => useStartShowcase(), { wrapper: createWrapper() });

    result.current.mutate('showcase-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual({ status: 'done' });
    expect(retryUntilCompleted).not.toHaveBeenCalled();
    expect(waitForShowcaseStatus).not.toHaveBeenCalled();
  });

  it('reports done when the retry confirms the action', async () => {
    vi.spyOn(api, 'startShowcase')
      .mockResolvedValueOnce({ status: 'pending' })
      .mockResolvedValueOnce({ status: 'done' });
    vi.mocked(retryUntilCompleted).mockImplementation(async (attempt) => (await attempt()) === 'done');
    const { result } = renderHook(() => useStartShowcase(), { wrapper: createWrapper() });

    result.current.mutate('showcase-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual({ status: 'done' });
    expect(api.startShowcase).toHaveBeenCalledTimes(2);
    expect(waitForShowcaseStatus).not.toHaveBeenCalled();
  });

  it('reports unknown when the retry budget is exhausted', async () => {
    vi.spyOn(api, 'startShowcase').mockResolvedValue({ status: 'pending' });
    vi.mocked(retryUntilCompleted).mockResolvedValue(false);
    const { result } = renderHook(() => useStartShowcase(), { wrapper: createWrapper() });

    result.current.mutate('showcase-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual({ status: 'unknown' });
    expect(waitForShowcaseStatus).not.toHaveBeenCalled();
  });
});
