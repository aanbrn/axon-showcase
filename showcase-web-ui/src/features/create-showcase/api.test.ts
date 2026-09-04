// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import { scheduleShowcase } from './api';

afterEach(() => {
  vi.restoreAllMocks();
});

const scheduleRequest = { title: 'Demo', startTime: '2026-09-02T10:00:00Z', duration: 'PT5M' };

describe('scheduleShowcase', () => {
  it('sends the showcase payload as JSON to POST /showcases', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ showcaseId: '1' }), {
        status: 201,
        headers: { 'Content-Type': 'application/json' },
      }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await expect(scheduleShowcase(scheduleRequest)).resolves.toEqual({ status: 'scheduled', showcaseId: '1' });

    expect(fetchMock).toHaveBeenCalledWith(
      '/showcases',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(scheduleRequest),
      }),
    );
  });

  it('returns pending with the idempotency key on a 202 Accepted response', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(new Response(null, { status: 202, headers: { 'Idempotency-Key': 'key-1' } }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(scheduleShowcase(scheduleRequest)).resolves.toEqual({
      status: 'pending',
      idempotencyKey: 'key-1',
    });
  });

  it('sends the idempotency key when provided', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 202 }));
    vi.stubGlobal('fetch', fetchMock);

    await scheduleShowcase(scheduleRequest, 'key-1');

    expect(fetchMock).toHaveBeenCalledWith(
      '/showcases',
      expect.objectContaining({
        headers: expect.objectContaining({ 'Idempotency-Key': 'key-1' }),
      }),
    );
  });

  it('surfaces the problem-detail message on an error response', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(new Response(JSON.stringify({ detail: 'Title already in use' }), { status: 409 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(scheduleShowcase(scheduleRequest)).rejects.toThrow('Title already in use');
  });
});
