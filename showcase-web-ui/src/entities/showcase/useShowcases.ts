// SPDX-License-Identifier: MIT
import { useQuery } from '@tanstack/react-query';
import { fetchShowcases } from './api';
import { SHOWCASES_QUERY_KEY } from './query-keys';

/**
 * Provides the cached showcase list.
 *
 * <p>Wraps the showcase list query, exposing TanStack Query's caching, loading, and error state. The list is refetched
 * when invalidated by a mutation or a live event.
 */
export function useShowcases() {
  return useQuery({
    queryKey: SHOWCASES_QUERY_KEY,
    queryFn: () => fetchShowcases(),
  });
}
