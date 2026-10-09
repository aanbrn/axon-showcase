import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vitest/config';

// The baseline stores a fraction (0.80, mirroring config/jacoco/coverage-baseline.properties); Vitest's thresholds are
// percentages (0-100), so the fraction is scaled here.
const coverageMinimumPercent = Number(process.env.VITEST_COVERAGE_MIN ?? '0.80') * 100;

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/showcases': 'http://localhost:8080',
      '/events': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/telemetry': 'http://localhost:8080',
    },
  },
  build: {
    outDir: 'build/dist',
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['src/test-setup.ts'],
    include: ['src/**/*.test.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'lcov'],
      reportsDirectory: 'build/coverage',
      thresholds: {
        statements: coverageMinimumPercent,
      },
    },
  },
});
