// SPDX-License-Identifier: MIT
import { mutate, type MutationResult } from '@/shared/api';

/** The raw result of a lifecycle action, either completed or accepted for processing. */
export type ShowcaseActionResult = MutationResult;

/**
 * The reconciled outcome of a lifecycle action.
 *
 * <p>"unknown" represents an action that never confirmed within the retry budget, distinct from a confirmed completion
 * so the UI can tell them apart.
 */
export type ShowcaseActionOutcome = { status: 'done' } | { status: 'unknown' };

/**
 * Starts a scheduled showcase.
 *
 * @param showcaseId the id of the showcase to start
 * @returns "done" or "pending" depending on whether the gateway processed the write synchronously
 */
export async function startShowcase(showcaseId: string): Promise<ShowcaseActionResult> {
  return mutate(`/showcases/${showcaseId}/start`, 'PUT');
}

/**
 * Finishes a started showcase.
 *
 * @param showcaseId the id of the showcase to finish
 * @returns "done" or "pending" depending on whether the gateway processed the write synchronously
 */
export async function finishShowcase(showcaseId: string): Promise<ShowcaseActionResult> {
  return mutate(`/showcases/${showcaseId}/finish`, 'PUT');
}

/**
 * Removes a showcase.
 *
 * @param showcaseId the id of the showcase to remove
 * @returns "done" or "pending" depending on whether the gateway processed the write synchronously
 */
export async function removeShowcase(showcaseId: string): Promise<ShowcaseActionResult> {
  return mutate(`/showcases/${showcaseId}`, 'DELETE');
}
