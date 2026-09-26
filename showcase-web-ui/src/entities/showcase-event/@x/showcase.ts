// SPDX-License-Identifier: MIT
/**
 * The showcase-event slice's cross-import API for the sibling `entities/showcase` slice.
 *
 * <p>The showcase timeline merges received events into a showcase, so it needs the event type. Feature-Sliced Design
 * forbids a sibling-slice import except through a declared `@x` entry point like this one, so the edge is explicit and
 * narrow rather than a direct reach into the slice's internals.
 */
export type { ShowcaseEvent, ShowcaseEventType } from '../types';
