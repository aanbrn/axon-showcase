// SPDX-License-Identifier: MIT
/**
 * The showcase-event slice's cross-import API for the sibling `entities/showcase` slice.
 *
 * <p>The showcase list reconciles against the live stream and the timeline merges received events into a showcase, so
 * the slice needs the event type and the received-events feed. Feature-Sliced Design forbids a sibling-slice import
 * except through a declared `@x` entry point like this one, so the edge is explicit and narrow rather than a direct
 * reach into the slice's internals.
 */
export { useLiveEvents } from '../state';
export type { ShowcaseEvent, ShowcaseEventType } from '../types';
