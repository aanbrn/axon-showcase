// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import { onCLS, onFCP, onINP, onLCP, onTTFB } from 'web-vitals';
import { initClientTelemetry, reportError, reportVital } from './telemetry';

vi.mock('web-vitals', () => ({
  onLCP: vi.fn(),
  onINP: vi.fn(),
  onCLS: vi.fn(),
  onFCP: vi.fn(),
  onTTFB: vi.fn(),
}));

afterEach(() => {
  vi.restoreAllMocks();
  vi.clearAllMocks();
});

function stubFetch() {
  const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

function requestBody(fetchMock: ReturnType<typeof stubFetch>) {
  return JSON.parse((fetchMock.mock.calls[0][1] as RequestInit).body as string);
}

describe('reportVital', () => {
  it('posts the vital to the telemetry endpoint', async () => {
    const fetchMock = stubFetch();

    reportVital({ name: 'LCP', rating: 'good', value: 1234 });

    const init = fetchMock.mock.calls[0][1] as RequestInit;
    expect(fetchMock.mock.calls[0][0]).toBe('/telemetry');
    expect(init.method).toBe('POST');
    expect(init.keepalive).toBe(true);
    expect(requestBody(fetchMock)).toEqual({
      path: window.location.pathname,
      vitals: [{ name: 'LCP', rating: 'good', value: 1234 }],
      errors: [],
    });
  });

  it('carries the page load traceparent', async () => {
    const fetchMock = stubFetch();

    reportVital({ name: 'CLS', rating: 'good', value: 0.02 });

    const headers = new Headers((fetchMock.mock.calls[0][1] as RequestInit).headers);
    expect(headers.get('traceparent')).toMatch(/^00-[0-9a-f]{32}-[0-9a-f]{16}-01$/);
  });

  it('does not throw when the report fails', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('offline')));

    expect(() => reportVital({ name: 'INP', rating: 'good', value: 100 })).not.toThrow();
  });
});

describe('reportError', () => {
  it('posts an Error with its name and message', async () => {
    const fetchMock = stubFetch();

    reportError(new TypeError('boom'));

    expect(requestBody(fetchMock).errors).toEqual([{ type: 'TypeError', message: 'boom' }]);
  });

  it('posts a non-Error reason as a generic Error', async () => {
    const fetchMock = stubFetch();

    reportError('nope');

    expect(requestBody(fetchMock).errors).toEqual([{ type: 'Error', message: 'nope' }]);
  });
});

describe('initClientTelemetry', () => {
  it('subscribes every Core Web Vital observer', () => {
    initClientTelemetry();

    for (const observe of [onLCP, onINP, onCLS, onFCP, onTTFB]) {
      expect(observe).toHaveBeenCalledOnce();
    }
  });

  it('reports an uncaught error raised through the registered listener', () => {
    const fetchMock = stubFetch();
    const addEventListener = vi.spyOn(window, 'addEventListener');

    initClientTelemetry();
    const errorHandler = addEventListener.mock.calls.find(([type]) => type === 'error')?.[1];
    expect(errorHandler).toBeDefined();
    (errorHandler as (event: ErrorEvent) => void)(
      new ErrorEvent('error', { message: 'boom', error: new TypeError('boom') }),
    );

    expect(requestBody(fetchMock).errors).toEqual([{ type: 'TypeError', message: 'boom' }]);
  });

  it('reports an unhandled rejection raised through the registered listener', () => {
    const fetchMock = stubFetch();
    const addEventListener = vi.spyOn(window, 'addEventListener');

    initClientTelemetry();
    const rejectionHandler = addEventListener.mock.calls.find(([type]) => type === 'unhandledrejection')?.[1];
    expect(rejectionHandler).toBeDefined();
    (rejectionHandler as (event: PromiseRejectionEvent) => void)({
      reason: new Error('rejected'),
    } as PromiseRejectionEvent);

    expect(requestBody(fetchMock).errors).toEqual([{ type: 'Error', message: 'rejected' }]);
  });
});
