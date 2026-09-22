// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import { traceparent } from './tracing';

describe('traceparent', () => {
  it('has the W3C Trace Context shape with the sampled flag set', () => {
    expect(traceparent()).toMatch(/^00-[0-9a-f]{32}-[0-9a-f]{16}-01$/);
  });

  it('keeps one trace id per page load with a distinct parent id per call', () => {
    const first = traceparent();
    const second = traceparent();
    const traceId = (value: string) => value.split('-')[1];
    const parentId = (value: string) => value.split('-')[2];

    expect(traceId(first)).toBe(traceId(second));
    expect(parentId(first)).not.toBe(parentId(second));
  });
});
