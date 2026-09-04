// SPDX-License-Identifier: MIT
import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

const DURATIONS = Array.from({ length: 10 }, (_, i) => `PT${i + 1}M`);

const startTimeSchema = z
  .string()
  .min(1, 'Start time is required')
  .refine((value) => new Date(value).getTime() > Date.now(), 'Start time must be in the future')
  .transform((value) => new Date(value).toISOString());

const showcaseSchema = z.object({
  title: z.string().trim().min(1, 'Title is required').max(255),
  startTime: startTimeSchema,
  duration: z.enum(DURATIONS as [string, ...string[]]),
});

export type ShowcaseFormValues = z.infer<typeof showcaseSchema>;

/**
 * Formats a date as a datetime-local input value.
 *
 * <p>Produces the "YYYY-MM-DDTHH:mm" format the datetime-local input expects, zero-padding month, day, hour, and minute
 * and dropping seconds.
 *
 * @param date the date to format
 * @returns the value in "YYYY-MM-DDTHH:mm" format
 */
export function toDateTimeLocalValue(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

/**
 * The default start time for the picker: the next minute from now.
 *
 * <p>Prefilled to a future moment so a scheduled showcase does not start immediately via the saga.
 */
function defaultStartTime(): string {
  const now = new Date();
  now.setSeconds(0, 0);
  now.setMinutes(now.getMinutes() + 1);
  return toDateTimeLocalValue(now);
}

/**
 * The showcase creation form: title, future start time, and duration.
 *
 * <p>Validates the title length and requires a future start time (the raw wall-clock value is converted to an ISO-8601
 * instant on submit). The start-time picker pre-fills with the next minute and rolls forward at each minute boundary
 * until the field is edited, so it always shows a future time when untouched.
 */
export function CreateShowcaseForm(props: { onSubmit: (form: ShowcaseFormValues) => void; busy: boolean }) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, dirtyFields },
  } = useForm<ShowcaseFormValues>({
    resolver: zodResolver(showcaseSchema),
    defaultValues: { title: '', startTime: defaultStartTime(), duration: 'PT5M' },
  });

  useEffect(() => {
    if (dirtyFields.startTime) {
      return;
    }
    let timer: number;
    const refreshAtMinuteBoundary = () => {
      const now = new Date();
      const nextBoundary = new Date(now);
      nextBoundary.setSeconds(0, 0);
      nextBoundary.setMinutes(nextBoundary.getMinutes() + 1);
      timer = window.setTimeout(() => {
        setValue('startTime', defaultStartTime());
        refreshAtMinuteBoundary();
      }, nextBoundary.getTime() - now.getTime());
    };
    refreshAtMinuteBoundary();
    return () => clearTimeout(timer);
  }, [dirtyFields.startTime, setValue]);

  return (
    <form
      className="create"
      aria-label="Create showcase"
      onSubmit={handleSubmit((values) => {
        props.onSubmit(values);
        reset({ title: '', startTime: defaultStartTime(), duration: 'PT5M' });
      })}
    >
      <input type="text" placeholder="Title" aria-label="Title" {...register('title')} />
      {errors.title && <span className="field-error">{errors.title.message}</span>}
      <input type="datetime-local" aria-label="Start time" {...register('startTime')} />
      {errors.startTime && <span className="field-error">{errors.startTime.message}</span>}
      <select aria-label="Duration" {...register('duration')}>
        {DURATIONS.map((duration) => (
          <option key={duration} value={duration}>
            {Number(duration.slice(2, -1))} minutes
          </option>
        ))}
      </select>
      <button type="submit" disabled={props.busy}>
        {props.busy ? 'Creating...' : 'Schedule'}
      </button>
    </form>
  );
}
