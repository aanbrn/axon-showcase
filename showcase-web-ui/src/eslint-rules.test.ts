// SPDX-License-Identifier: MIT
import { ESLint } from 'eslint';
import { describe, expect, it } from 'vitest';

const HEADER = '// SPDX-License-Identifier: MIT\n';
const NAMING_RULE = '@typescript-eslint/naming-convention';

/**
 * Lints a snippet against the module's real flat config and returns every rule id it reports.
 *
 * <p>The config is loaded by path (`overrideConfigFile`) rather than imported: the tsconfig covers `src` with no
 * `allowJs`, so a static import of the config would fail the type-check.
 */
async function lintViolations(code: string, filePath: string): Promise<string[]> {
  const eslint = new ESLint({ overrideConfigFile: 'eslint.config.js' });
  const results = await eslint.lintText(HEADER + code, { filePath });
  return results.flatMap((result) => result.messages.map((message) => message.ruleId ?? ''));
}

describe('web UI import boundaries', () => {
  it('rejects a deep cross-slice import', async () => {
    const violations = await lintViolations(
      "import type { Showcase } from '@/entities/showcase/types';\n",
      'src/pages/showcases/Deep.tsx',
    );

    expect(violations).toContain('boundaries/dependencies');
  });

  it('rejects an upward-layer import', async () => {
    const violations = await lintViolations("import { store } from '@/app/store';\n", 'src/entities/showcase/Up.ts');

    expect(violations).toContain('boundaries/dependencies');
  });

  it('rejects an undeclared sibling-slice import', async () => {
    const violations = await lintViolations(
      "import { connectEventStream } from '@/entities/showcase-event';\n",
      'src/entities/showcase/Sibling.ts',
    );

    expect(violations).toContain('boundaries/dependencies');
  });

  it('rejects a source file that matches no layer or slice', async () => {
    const violations = await lintViolations('export const stray = 1;\n', 'src/stray.ts');

    expect(violations).toContain('boundaries/no-unknown-files');
  });

  it('accepts a public-API downward import', async () => {
    const violations = await lintViolations(
      "import { useShowcases } from '@/entities/showcase';\nexport const read = useShowcases;\n",
      'src/pages/showcases/Ok.tsx',
    );

    expect(violations).toEqual([]);
  });

  it('accepts a declared cross-import through the @x API', async () => {
    const violations = await lintViolations(
      "import type { ShowcaseEvent } from '@/entities/showcase-event/@x/showcase';\n" +
        'export const event: ShowcaseEvent | null = null;\n',
      'src/entities/showcase/Cross.ts',
    );

    expect(violations).toEqual([]);
  });
});

describe('web UI naming conventions', () => {
  it('rejects a lowercase-leading function not in strictCamelCase', async () => {
    const violations = await lintViolations(
      'export function bad_function() {\n  return 1;\n}\n',
      'src/pages/showcases/BadFunction.ts',
    );

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects an uppercase-leading function not in StrictPascalCase', async () => {
    const violations = await lintViolations(
      'export function Bad_function() {\n  return 1;\n}\n',
      'src/pages/showcases/BadFunctionUpper.ts',
    );

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects a lowercase-leading variable not in strictCamelCase', async () => {
    const violations = await lintViolations('export const bad_variable = 1;\n', 'src/pages/showcases/BadVariable.ts');

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects an uppercase-leading variable in neither UPPER_CASE nor StrictPascalCase', async () => {
    const violations = await lintViolations(
      'export const Bad_Variable = 1;\n',
      'src/pages/showcases/BadVariableUpper.ts',
    );

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects a parameter not in strictCamelCase', async () => {
    const violations = await lintViolations(
      'export function withParam(bad_param: number) {\n  return bad_param;\n}\n',
      'src/pages/showcases/BadParam.ts',
    );

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects a used underscore-prefixed parameter', async () => {
    const violations = await lintViolations(
      'export function usedParam(_x: number) {\n  return _x;\n}\n',
      'src/pages/showcases/UsedUnderscore.ts',
    );

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects a type alias not in StrictPascalCase', async () => {
    const violations = await lintViolations('export type bad_type = string;\n', 'src/pages/showcases/BadType.ts');

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects an interface not in StrictPascalCase', async () => {
    const violations = await lintViolations(
      'export interface bad_interface {\n  value: number;\n}\n',
      'src/pages/showcases/BadInterface.ts',
    );

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects a type parameter not in StrictPascalCase', async () => {
    const violations = await lintViolations(
      'export function identity<bad_t>(value: bad_t): bad_t {\n  return value;\n}\n',
      'src/pages/showcases/BadTypeParam.ts',
    );

    expect(violations).toContain(NAMING_RULE);
  });

  it('rejects a hook called outside a use* function', async () => {
    const violations = await lintViolations(
      "import { useState } from 'react';\n\nexport function badHook() {\n  return useState(0);\n}\n",
      'src/pages/showcases/BadHook.ts',
    );

    expect(violations).toContain('react-hooks/rules-of-hooks');
  });

  it('rejects a unit test named *.spec.ts that Vitest will not collect', async () => {
    const violations = await lintViolations('export const value = 1;\n', 'src/pages/showcases/Dead.spec.ts');

    expect(violations).toContain('no-restricted-syntax');
  });

  it('rejects a unit test named *.spec.tsx that Vitest will not collect', async () => {
    const violations = await lintViolations('export const value = 1;\n', 'src/pages/showcases/Dead.spec.tsx');

    expect(violations).toContain('no-restricted-syntax');
  });

  it('accepts conforming names', async () => {
    const violations = await lintViolations(
      [
        'export const MAX_RETRIES = 3;',
        'export const ShowcaseCard = () => null;',
        'export function formatTitle(title: string): string {',
        '  return title;',
        '}',
        'export function useTitle(title: string): string {',
        '  return formatTitle(title);',
        '}',
        'export type ShowcaseTitle = string;',
        'export interface ShowcaseConfig {',
        '  title: ShowcaseTitle;',
        '}',
        'export function identity<T>(value: T): T {',
        '  return value;',
        '}',
        '',
      ].join('\n'),
      'src/pages/showcases/Conforming.ts',
    );

    expect(violations).toEqual([]);
  });

  it('accepts an unused underscore parameter', async () => {
    const violations = await lintViolations(
      'export function ignoreSecond(value: number, _: number): number {\n  return value;\n}\n',
      'src/pages/showcases/UnusedUnderscore.ts',
    );

    expect(violations).toEqual([]);
  });

  it('accepts a unit test named *.test.ts', async () => {
    const violations = await lintViolations('export const value = 1;\n', 'src/pages/showcases/Collected.test.ts');

    expect(violations).toEqual([]);
  });

  it('accepts a unit test named *.test.tsx', async () => {
    const violations = await lintViolations('export const value = 1;\n', 'src/pages/showcases/Collected.test.tsx');

    expect(violations).toEqual([]);
  });
});
