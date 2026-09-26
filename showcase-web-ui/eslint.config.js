// SPDX-License-Identifier: MIT
import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import reactHooks from 'eslint-plugin-react-hooks';
import prettier from 'eslint-config-prettier';
import header from '@tony.ganchev/eslint-plugin-header';
import boundaries from 'eslint-plugin-boundaries';

export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'build', '.gradle'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: { browser: true },
    },
    plugins: {
      'react-hooks': reactHooks,
      header,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'header/header': ['error', 'line', [' SPDX-License-Identifier: MIT']],
    },
  },
  {
    files: ['**/*.{ts,tsx}'],
    plugins: { boundaries },
    settings: {
      'import/resolver': { typescript: { alwaysTryTypes: true } },
      'boundaries/ignore': ['src/main.tsx', 'src/test-setup.ts', 'src/vite-env.d.ts', '**/*.test.ts', '**/*.test.tsx'],
      'boundaries/elements': [
        { type: 'shared', pattern: 'src/shared' },
        { type: 'entity', pattern: 'src/entities/*', capture: ['slice'] },
        { type: 'feature', pattern: 'src/features/*', capture: ['slice'] },
        { type: 'widget', pattern: 'src/widgets/*', capture: ['slice'] },
        { type: 'page', pattern: 'src/pages/*', capture: ['slice'] },
        { type: 'app', pattern: 'src/app' },
      ],
    },
    rules: {
      'boundaries/no-unknown-files': ['error'],
      'boundaries/dependencies': [
        'error',
        {
          default: 'disallow',
          policies: [
            {
              from: { element: { type: 'entity' } },
              allow: { to: { element: { type: 'shared', fileInternalPath: 'index.ts' } } },
            },
            {
              from: { element: { type: 'entity' } },
              allow: { to: { element: { type: 'entity', fileInternalPath: '@x/*' } } },
            },
            {
              from: { element: { type: 'feature' } },
              allow: { to: { element: { type: 'feature', fileInternalPath: '@x/*' } } },
            },
            {
              from: { element: { type: 'widget' } },
              allow: { to: { element: { type: 'widget', fileInternalPath: '@x/*' } } },
            },
            {
              from: { element: { type: 'page' } },
              allow: { to: { element: { type: 'page', fileInternalPath: '@x/*' } } },
            },
            {
              from: { element: { type: 'feature' } },
              allow: {
                to: {
                  element: { types: { anyOf: ['shared', 'entity'] }, fileInternalPath: 'index.ts' },
                },
              },
            },
            {
              from: { element: { type: 'widget' } },
              allow: {
                to: {
                  element: { types: { anyOf: ['shared', 'entity', 'feature'] }, fileInternalPath: 'index.ts' },
                },
              },
            },
            {
              from: { element: { type: 'page' } },
              allow: {
                to: {
                  element: {
                    types: { anyOf: ['shared', 'entity', 'feature', 'widget'] },
                    fileInternalPath: 'index.ts',
                  },
                },
              },
            },
            {
              from: { element: { type: 'app' } },
              allow: {
                to: {
                  element: {
                    types: { anyOf: ['shared', 'entity', 'feature', 'widget', 'page'] },
                    fileInternalPath: 'index.ts',
                  },
                },
              },
            },
          ],
        },
      ],
    },
  },
  prettier,
);
