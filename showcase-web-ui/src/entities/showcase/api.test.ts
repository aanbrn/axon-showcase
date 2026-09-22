// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchShowcases } from './api';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('fetchShowcases', () => {
  it('fetches the showcase list', async () => {
    const showcase = { showcaseId: '1', title: 'Demo', startTime: '2026-09-02T10:00:00Z' };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([showcase])));
    vi.stubGlobal('fetch', fetchMock);

    await expect(fetchShowcases()).resolves.toEqual([showcase]);
    expect(fetchMock.mock.calls[0][0]).toBe('/showcases');
    expect(new Headers((fetchMock.mock.calls[0][1] as RequestInit).headers).get('traceparent')).toMatch(/^00-/);
  });
});
