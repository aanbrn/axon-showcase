// SPDX-License-Identifier: MIT
import { BASE } from '@/shared/api';
import type { ShowcaseEvent } from '../types';

/**
 * Connects to the gateway's live showcase event stream over Server-Sent Events.
 *
 * <p>Subscribes to the "showcase" event type emitted by the gateway and parses each frame into a {@link ShowcaseEvent}.
 * The browser's EventSource reconnects automatically on transient failures, and replay-buffered events are re-delivered
 * on reconnect.
 *
 * @param onEvent invoked for each received domain event
 * @returns a disconnect function that closes the stream
 */
export function connectEventStream(onEvent: (event: ShowcaseEvent) => void): () => void {
  const source = new EventSource(`${BASE}/events`);

  source.addEventListener('showcase', (message) => {
    try {
      const parsed = JSON.parse((message as MessageEvent).data as string) as ShowcaseEvent;
      onEvent(parsed);
    } catch {
      // ignore malformed event frames
    }
  });

  source.onerror = () => {
    // EventSource reconnects automatically; nothing to do here.
  };

  return () => source.close();
}
