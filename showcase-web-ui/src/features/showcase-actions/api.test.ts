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
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/showcases/1/start', { method: 'PUT' });
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/showcases/1/finish', { method: 'PUT' });
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/showcases/1', { method: 'DELETE' });
  });

  it('reports pending when start/finish/remove return 202', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 202 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(startShowcase('1')).resolves.toEqual({ status: 'pending' });
    await expect(finishShowcase('1')).resolves.toEqual({ status: 'pending' });
    await expect(removeShowcase('1')).resolves.toEqual({ status: 'pending' });
  });
});
