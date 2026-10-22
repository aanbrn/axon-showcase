# Tasks

## 1. The bump

- [x] 1.1 Re-resolve the in-range set against npm's own report (`npm outdated` in `showcase-web-ui/`) and the declared
      names in `package.json`, rather than reading the weekly tracker issue. **Done**: eight declared dependencies have
      in-range updates, `vite` and `typescript-eslint` among them; two majors are available and excluded (1.4).
- [x] 1.2 Run `npm update`, then describe the lockfile diff by what moved. **Done**: the graph reconciliation moved 51
      packages and removed one stale nested entry; the direct moves are the eight, with `vite` 8.3.4 → 8.3.5 the only
      bundle-path move.
- [x] 1.3 Confirm no declared dependency range moved. **Done**: the lockfile's root block is identical to `HEAD`'s, so
      the ranges are untouched and the lockfile carries only resolved versions.
- [x] 1.4 Record the two excluded majors and why. **Done**: `typescript` 7.0.2 is suppressed by
      `config/web-ui-updates/major-disabled.txt` (the `typescript-eslint` peer range caps TypeScript below the next
      major); `@hookform/resolvers` 6.0.0 is a major migration of its own.
- [x] 1.5 Retire the `overrides` entry: its recorded trigger — neither `eslint-plugin-boundaries` nor
      `@boundaries/elements` still pinning `handlebars: "4.7.9"` — is met by this bump. **Done**: the block is deleted
      from `package.json`; `npm ls handlebars` resolves `4.7.10` from the plugins' own pins, and the lockfile carries no
      `4.7.9` entry.
- [x] 1.6 Remove the parked `handlebars` re-check from `docs/ideas.md`: the condition it named is met, so it is
      implemented rather than open. **Done**: the entry is gone and the implementation is `package.json`'s diff.

## 2. Verification

- [x] 2.1 Re-run the report that produced the finding. **Done**: `npm outdated` now lists only the two excluded majors.
- [x] 2.2 Run the module's gates. **Done**: `./gradlew :showcase-web-ui:check` BUILD SUCCESSFUL (lint, `format:check`,
      the type-check, Vitest).
- [x] 2.3 Build the production bundle, since a bundle-path package moved. **Done**: `./gradlew :showcase-web-ui:build`
      BUILD SUCCESSFUL, and `build/dist/assets/` holds the emitted `index-*.js` and `index-*.css`.
- [x] 2.4 Run the npm audit check, whose verdict the override removal and the graph move could change. **Done**:
      `./gradlew :showcase-web-ui:npmAudit` exits 0; the remaining high-severity chain is the `braces` suppression
      `npm-audit-ignores.json` documents, reached only as a dev transitive.
- [x] 2.5 Run `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`; run the manual 120-character
      check over the change dir. **Done**: all green; the change validates (`--strict`).

## 3. Review, capture and the manual pass

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      and the class-level dismissal are in `design.md`.
- [x] 3.2 Run a `review-quick` pass over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` and running the ledger checkpoint after each append; repeat until a round reports nothing
      new. **Done**: each row was appended as its report arrived, with the checkpoint run after each append; the ledger
      carries the rounds and their outcomes, and the loop is clean.
- [x] 3.3 Derive the deep-review routing outcome, decide at or above the floor, and record the outcome and its signals
      here. **Done**: `NOT_NEEDED` — no surface the routing tool flags, so no deep pass, no hand-over and no expensive
      invocation.
- [x] 3.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason with the reason.
      **Done**: no new rule (the three misses were instruction-following against recorded ones) and one replacement in
      the `gradle-build` pack: its `overrides` bullet now states the `handlebars` instance in the past tense, since the
      present-tense text claimed a pin and an override that no longer exist (`+3/-3` lines in the pack — the tense
      correction and the marker, net 0 — `AGENTS.md` unchanged). The incident clause the first draft added was dropped
      in review as a restatement of the rule's own obligation.
- [x] 3.5 Ask the owner for the manual review pass; commit, push, archive and merge only after approval. **Done**: the
      owner approved on 2026-10-22 after the manual review request; the change ships on PR #660.
