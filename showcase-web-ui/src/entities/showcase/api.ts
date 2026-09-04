// SPDX-License-Identifier: MIT
import { handle, request } from '@/shared/api';
import type { Showcase } from './types';

/**
 * Fetches the list of showcases from the read model via the gateway.
 *
 * @returns the showcases as projected by the read side
 */
export async function fetchShowcases(): Promise<Showcase[]> {
  const response = await request('/showcases');
  return handle<Showcase[]>(response);
}
