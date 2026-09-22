// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import { finishShowcase, removeShowcase, startShowcase } from './api';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('showcase actions', () => {
  it('reports done when start/finish/remove succeed', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(startShowcase('1')).resolves.toEqual({ status: 'done' });
    await expect(finishShowcase('1')).resolves.toEqual({ status: 'done' });
    await expect(removeShowcase('1')).resolves.toEqual({ status: 'done' });

    expect(fetchMock).toHaveBeenCalledTimes(3);
    expect(fetchMock.mock.calls.map(([url]) => url)).toEqual([
      '/showcases/1/start',
      '/showcases/1/finish',
      '/showcases/1',
    ]);
    expect(fetchMock.mock.calls.map(([, init]) => (init as RequestInit).method)).toEqual(['PUT', 'PUT', 'DELETE']);
    for (const [, init] of fetchMock.mock.calls) {
      expect(new Headers((init as RequestInit).headers).get('traceparent')).toMatch(/^00-/);
    }
  });

  it('reports pending when start/finish/remove return 202', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 202 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(startShowcase('1')).resolves.toEqual({ status: 'pending' });
    await expect(finishShowcase('1')).resolves.toEqual({ status: 'pending' });
    await expect(removeShowcase('1')).resolves.toEqual({ status: 'pending' });
  });
});
