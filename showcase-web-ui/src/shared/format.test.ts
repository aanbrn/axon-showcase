// SPDX-License-Identifier: MIT
import { describe, expect, it } from 'vitest';
import { formatDuration, formatTime } from './format';

describe('formatTime', () => {
  it('formats a date as a locale time string', () => {
    expect(formatTime('2026-09-02T10:05:00.000Z')).toMatch(/\d{1,2}:\d{2}/);
  });

  it('returns the raw value for an unparseable date', () => {
    expect(formatTime('not-a-date')).toBe('not-a-date');
  });
});

describe('formatDuration', () => {
  it('formats an ISO-8601 minute duration', () => {
    expect(formatDuration('PT5M')).toBe('5 min');
    expect(formatDuration('PT10M')).toBe('10 min');
  });

  it('returns the raw value for an unknown duration format', () => {
    expect(formatDuration('PT30S')).toBe('PT30S');
  });
});
