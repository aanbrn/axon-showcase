// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import { handle, mutate } from './api';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('handle', () => {
  it('parses a JSON response body', async () => {
    const response = new Response(JSON.stringify({ id: 1 }), { status: 200 });
    await expect(handle<{ id: number }>(response)).resolves.toEqual({ id: 1 });
  });

  it('returns undefined for an empty body', async () => {
    const response = new Response('', { status: 200 });
    await expect(handle<void>(response)).resolves.toBeUndefined();
  });

  it('returns undefined for a 204 No Content response', async () => {
    const response = new Response(null, { status: 204 });
    await expect(handle<void>(response)).resolves.toBeUndefined();
  });

  it('throws the problem-detail message on an error response', async () => {
    const response = new Response(JSON.stringify({ detail: 'Title already in use' }), { status: 409 });
    await expect(handle<void>(response)).rejects.toThrow('Title already in use');
  });

  it('throws a generic message on a non-JSON error response', async () => {
    const response = new Response('oops', { status: 500 });
    await expect(handle<void>(response)).rejects.toThrow('Request failed with status 500');
  });
});

describe('mutate', () => {
  it('reports done for a successful response', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(mutate('/showcases/1/start', 'PUT')).resolves.toEqual({ status: 'done' });
    expect(fetchMock).toHaveBeenCalledWith('/showcases/1/start', { method: 'PUT' });
  });

  it('reports pending for a 202 Accepted response', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 202 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(mutate('/showcases/1', 'DELETE')).resolves.toEqual({ status: 'pending' });
  });

  it('propagates the problem-detail message on an error response', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(new Response(JSON.stringify({ detail: 'Showcase not found' }), { status: 404 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(mutate('/showcases/1/start', 'PUT')).rejects.toThrow('Showcase not found');
  });
});
