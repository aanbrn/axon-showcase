// SPDX-License-Identifier: MIT
import { describe, expect, it, vi } from 'vitest';
import { retryUntilCompleted } from './retry';

describe('retryUntilCompleted', () => {
  it('returns true when the attempt eventually completes', async () => {
    let calls = 0;
    const attempt = vi.fn().mockImplementation(async () => {
      calls++;
      return calls < 2 ? 'retry' : 'done';
    });

    await expect(retryUntilCompleted(attempt, { attempts: 3, delayMs: 0 })).resolves.toBe(true);
    expect(attempt).toHaveBeenCalledTimes(2);
  });

  it('returns false when the budget is exhausted', async () => {
    const attempt = vi.fn().mockResolvedValue('retry');

    await expect(retryUntilCompleted(attempt, { attempts: 2, delayMs: 0 })).resolves.toBe(false);
    expect(attempt).toHaveBeenCalledTimes(2);
  });

  it('propagates an error from the attempt', async () => {
    const attempt = vi.fn().mockRejectedValue(new Error('boom'));

    await expect(retryUntilCompleted(attempt, { attempts: 3, delayMs: 0 })).rejects.toThrow('boom');
  });
});
