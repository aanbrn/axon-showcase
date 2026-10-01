# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach (verified 2026-10-02):

- `npmOutdated` (`build-logic/src/main/kotlin/frontend-conventions.gradle.kts`) runs `npm run outdated:report`, i.e.
  `showcase-web-ui/scripts/outdated-report.sh`, and then logs `build/npm-outdated.txt` verbatim. The workflow
  (`dependency-updates.yml`) reads that same file into the issue body — so the report's content _is_ the surfaced data,
  and suppressing a row there is what removes it from the issue.
- `npm outdated` on this project prints a whitespace-aligned table whose header is
  `Package  Current  Wanted  Latest  Location  Depended by`; the deferred row today is
  `typescript  6.0.3  6.0.3  7.0.2  node_modules/typescript  showcase-web-ui`. `Wanted` is the newest version the
  manifest's range admits and `Latest` the newest published, so a row is a _major_ update exactly when `Wanted`'s and
  `Latest`'s leading integers differ.
- The JVM precedent: `config/dependency-updates/major-disabled.txt`, parsed by `DependencyUpdateRules` and applied at
  the source — the plugin's `rejectVersionIf` drops a blocked candidate before the report is built. That hook is the
  JVM's lever; the npm side has none, so the filter here runs on the report's text. The rule uses the same _shape_
  (leading-integer comparison, an entry list with comments), on a package name rather than a `group:module`.
- `typescript-eslint` caps TypeScript at `<6.1.0`, which is why the major is deferred; the constraint is
  `package.json`'s.

## Goals / Non-Goals

**Goals:**

- One treatment across both update reports: a suppression list whose entries drop _major-only_ rows while the package's
  same-major updates stay visible.

**Non-Goals:**

- Changing which majors are deferred, or the `typescript` pin.
- Suppressing a package outright (an entry drops only its major-only rows, as on the JVM side).
- The npm `overrides` mechanism (it changes what installs, not what is reported).

## Decisions

- **Filter the report text, not the npm invocation.** `npm outdated` has no suppression flag — its options only shape
  the output (`--json`, `--long`, `--parseable`, `--all`) — and no hook equivalent to the JVM side's `rejectVersionIf`,
  which is what drops a blocked candidate before the report is built. So the filter belongs on the text the task already
  writes, where the row data is. Options considered: `npm outdated --json` plus a renderer (rejected — it would replace
  the table the issue and the docs show, for no gain in a rule that already has the columns); an `overrides`/pin-based
  approach (rejected — it changes what installs, not what is reported, and a pin is not a suppression).
- **Parse the table's columns rather than matching raw lines.** The rule needs to know whether `Wanted` and `Latest`
  differ in their leading integer, so it reads the header to locate them and splits each row on whitespace. Rows whose
  columns cannot be read (a `Linked`/`Missing` marker in `Latest`, a short line) are kept unchanged — a filter must not
  silently hide a row it does not understand.
- **Put the rule in `NpmOutdatedRules`, beside the pattern the repo uses for `Versions`/`DependencyUpdateRules`.** The
  cases worth pinning are the same-major/major-only split, an unlisted package, and an unparseable row — each a unit
  test rather than a whole-report run.
- **Keep the raw table in a separate file (`build/npm-outdated-raw.txt`) and render the filtered text into
  `build/npm-outdated.txt`** — the file the task logs and the workflow reads — so the workflow needs no change and the
  raw data stays available for a diagnosis.
- **Key the list on the package name, one per line, in `config/web-ui-updates/major-disabled.txt`.** It mirrors the JVM
  file's name/format conventions (line list, `#` comments, a pointer to the rationale), which the suppression-list
  rename made the repo-wide shape.
- **Widen every surface that enumerates the suppression lists.** A suppression _surface_ is named in three places
  besides the config itself — the architecture-auditor's definition, its `/audit-architecture` command, and the
  `agent-skills` requirement that owns the sweep (via a `MODIFIED` delta) — plus `AGENTS.md`'s summary of that sweep; a
  new list that left them would leave an enumeration reading complete while missing a member.

## Risks / Trade-offs

- **A parser that mis-reads the table could drop or keep the wrong rows.** → The rule is unit-tested against the real
  header and real rows (including a `Linked`/`Latest` marker), and any row it cannot parse is kept as-is.
- **`npm outdated`'s output is not a stable interface.** → The filter is defensive (header-located columns, unparsed
  rows kept), and the change pins the rule's behaviour rather than npm's output.
- **A second suppression list to keep in step with the JVM one.** → They suppress different ecosystems' coordinates, so
  they are independent by design; the `dependency-management` requirement states each.

## Migration Plan

- Apply: the list, the rule and its tests, the script/task wiring, and the docs.
- Rollback: revert the commit; the report returns to `npm outdated`'s raw table. No deployed state is affected.
