// SPDX-License-Identifier: MIT
import { expect, test } from '@playwright/test';

/**
 * Browser end-to-end tests for the showcase web UI against the real stack.
 *
 * <p>These exercise the actual CQRS pipeline through the UI: the form talks to the gateway, the gateway dispatches to
 * the command side, the projection updates OpenSearch, and the UI reconciles the list via the SSE event stream.
 */

function fillStartTime(minutesFromNow: number) {
  const pad = (n: number) => String(n).padStart(2, '0');
  const future = new Date(Date.now() + minutesFromNow * 60_000);
  return `${future.getFullYear()}-${pad(future.getMonth() + 1)}-${pad(future.getDate())}T${pad(future.getHours())}:${pad(future.getMinutes())}`;
}

async function scheduleShowcase(page: import('@playwright/test').Page, title: string, minutesFromNow: number) {
  await page.goto('/');
  await page.getByPlaceholder('Title').fill(title);
  await page.getByLabel('Start time').fill(fillStartTime(minutesFromNow));
  await page.getByRole('button', { name: 'Schedule', exact: true }).click();
  const row = page.getByRole('button', { name: new RegExp(`^${title} `) });
  await expect(row).toBeVisible({ timeout: 30_000 });
  return row;
}

test('creates a showcase that appears in the list', async ({ page }) => {
  const title = `E2E-${Date.now()}`;
  const row = await scheduleShowcase(page, title, 30);

  await expect(row).toContainText('SCHEDULED');
});

test('starts a showcase and reflects the STARTED status', async ({ page }) => {
  const title = `E2E-${Date.now()}`;
  const row = await scheduleShowcase(page, title, 30);

  await row.click();
  await page.getByRole('button', { name: 'Start', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Finish', exact: true })).toBeVisible({ timeout: 30_000 });
});

test('saga auto-starts a scheduled showcase and the list reflects it over SSE', async ({ page }) => {
  const title = `Saga-${Date.now()}`;
  const row = await scheduleShowcase(page, title, 1);

  await expect(row).toContainText('SCHEDULED');
  await expect(row).toContainText('STARTED', { timeout: 90_000 });
});

test('live events append to the selected showcase timeline', async ({ page }) => {
  const title = `Timeline-${Date.now()}`;
  const row = await scheduleShowcase(page, title, 1);

  await row.click();
  await expect(page.getByRole('heading', { name: title })).toBeVisible({ timeout: 30_000 });
  await expect(page.getByText('Scheduled', { exact: true })).toBeVisible({ timeout: 30_000 });
  await expect(page.getByText('Started', { exact: true })).toBeVisible({ timeout: 90_000 });
});

test('a duplicate title surfaces the gateway validation error', async ({ page }) => {
  const title = `Dup-${Date.now()}`;
  await scheduleShowcase(page, title, 30);

  await page.getByPlaceholder('Title').fill(title);
  await page.getByLabel('Start time').fill(fillStartTime(30));
  await page.getByRole('button', { name: 'Schedule', exact: true }).click();

  await expect(page.getByText('Given title is in use already')).toBeVisible({ timeout: 30_000 });
});
