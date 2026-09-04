// SPDX-License-Identifier: MIT
import { defineConfig, devices } from '@playwright/test';

/**
 * End-to-end tests for the web UI, run against the real stack.
 *
 * <p>Assumes the full service stack (infra + four services) is already running — the Gradle `e2eTest` task boots it
 * via docker-compose before Playwright starts. The UI is served from the production build (`vite preview`), whose
 * proxy forwards `/showcases` and `/events` to the gateway on :8080.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  timeout: 60_000,
  retries: 0,
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:4173',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run preview',
    url: 'http://localhost:4173',
    reuseExistingServer: true,
    timeout: 30_000,
  },
});
