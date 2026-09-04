// SPDX-License-Identifier: MIT
/**
 * The base URL for all API requests, resolved from the VITE_API_BASE_URL environment variable.
 *
 * <p>Defaults to the empty string so requests are same-origin relative paths, which works with the Vite dev-server
 * proxy and the gateway serving the UI from the same origin.
 */
export const BASE = import.meta.env.VITE_API_BASE_URL ?? '';

/**
 * The outcome of a state-changing request.
 *
 * <p>Long-running writes may be accepted for asynchronous processing and complete later; the UI reconciles such
 * "pending" outcomes against the read model rather than treating them as failures.
 */
export type MutationResult = { status: 'done' } | { status: 'pending' };

/**
 * Sends an HTTP request to the given path, prefixed with the configured base URL.
 *
 * <p>Returns the raw response so callers can branch on the status (e.g. 202 Accepted) before unwrapping the body
 * through {@link handle}.
 *
 * @param path the request path, e.g. "/showcases"
 * @param init optional fetch options (method, headers, body, etc.)
 * @returns the raw fetch response
 */
export async function request(path: string, init?: RequestInit): Promise<Response> {
  return fetch(`${BASE}${path}`, init);
}

/**
 * Reports whether a response indicates the write was accepted for asynchronous processing.
 *
 * <p>The gateway answers lifecycle commands with 202 Accepted when the write is queued; callers treat this as "pending"
 * and poll the read model for the eventual outcome.
 *
 * @param response the response to inspect
 * @returns true when the response status is 202 Accepted
 */
export function isPending(response: Response): boolean {
  return response.status === 202;
}

/**
 * Performs a state-changing HTTP request that may complete asynchronously.
 *
 * <p>Unwraps the response through {@link handle}, treating a 202 Accepted response as "pending" rather than an error so
 * the caller can reconcile against the read model.
 *
 * @param path the request path
 * @param method the HTTP method, e.g. "PUT" or "DELETE"
 * @returns "done" when the write completed, "pending" when it was accepted for processing
 */
export async function mutate(path: string, method: string): Promise<MutationResult> {
  const response = await request(path, { method });
  if (isPending(response)) {
    return { status: 'pending' };
  }
  await handle<void>(response);
  return { status: 'done' };
}

/**
 * Unwraps an HTTP response into its body, surfacing errors as exceptions.
 *
 * <p>On an error response, throws an {@link Error} carrying the problem-detail message when the body is JSON with a
 * "detail" field, falling back to a status-based message. On success, parses the JSON body, returning undefined for
 * empty bodies and 204 No Content responses.
 *
 * @param response the response to unwrap
 * @returns the parsed body, or undefined when there is none
 */
export async function handle<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let detail: string | undefined;
    try {
      const body = (await response.json()) as { detail?: string };
      detail = body.detail;
    } catch {
      // non-JSON error body
    }
    throw new Error(detail ?? `Request failed with status ${response.status}`);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  return (text.length === 0 ? undefined : JSON.parse(text)) as T;
}
