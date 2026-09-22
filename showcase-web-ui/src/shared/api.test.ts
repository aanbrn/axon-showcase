// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import { handle, mutate, request } from './api';

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

describe('request', () => {
  it('carries a W3C traceparent on every call', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await request('/showcases');

    expect(fetchMock.mock.calls[0][0]).toBe('/showcases');
    expect(new Headers((fetchMock.mock.calls[0][1] as RequestInit).headers).get('traceparent')).toMatch(
      /^00-[0-9a-f]{32}-[0-9a-f]{16}-01$/,
    );
  });

  it('keeps the caller headers alongside the traceparent', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await request('/showcases', { method: 'POST', headers: { 'Content-Type': 'application/json' } });

    expect(fetchMock.mock.calls[0][0]).toBe('/showcases');
    const init = fetchMock.mock.calls[0][1] as RequestInit;
    const headers = new Headers(init.headers);
    expect(init.method).toBe('POST');
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get('traceparent')).toMatch(/^00-[0-9a-f]{32}-[0-9a-f]{16}-01$/);
  });

  it('preserves a caller-provided Headers instance', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await request('/showcases', { headers: new Headers({ 'X-Custom': 'yes' }) });

    const headers = new Headers((fetchMock.mock.calls[0][1] as RequestInit).headers);
    expect(headers.get('X-Custom')).toBe('yes');
    expect(headers.get('traceparent')).toMatch(/^00-/);
  });
});

describe('mutate', () => {
  it('reports done for a successful response', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(mutate('/showcases/1/start', 'PUT')).resolves.toEqual({ status: 'done' });
    expect(fetchMock.mock.calls[0][0]).toBe('/showcases/1/start');
    const init = fetchMock.mock.calls[0][1] as RequestInit;
    expect(init.method).toBe('PUT');
    expect(new Headers(init.headers).get('traceparent')).toMatch(/^00-[0-9a-f]{32}-[0-9a-f]{16}-01$/);
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
