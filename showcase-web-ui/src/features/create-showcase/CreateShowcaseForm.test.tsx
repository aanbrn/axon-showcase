// SPDX-License-Identifier: MIT
import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CreateShowcaseForm, toDateTimeLocalValue } from './CreateShowcaseForm';

const FUTURE = '2030-09-02T10:00';

describe('toDateTimeLocalValue', () => {
  it('formats a date as a datetime-local value with zero-padded fields', () => {
    expect(toDateTimeLocalValue(new Date(2026, 8, 2, 9, 5))).toBe('2026-09-02T09:05');
    expect(toDateTimeLocalValue(new Date(2026, 0, 3, 23, 59))).toBe('2026-01-03T23:59');
  });
});

describe('CreateShowcaseForm', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('offers durations from 1 to 10 minutes with 5 selected by default', () => {
    render(<CreateShowcaseForm onSubmit={vi.fn()} busy={false} />);
    const select = screen.getByLabelText('Duration') as HTMLSelectElement;

    const labels = [...select.options].map((option) => option.textContent);
    expect(labels).toEqual([
      '1 minutes',
      '2 minutes',
      '3 minutes',
      '4 minutes',
      '5 minutes',
      '6 minutes',
      '7 minutes',
      '8 minutes',
      '9 minutes',
      '10 minutes',
    ]);
    expect(select.value).toBe('PT5M');
  });

  it('prefills the start time with the next minute', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 8, 2, 9, 5, 30));
    render(<CreateShowcaseForm onSubmit={vi.fn()} busy={false} />);
    expect((screen.getByLabelText('Start time') as HTMLInputElement).value).toBe('2026-09-02T09:06');
  });

  it('rolls the picker to the next minute at each minute boundary', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 8, 2, 9, 5, 30));
    render(<CreateShowcaseForm onSubmit={vi.fn()} busy={false} />);

    vi.advanceTimersByTime(30_000);
    expect((screen.getByLabelText('Start time') as HTMLInputElement).value).toBe('2026-09-02T09:07');

    vi.advanceTimersByTime(60_000);
    expect((screen.getByLabelText('Start time') as HTMLInputElement).value).toBe('2026-09-02T09:08');
  });

  it('resets the start time to the current next minute after a submit', async () => {
    const onSubmit = vi.fn();
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 8, 2, 9, 5, 30));
    render(<CreateShowcaseForm onSubmit={onSubmit} busy={false} />);

    const titleInput = screen.getByPlaceholderText('Title');
    fireEvent.change(titleInput, { target: { value: 'My Showcase' } });
    fireEvent.submit(screen.getByRole('form', { name: 'Create showcase' }));

    await vi.advanceTimersByTimeAsync(0);
    expect(onSubmit).toHaveBeenCalled();
    vi.advanceTimersByTime(2 * 60_000);
    expect((screen.getByLabelText('Start time') as HTMLInputElement).value).toBe('2026-09-02T09:08');
  });

  it('stops auto-refreshing once the start time is edited', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 8, 2, 9, 5, 30));
    render(<CreateShowcaseForm onSubmit={vi.fn()} busy={false} />);

    const input = screen.getByLabelText('Start time') as HTMLInputElement;
    input.focus();
    fireEvent.change(input, { target: { value: '2030-09-02T10:00' } });

    vi.advanceTimersByTime(120_000);
    expect(input.value).toBe('2030-09-02T10:00');
  });

  it('submits the form fields and resets them', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();

    render(<CreateShowcaseForm onSubmit={onSubmit} busy={false} />);
    await user.clear(screen.getByLabelText('Start time'));
    await user.type(screen.getByPlaceholderText('Title'), 'My Showcase');
    await user.type(screen.getByLabelText('Start time'), FUTURE);
    await user.click(screen.getByRole('button', { name: 'Schedule' }));

    expect(onSubmit).toHaveBeenCalledWith({
      title: 'My Showcase',
      startTime: expect.stringMatching(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/),
      duration: 'PT5M',
    });
    const submittedStartTime = onSubmit.mock.calls[0][0].startTime as string;
    expect(new Date(submittedStartTime).getTime()).toBe(new Date(FUTURE).getTime());
    expect(screen.getByPlaceholderText('Title')).toHaveValue('');
  });

  it('shows a validation error for an empty title', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();

    render(<CreateShowcaseForm onSubmit={onSubmit} busy={false} />);
    await user.clear(screen.getByLabelText('Start time'));
    await user.click(screen.getByRole('button', { name: 'Schedule' }));

    expect(screen.getByText('Title is required')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('rejects a past start time', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();

    render(<CreateShowcaseForm onSubmit={onSubmit} busy={false} />);
    await user.type(screen.getByPlaceholderText('Title'), 'My Showcase');
    await user.clear(screen.getByLabelText('Start time'));
    await user.type(screen.getByLabelText('Start time'), '2020-01-01T10:00');
    await user.click(screen.getByRole('button', { name: 'Schedule' }));

    expect(screen.getByText('Start time must be in the future')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('disables the submit button while busy', () => {
    render(<CreateShowcaseForm onSubmit={vi.fn()} busy={true} />);
    expect(screen.getByRole('button', { name: 'Creating...' })).toBeDisabled();
  });
});
