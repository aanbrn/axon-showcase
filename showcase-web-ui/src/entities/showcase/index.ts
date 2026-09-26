// SPDX-License-Identifier: MIT
/**
 * The showcase entity slice's public API.
 *
 * <p>Its domain types, read hooks, timeline helpers, and the selection state it owns.
 */
export { fetchShowcases } from './api';
export { contextualTime } from './lib/showcaseTime';
export { mergeTimeline, type TimelineEntry } from './lib/timelineEntries';
export { SHOWCASES_QUERY_KEY } from './query-keys';
export { waitForEvent, waitForReadModel } from './query-hooks';
export { showcaseSelectionReducer, useSelectedShowcaseId, useSelectShowcase } from './state';
export { type ScheduleShowcaseRequest, type Showcase, type ShowcaseStatus } from './types';
export { useShowcases } from './useShowcases';
