// SPDX-License-Identifier: MIT
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { Timeline } from './Timeline';
import type { TimelineEntry } from '@/entities/showcase/lib/timelineEntries';

describe('Timeline', () => {
  it('shows a placeholder when there are no entries', () => {
    render(<Timeline entries={[]} />);
    expect(screen.getByText('No timeline entries yet.')).toBeInTheDocument();
  });

  it('renders each entry with its label', () => {
    const entries: TimelineEntry[] = [
      { id: 'scheduled', label: 'Scheduled', time: '2026-09-02T10:00:00Z' },
      { id: 'started', label: 'Started', time: '2026-09-02T10:05:00Z' },
    ];
    render(<Timeline entries={entries} />);
    expect(screen.getByText('Scheduled')).toBeInTheDocument();
    expect(screen.getByText('Started')).toBeInTheDocument();
  });
});
