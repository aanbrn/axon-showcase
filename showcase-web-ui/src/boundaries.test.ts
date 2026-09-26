// SPDX-License-Identifier: MIT
import { ESLint } from 'eslint';
import { describe, expect, it } from 'vitest';

const HEADER = '// SPDX-License-Identifier: MIT\n';

/**
 * Lints a snippet against the module's real flat config and returns the boundary rule ids it reports.
 *
 * <p>The config is loaded by path (`overrideConfigFile`) rather than imported: the tsconfig covers `src` with no
 * `allowJs`, so a static import of the config would fail the type-check.
 */
async function boundaryViolations(code: string, filePath: string): Promise<string[]> {
  const eslint = new ESLint({ overrideConfigFile: 'eslint.config.js' });
  const results = await eslint.lintText(HEADER + code, { filePath });
  return results
    .flatMap((result) => result.messages.map((message) => message.ruleId ?? ''))
    .filter((ruleId) => ruleId.startsWith('boundaries/'));
}

describe('web UI import boundaries', () => {
  it('rejects a deep cross-slice import', async () => {
    const violations = await boundaryViolations(
      "import type { Showcase } from '@/entities/showcase/types';\n",
      'src/pages/showcases/Deep.tsx',
    );

    expect(violations).toContain('boundaries/dependencies');
  });

  it('rejects an upward-layer import', async () => {
    const violations = await boundaryViolations(
      "import { store } from '@/app/store';\n",
      'src/entities/showcase/Up.ts',
    );

    expect(violations).toContain('boundaries/dependencies');
  });

  it('rejects an undeclared sibling-slice import', async () => {
    const violations = await boundaryViolations(
      "import { connectEventStream } from '@/entities/showcase-event';\n",
      'src/entities/showcase/Sibling.ts',
    );

    expect(violations).toContain('boundaries/dependencies');
  });

  it('rejects a source file that matches no layer or slice', async () => {
    const violations = await boundaryViolations('export const stray = 1;\n', 'src/stray.ts');

    expect(violations).toContain('boundaries/no-unknown-files');
  });

  it('accepts a public-API downward import', async () => {
    const violations = await boundaryViolations(
      "import { useShowcases } from '@/entities/showcase';\n",
      'src/pages/showcases/Ok.tsx',
    );

    expect(violations).toEqual([]);
  });

  it('accepts a declared cross-import through the @x API', async () => {
    const violations = await boundaryViolations(
      "import type { ShowcaseEvent } from '@/entities/showcase-event/@x/showcase';\n",
      'src/entities/showcase/Cross.ts',
    );

    expect(violations).toEqual([]);
  });
});
