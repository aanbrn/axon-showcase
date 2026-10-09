// SPDX-License-Identifier: MIT
import { onCLS, onFCP, onINP, onLCP, onTTFB } from 'web-vitals';
import { request } from './api';

/**
 * Client-side observability for the web UI.
 *
 * <p>Measures the Core Web Vitals and captures uncaught errors and unhandled rejections, reporting each to the
 * gateway's telemetry endpoint. Reporting is fire-and-forget: a failed report never affects the page.
 */

/** A single Core Web Vital as sent to the gateway. */
type Vital = { name: string; rating: string; value: number };

/** A single JavaScript error as sent to the gateway. */
type ClientError = { type: string; message: string; source?: string; line?: number; column?: number };

/** The client-telemetry payload sent to the gateway. */
type ClientTelemetryReport = { path: string; vitals: Vital[]; errors: ClientError[] };

/** The Core Web Vitals observers subscribed on initialisation. */
const VITALS = [onLCP, onINP, onCLS, onFCP, onTTFB];

/**
 * Subscribes the Core Web Vitals observers and the uncaught-error listeners.
 *
 * <p>Call once from the application entry point.
 */
export function initClientTelemetry(): void {
  for (const observe of VITALS) {
    observe(reportVital);
  }
  window.addEventListener('error', (event) => reportError(event.error ?? event.message, event));
  window.addEventListener('unhandledrejection', (event) => reportError(event.reason));
}

/**
 * Reports a measured Core Web Vital.
 *
 * @param metric the measured vital
 */
export function reportVital(metric: Vital): void {
  send({
    path: currentRoute(),
    vitals: [{ name: metric.name, rating: metric.rating, value: metric.value }],
    errors: [],
  });
}

/**
 * Reports an uncaught error or unhandled rejection.
 *
 * @param reason the thrown value
 * @param event the originating error event, when available
 */
export function reportError(reason: unknown, event?: ErrorEvent): void {
  send({ path: currentRoute(), vitals: [], errors: [toError(reason, event)] });
}

/**
 * Sends a telemetry report, swallowing any failure so the page is never affected.
 *
 * @param report the report to send
 */
function send(report: ClientTelemetryReport): void {
  void request('/telemetry', {
    method: 'POST',
    keepalive: true,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(report),
  }).catch(() => undefined);
}

/**
 * Reduces an uncaught error to the bounded payload sent to the gateway.
 *
 * @param reason the thrown value
 * @param event the originating error event, when available
 * @returns the error payload
 */
function toError(reason: unknown, event?: ErrorEvent): ClientError {
  if (reason instanceof Error) {
    return { type: reason.name, message: reason.message };
  }
  if (event) {
    return {
      type: 'Error',
      message: event.message,
      source: event.filename,
      line: event.lineno,
      column: event.colno,
    };
  }
  return { type: 'Error', message: String(reason) };
}

/**
 * The current route, sent so telemetry can be grouped by page.
 *
 * @returns the current pathname
 */
function currentRoute(): string {
  return window.location.pathname;
}
