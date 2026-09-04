// SPDX-License-Identifier: MIT
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { ShowcaseDetail } from './ShowcaseDetail';
import type { Showcase } from '@/entities/showcase/types';

const showcase: Showcase = {
  showcaseId: '1',
  title: 'Demo',
  startTime: '2026-09-02T10:00:00Z',
  duration: 'PT5M',
  status: 'SCHEDULED',
  scheduledAt: '2026-09-02T10:00:00Z',
};

const timeline = [{ id: 'scheduled', label: 'Scheduled', time: '2026-09-02T10:00:00Z' }];

describe('ShowcaseDetail', () => {
  it('renders the showcase title and timeline', () => {
    render(
      <ShowcaseDetail
        showcase={showcase}
        timeline={timeline}
        onStart={vi.fn()}
        onFinish={vi.fn()}
        onRemove={vi.fn()}
      />,
    );
    expect(screen.getByRole('heading', { name: 'Demo' })).toBeInTheDocument();
    expect(screen.getByText('Scheduled')).toBeInTheDocument();
  });

  it('shows Start for a scheduled showcase and invokes onStart', () => {
    const onStart = vi.fn();
    render(
      <ShowcaseDetail
        showcase={showcase}
        timeline={timeline}
        onStart={onStart}
        onFinish={vi.fn()}
        onRemove={vi.fn()}
      />,
    );
    const startButton = screen.getByRole('button', { name: 'Start' });
    expect(startButton).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Finish' })).not.toBeInTheDocument();
    fireEvent.click(startButton);
    expect(onStart).toHaveBeenCalled();
  });

  it('shows Finish for a started showcase', () => {
    render(
      <ShowcaseDetail
        showcase={{ ...showcase, status: 'STARTED' }}
        timeline={timeline}
        onStart={vi.fn()}
        onFinish={vi.fn()}
        onRemove={vi.fn()}
      />,
    );
    expect(screen.getByRole('button', { name: 'Finish' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Start' })).not.toBeInTheDocument();
  });

  it('always shows Remove and invokes onRemove', () => {
    const onRemove = vi.fn();
    render(
      <ShowcaseDetail
        showcase={showcase}
        timeline={timeline}
        onStart={vi.fn()}
        onFinish={vi.fn()}
        onRemove={onRemove}
      />,
    );
    fireEvent.click(screen.getByRole('button', { name: 'Remove' }));
    expect(onRemove).toHaveBeenCalled();
  });
});
