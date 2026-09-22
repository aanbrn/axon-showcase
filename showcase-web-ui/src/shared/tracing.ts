// SPDX-License-Identifier: MIT

/**
 * A W3C Trace Context `traceparent` for the browser's API calls.
 *
 * <p>The trace id is generated once per page load, so every request that page makes joins a single trace; each call
 * gets its own parent id. The sampled flag is set, so the gateway continues the trace and records it rather than
 * dropping it.
 */

/** The trace id shared by every request of this page load (32 lowercase hex characters). */
const TRACE_ID = randomHex(16);

/**
 * Builds a W3C Trace Context `traceparent` header value for one request.
 *
 * @returns the header value, `00-<32 hex trace id>-<16 hex parent id>-01`
 */
export function traceparent(): string {
  return `00-${TRACE_ID}-${randomHex(8)}-01`;
}

/**
 * Generates a lowercase hex string of the given byte length.
 *
 * @param bytes the number of random bytes
 * @returns `bytes * 2` lowercase hex characters
 */
function randomHex(bytes: number): string {
  const values = crypto.getRandomValues(new Uint8Array(bytes));
  return Array.from(values, (value) => value.toString(16).padStart(2, '0')).join('');
}
