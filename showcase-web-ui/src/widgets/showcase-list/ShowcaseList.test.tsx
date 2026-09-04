// SPDX-License-Identifier: MIT
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { ShowcaseList } from './ShowcaseList';
import type { Showcase } from '@/entities/showcase/types';
import { formatDateTime } from '@/shared/format';

const showcase: Showcase = {
  showcaseId: '1',
  title: 'Demo',
  startTime: '2026-09-02T10:00:00Z',
  duration: 'PT5M',
  status: 'SCHEDULED',
  scheduledAt: '2026-09-02T10:00:00Z',
};

describe('ShowcaseList', () => {
  it('renders each showcase with its status and scheduled time', () => {
    render(<ShowcaseList showcases={[showcase]} selectedId={null} onSelect={vi.fn()} />);
    expect(screen.getByText('Demo')).toBeInTheDocument();
    expect(screen.getByText('SCHEDULED')).toBeInTheDocument();
    expect(screen.getByText(`Start at: ${formatDateTime('2026-09-02T10:00:00Z')} · 5 min`)).toBeInTheDocument();
  });

  it('shows the finish time for a started showcase', () => {
    render(<ShowcaseList showcases={[{ ...showcase, status: 'STARTED' }]} selectedId={null} onSelect={vi.fn()} />);
    expect(screen.getByText(`Finish at: ${formatDateTime('2026-09-02T10:05:00.000Z')} · 5 min`)).toBeInTheDocument();
  });

  it('shows the finished time for a finished showcase', () => {
    render(
      <ShowcaseList
        showcases={[{ ...showcase, status: 'FINISHED', finishedAt: '2026-09-02T10:10:00Z' }]}
        selectedId={null}
        onSelect={vi.fn()}
      />,
    );
    expect(screen.getByText(`Finished at: ${formatDateTime('2026-09-02T10:10:00Z')} · 5 min`)).toBeInTheDocument();
  });

  it('invokes onSelect with the showcase id', () => {
    const onSelect = vi.fn();
    render(<ShowcaseList showcases={[showcase]} selectedId={null} onSelect={onSelect} />);
    fireEvent.click(screen.getByRole('button', { name: /Demo/ }));
    expect(onSelect).toHaveBeenCalledWith('1');
  });

  it('marks the selected showcase as active', () => {
    render(<ShowcaseList showcases={[showcase]} selectedId="1" onSelect={vi.fn()} />);
    expect(screen.getByRole('button', { name: /Demo/ }).closest('li')).toHaveClass('active');
  });
});
