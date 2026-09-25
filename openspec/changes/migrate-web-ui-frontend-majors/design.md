# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach:

- `showcase-web-ui/package.json` pins `react`/`react-dom` `^18.3.1`, `@types/react` `^18.3.12`, `@types/react-dom`
  `^18.3.1`, `@vitejs/plugin-react` `^4.3.4`, `vite` `^6.4.3`, `vitest` `^3.2.5`, `jsdom` `^25.0.1`, `typescript`
  `^5.7.2` (resolving `5.9.3`); there is no `.npmrc`, so npm's default peer resolution applies.
- Node is pinned at `24.20.0`, which satisfies every target's `engines` (Vite 8 `^20.19 || >=22.12`, Vitest 5
  `^22.12 || ^24 || >=26`, jsdom 30 `^22.22.2 || ^24.15 || >=26`).
- The peer graph forces the pairs: `@vitejs/plugin-react` 6 requires Vite `^8`; `react-dom` 19 peers `react ^19.3.0`;
  `@types/react-dom` and `@types/react` move together (the locked `@types/react-dom@18.3.7` peers `@types/react ^18`);
  `vitest` 5 peers Vite `^6.4 || ^7 || ^8` and `jsdom`. `@vitejs/plugin-react` 6's other peers
  (`@rolldown/plugin-babel`, `babel-plugin-react-compiler`, `oxc-transform-react`) are optional.
- The code is React-19-clean in shape: `createRoot` is already used (`src/main.tsx`), and `src` has no
  `ReactDOM.render`/`forwardRef`/`defaultProps`/`propTypes`/`element.ref`/`React.FC`/`JSX.*`/`React.*` usage.
  `@testing-library/react` 16.1 peers `react ^18 || ^19`, and `react-redux` 9.3 / `@tanstack/react-query` 5.103 /
  `react-hook-form` 7.88 all accept React 19.
- `typescript-eslint` (8.70.1) and `@typescript-eslint/parser` declare `typescript: >=4.8.4 <6.1.0`; TS 7 is outside it
  and no longer ships the classic JS API the parser consumes, so the lint stack cannot use it. TypeScript `6.0.3` is the
  newest release inside the range.

## Goals / Non-Goals

**Goals:**

- Move React, Vite, Vitest, jsdom, `@vitejs/plugin-react`, and `@types/react(-dom)` to their current majors.
- Move TypeScript to the newest version the lint stack supports (`6.0.3`).
- Preserve behavior: the `clients/web-ui` requirements (browsing, reconciliation, dispatch, validation, timeline, SSE,
  trace context) must still hold.

**Non-Goals:**

- TypeScript 7 — deferred until typescript-eslint (and the wider tsc ecosystem) supports it.
- Any UI/feature change; this is a toolchain migration.

## Decisions

- **Take the peer-coupled pairs together, and the rest with them.** Three pairs are peer-coupled: `@vitejs/plugin-react`
  6 requires Vite `^8`, `react-dom` 19 peers `react ^19.3.0`, and `@types/react-dom` 19 peers `@types/react ^19.3.0`.
  Vitest 5 and jsdom 30 are independent of those (Vitest 5 accepts Vite `^6.4 || ^7 || ^8` and its `jsdom` peer is
  optional), so a partial bump would resolve — but taking all eight clears the tracker in one change, and no further
  bump is forced (verify with a clean install: a peer warning naming a version we control would mean a missing member of
  a coupled pair).
- **Stop TypeScript at 6.0.3.** It is the newest release inside typescript-eslint's `<6.1.0` range. Rejected: TS 7
  (outside that range, and it no longer ships the classic JS API the lint stack consumes) and staying at `5.9.3` (two
  majors behind for no benefit).
- **Fix fallout minimally, without behavior change.** Any `tsc`/type, lint, Vite, or Vitest adjustments stay mechanical;
  the web UI's behavior is verified by the existing tests and the Playwright e2e.

## Risks / Trade-offs

- **Vite 8 is rolldown-based** — build/dev/preview internals change → the `build` and the Playwright e2e (which serves
  the built bundle) verify it.
- **Vitest 5 may change test defaults/APIs** → run `:showcase-web-ui:check` and read any failure.
- **`@types/react` 19 tightens some types** (e.g. `useRef` requires an argument) → run `tsc` (`:showcase-web-ui:build`).
- **React 19 changes runtime edges** (StrictMode, ref-as-prop) → the e2e exercises the real UI against the pipeline.

## Migration Plan

- Apply: bump the versions in one `package.json`/lockfile change, then fix whatever the gates report.
- Rollback: revert `package.json`/`package-lock.json` and any fallout edits.
