// SPDX-License-Identifier: MIT
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { type PropsWithChildren } from 'react';
import { Provider } from 'react-redux';
import { afterEach, describe, expect, it, vi } from 'vitest';
import * as api from '@/features/create-showcase/api';
import * as entityApi from '@/entities/showcase/api';
import type { Showcase } from '@/entities/showcase/types';
import { store } from '@/app/store';
import { ShowcasesPage } from './ShowcasesPage';

vi.mock('@/entities/showcase-event/api/eventStream', () => ({
  connectEventStream: () => () => {},
}));

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  return function Wrapper({ children }: PropsWithChildren) {
    return (
      <Provider store={store}>
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      </Provider>
    );
  };
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe('ShowcasesPage', () => {
  it('renders the empty state when there are no showcases', async () => {
    vi.spyOn(entityApi, 'fetchShowcases').mockResolvedValue([]);
    render(<ShowcasesPage />, { wrapper: createWrapper() });

    expect(await screen.findByText('No showcases yet.')).toBeInTheDocument();
    expect(await screen.findByText('Select a showcase to see its timeline.')).toBeInTheDocument();
  });

  it('lists showcases and shows details for the selected one', async () => {
    const showcase: Showcase = {
      showcaseId: '1',
      title: 'Demo',
      startTime: '2026-09-02T10:00:00Z',
      duration: 'PT5M',
      status: 'SCHEDULED',
      scheduledAt: '2026-09-02T10:00:00Z',
    };
    vi.spyOn(entityApi, 'fetchShowcases').mockResolvedValue([showcase]);
    const user = userEvent.setup();
    render(<ShowcasesPage />, { wrapper: createWrapper() });

    expect(await screen.findByText('Demo')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Demo/ }));

    expect(screen.getByRole('heading', { name: 'Demo' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Start' })).toBeInTheDocument();
  });

  it('schedules a showcase and resets the form', async () => {
    vi.spyOn(entityApi, 'fetchShowcases').mockResolvedValue([]);
    const scheduleSpy = vi.spyOn(api, 'scheduleShowcase').mockResolvedValue({ status: 'scheduled', showcaseId: '1' });
    const user = userEvent.setup();
    render(<ShowcasesPage />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('Title'), 'My Showcase');
    await user.clear(screen.getByLabelText('Start time'));
    await user.type(screen.getByLabelText('Start time'), '2030-09-02T10:00');
    await user.click(screen.getByRole('button', { name: 'Schedule' }));

    expect(scheduleSpy).toHaveBeenCalledWith({
      title: 'My Showcase',
      startTime: expect.stringMatching(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/),
      duration: 'PT5M',
    });
    const submittedStartTime = scheduleSpy.mock.calls[0][0].startTime as string;
    expect(new Date(submittedStartTime).getTime()).toBe(new Date('2030-09-02T10:00').getTime());
    await waitFor(() => expect(screen.getByPlaceholderText('Title')).toHaveValue(''));
  });
});
