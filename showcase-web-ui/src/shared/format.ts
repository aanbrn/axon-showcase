// SPDX-License-Identifier: MIT
/**
 * Formats an ISO-8601 timestamp as a locale time string, omitting the date.
 *
 * <p>Used for timeline markers where only the time of day is relevant.
 *
 * @param value the ISO-8601 timestamp to format
 * @returns the locale time string, or the raw value when the timestamp cannot be parsed
 */
export function formatTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleTimeString();
}

/**
 * Formats an ISO-8601 timestamp as a locale date and time string.
 *
 * <p>Used where the full moment matters, such as a showcase's scheduled start time in the list.
 *
 * @param value the ISO-8601 timestamp to format
 * @returns the locale date-time string, or the raw value when the timestamp cannot be parsed
 */
export function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

/**
 * Formats an ISO-8601 minute duration (e.g. "PT5M") as a human-readable "5 min".
 *
 * <p>Only minute-precision durations used by the showcase domain are recognized; anything else is returned unchanged.
 *
 * @param value the ISO-8601 duration to format
 * @returns the formatted duration, or the raw value for unrecognized formats
 */
export function formatDuration(value: string): string {
  const match = /^PT(\d+)M$/.exec(value);
  return match ? `${match[1]} min` : value;
}
