// SPDX-License-Identifier: MIT
import { QueryClient } from '@tanstack/react-query';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { retryUntilCompleted } from '@/shared/retry';
import type { Showcase } from '@/entities/showcase/types';
import type { ShowcaseEvent } from '@/entities/showcase-event/types';
import { waitForEvent, waitForReadModel, waitForShowcaseStatus } from './query-hooks';

vi.mock('@/shared/retry', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/shared/retry')>();
  return { ...actual, retryUntilCompleted: vi.fn() };
});

describe('waitForReadModel', () => {
  beforeEach(() => {
    vi.mocked(retryUntilCompleted).mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('fetches the list until the predicate matches', async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    const predicate = vi.fn().mockReturnValueOnce(false).mockReturnValueOnce(true);
    vi.mocked(retryUntilCompleted).mockImplementation(async (attempt) => {
      for (let i = 0; i < 3; i++) {
        if ((await attempt()) === 'done') {
          return true;
        }
      }
      return false;
    });

    const fetchSpy = vi.spyOn(queryClient, 'query').mockResolvedValue([{ showcaseId: '1', title: 'Demo' }] as never);

    await expect(waitForReadModel(queryClient, predicate)).resolves.toBe(true);
    expect(fetchSpy).toHaveBeenCalled();
    expect(predicate).toHaveBeenCalled();
  });

  it('returns false when the budget is exhausted', async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    vi.mocked(retryUntilCompleted).mockResolvedValue(false);

    await expect(waitForReadModel(queryClient, () => false)).resolves.toBe(false);
  });
});

describe('waitForEvent', () => {
  const showcase: Showcase = {
    showcaseId: '1',
    title: 'Demo',
    startTime: '2026-09-02T10:00:00Z',
    duration: 'PT5M',
    status: 'SCHEDULED',
    scheduledAt: '2026-09-02T10:00:00Z',
  };

  function stubFetch(returns: Showcase[]) {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    vi.spyOn(queryClient, 'query').mockResolvedValue(returns as never);
    return queryClient;
  }

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('waits until a STARTED event is reflected in the read model', async () => {
    const queryClient = stubFetch([{ ...showcase, status: 'STARTED' }]);
    vi.mocked(retryUntilCompleted).mockImplementation(async (attempt) => (await attempt()) === 'done');
    const event: ShowcaseEvent = {
      type: 'STARTED',
      showcaseId: '1',
      timestamp: '2026-09-02T10:05:00Z',
    };

    await expect(waitForEvent(queryClient, event)).resolves.toBe(true);
  });

  it('waits until a REMOVED event means the showcase is gone', async () => {
    const queryClient = stubFetch([]);
    vi.mocked(retryUntilCompleted).mockImplementation(async (attempt) => (await attempt()) === 'done');
    const event: ShowcaseEvent = {
      type: 'REMOVED',
      showcaseId: '1',
      timestamp: '2026-09-02T10:10:00Z',
    };

    await expect(waitForEvent(queryClient, event)).resolves.toBe(true);
  });
});

describe('reconciliation deduplication', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('coalesces concurrent reconciliations for the same showcase and outcome', async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    const showcase: Showcase = {
      showcaseId: '1',
      title: 'Demo',
      startTime: '2026-09-02T10:00:00Z',
      duration: 'PT5M',
      status: 'STARTED',
      scheduledAt: '2026-09-02T10:00:00Z',
    };
    vi.spyOn(queryClient, 'query').mockResolvedValue([showcase] as never);
    let attempts = 0;
    vi.mocked(retryUntilCompleted).mockImplementation(async (attempt) => {
      attempts++;
      return (await attempt()) === 'done';
    });
    const event: ShowcaseEvent = {
      type: 'STARTED',
      showcaseId: '1',
      timestamp: '2026-09-02T10:05:00Z',
    };

    const [fromEvent, fromStatus] = await Promise.all([
      waitForEvent(queryClient, event),
      waitForShowcaseStatus(queryClient, '1', 'STARTED'),
    ]);

    expect(fromEvent).toBe(true);
    expect(fromStatus).toBe(true);
    expect(attempts).toBe(1);
  });

  it('supersedes an in-flight reconciliation with the latest event state', async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    const scheduled: Showcase = {
      showcaseId: '1',
      title: 'Demo',
      startTime: '2026-09-02T10:00:00Z',
      duration: 'PT5M',
      status: 'SCHEDULED',
      scheduledAt: '2026-09-02T10:00:00Z',
    };
    let fetchState: Showcase[] = [scheduled];
    vi.spyOn(queryClient, 'query').mockImplementation(async () => fetchState as never);
    let attempts = 0;
    vi.mocked(retryUntilCompleted).mockImplementation(async (attempt) => {
      attempts++;
      // after the first poll, the saga has started the showcase
      if (attempts === 1) {
        fetchState = [{ ...scheduled, status: 'STARTED' }];
      }
      return (await attempt()) === 'done';
    });

    const scheduledEvent: ShowcaseEvent = {
      type: 'SCHEDULED',
      showcaseId: '1',
      timestamp: '2026-09-02T10:00:00Z',
    };
    const startedEvent: ShowcaseEvent = {
      type: 'STARTED',
      showcaseId: '1',
      timestamp: '2026-09-02T10:05:00Z',
    };

    const [fromScheduled, fromStarted] = await Promise.all([
      waitForEvent(queryClient, scheduledEvent),
      waitForEvent(queryClient, startedEvent),
    ]);

    expect(fromScheduled).toBe(true);
    expect(fromStarted).toBe(true);
    expect(attempts).toBe(1);
  });
});
