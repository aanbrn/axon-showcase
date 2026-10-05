// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import type { Showcase } from '../types';
import { ReconciliationController } from './reconciliation';

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

function controller(refresh: () => Promise<Showcase[]>, attempts = 5) {
  return new ReconciliationController({
    refresh,
    debounceMs: 0,
    settleMs: 0,
    attempts,
    sleep: () => Promise.resolve(),
  });
}

async function settled(): Promise<void> {
  await new Promise((resolve) => setTimeout(resolve, 0));
}

describe('ReconciliationController', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('coalesces a burst across showcases into a single refetch', async () => {
    const refresh = vi.fn().mockResolvedValue([showcase('1'), showcase('2')]);
    const subject = controller(refresh);

    subject.reconcile('1', (showcases) => showcases.some((entry) => entry.showcaseId === '1'));
    subject.reconcile('2', (showcases) => showcases.some((entry) => entry.showcaseId === '2'));

    await settled();
    expect(refresh).toHaveBeenCalledTimes(1);
  });

  it('keeps only the newest expected state for a showcase', async () => {
    const refresh = vi.fn().mockResolvedValue([showcase('1', 'FINISHED')]);
    const subject = controller(refresh);

    subject.reconcile('1', (showcases) => showcases.some((entry) => entry.status === 'STARTED'));
    subject.reconcile('1', (showcases) => showcases.some((entry) => entry.status === 'FINISHED'));

    await settled();
    expect(refresh).toHaveBeenCalledTimes(1);
  });

  it('refetches again until a later attempt observes the expected state', async () => {
    const refresh = vi
      .fn()
      .mockResolvedValueOnce([])
      .mockResolvedValue([showcase('1')]);
    const subject = controller(refresh);

    subject.reconcile('1', (showcases) => showcases.some((entry) => entry.showcaseId === '1'));

    await settled();
    expect(refresh).toHaveBeenCalledTimes(2);
  });

  it('abandons an unobserved state once its refetch budget is spent', async () => {
    const refresh = vi.fn().mockResolvedValue([]);
    const subject = controller(refresh, 3);

    subject.reconcile('1', (showcases) => showcases.some((entry) => entry.showcaseId === '1'));

    await settled();
    expect(refresh).toHaveBeenCalledTimes(3);
  });

  it('stops the settle loop when a refetch fails', async () => {
    const refresh = vi.fn().mockRejectedValue(new Error('unreachable'));
    const subject = controller(refresh);

    subject.reconcile('1', (showcases) => showcases.some((entry) => entry.showcaseId === '1'));

    await settled();
    expect(refresh).toHaveBeenCalledTimes(1);
  });
});
