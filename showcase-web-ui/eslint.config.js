import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import reactHooks from 'eslint-plugin-react-hooks';
import prettier from 'eslint-config-prettier';
import header from '@tony.ganchev/eslint-plugin-header';

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
  prettier,
);
