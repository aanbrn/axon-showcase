// SPDX-License-Identifier: MIT
/**
 * The showcase-event entity slice's public API.
 *
 * <p>The event type, the stream connection, and the received-events state it owns. The sibling showcase slice reads the
 * event type through the declared `@x` cross-import API instead.
 */
export { connectEventStream } from './api/eventStream';
export { showcaseEventsReducer, useEventReceived, useLiveEvents } from './state';
export { type ShowcaseEvent, type ShowcaseEventType } from './types';
