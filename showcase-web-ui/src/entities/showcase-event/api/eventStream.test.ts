// SPDX-License-Identifier: MIT
import { afterEach, describe, expect, it, vi } from 'vitest';
import { BASE } from '@/shared/api';
import { connectEventStream } from './eventStream';

type Listener = (event: { data: string }) => void;

class FakeEventSource {
  private listeners = new Map<string, Listener[]>();
  close = vi.fn();

  addEventListener = vi.fn((type: string, listener: Listener) => {
    const listeners = this.listeners.get(type) ?? [];
    listeners.push(listener);
    this.listeners.set(type, listeners);
  });

  emit(type: string, data: string) {
    for (const listener of this.listeners.get(type) ?? []) {
      listener({ data });
    }
  }
}

function stubEventSource() {
  const fake = new FakeEventSource();
  vi.stubGlobal(
    'EventSource',
    vi.fn(() => fake),
  );
  return fake;
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe('connectEventStream', () => {
  it('connects to the events endpoint under the configured base url', () => {
    const fake = stubEventSource();
    const eventSourceConstructor = vi.mocked(globalThis.EventSource);

    connectEventStream(vi.fn());

    expect(eventSourceConstructor).toHaveBeenCalledWith(`${BASE}/events`);
    expect(fake.close).not.toHaveBeenCalled();
  });

  it('subscribes to the showcase event type', () => {
    const fake = stubEventSource();

    connectEventStream(vi.fn());

    expect(fake.addEventListener).toHaveBeenCalledWith('showcase', expect.any(Function));
  });

  it('parses incoming showcase events and invokes the handler', () => {
    const fake = stubEventSource();
    const onEvent = vi.fn();
    const event = { type: 'STARTED', showcaseId: '1', timestamp: '2026-09-02T10:05:00Z' };

    connectEventStream(onEvent);
    fake.emit('showcase', JSON.stringify(event));

    expect(onEvent).toHaveBeenCalledWith(event);
  });

  it('ignores malformed event frames', () => {
    const fake = stubEventSource();
    const onEvent = vi.fn();

    connectEventStream(onEvent);
    fake.emit('showcase', 'not-json');

    expect(onEvent).not.toHaveBeenCalled();
  });

  it('closes the source on disconnect', () => {
    const fake = stubEventSource();
    const onEvent = vi.fn();

    const disconnect = connectEventStream(onEvent);
    disconnect();

    expect(fake.close).toHaveBeenCalledTimes(1);
  });
});
