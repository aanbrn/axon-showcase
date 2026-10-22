# Design

## Context

`npm update` is the operation the previous web UI bumps used, and it reconciles the whole dependency graph rather than
the packages named: an in-range direct bump moved the eslint cache stack across majors once before. So the unit
describes the lockfile diff by what moved, not as "patches and minors", and its verification is the module's own gates
rather than a list of the packages it meant to change.

The `overrides` entry is a second, separate edit. It exists because two dev-dependency-packaged plugins pinned
`handlebars: "4.7.9"` exactly while npm's only offered fix was a breaking downgrade of the plugin; the parked idea held
it with a recorded trigger — drop it once neither plugin requests `4.7.9` — and this bump met that trigger. The removal
is verified from the resolved graph, not from the plugins' changelogs.

## Goals / Non-Goals

**Goals:**

- Take every in-range update the report names, and refresh the transitives `npm update` reconciles with them.
- Retire the `overrides` entry whose recorded trigger has fired, and remove the parked idea it was held under.
- Verify the module's gates, its production bundle, and its npm audit check.

**Non-Goals:**

- `typescript` 7.0.2: `config/web-ui-updates/major-disabled.txt` suppresses it because `typescript-eslint`'s peer range
  caps TypeScript below the next major; a major bump cannot be taken alone.
- `@hookform/resolvers` 6.0.0: a major is a decision with its own migration, not an update-check row.

## Decisions

1. **`npm update`, then describe the diff.** The operation's graph-wide effect is why the description is derived from
   the lockfile (a per-entry before/after comparison) rather than from the intended list.
2. **Drop the override, do not re-park it.** Its own recorded condition — that neither plugin still pins `4.7.9` — is
   met, and `npm ls handlebars` resolves `4.7.10` with the override gone. Re-parking it would leave an unexpiring
   suppression whose trigger has fired.
3. **Build the bundle.** `vite` moved, so `:showcase-web-ui:build` runs in addition to `check`: the repository's rule is
   that a bump touching a bundle-path package must show the bundle emits.
4. **Ranges stay as they are.** No `package.json` dependency range changes; the only `package.json` edit is the
   `overrides` removal.

## Risks / Trade-offs

- **A graph-wide reconciliation can move more than the report names.** It did: 51 packages. The description says so, and
  the gates cover the outcome rather than the intent.
- **The e2e suite is opt-in** (`:showcase-web-ui:e2eTest`) and is not part of `check`. It is not run here: no
  application code changes, and the `AGENTS.md` rule that mandates a deliberate `e2eTest` covers a React/Vite **major**,
  not a patch. The scheduled `e2e` workflow runs daily and is the standing runtime net.
- **What the checks prove.** The frontend `check` runs lint, `format:check`, the type-check and Vitest against the
  sources; the bundle build proves the production bundle emits; neither exercises the emitted bundle at runtime. That is
  the scheduled e2e run's job, and the reason this unit states its coverage rather than claiming runtime coverage.

## Verification

- `npm outdated` after the update — only the two excluded majors remain.
- `npm ls handlebars` with the override removed — `4.7.10`, with no `4.7.9` entry anywhere in the lockfile.
- `./gradlew :showcase-web-ui:check` — lint, `format:check`, the TypeScript type-check and Vitest.
- `./gradlew :showcase-web-ui:build` — the production bundle emits (`build/dist/assets/*.js`, `*.css`).
- `./gradlew :showcase-web-ui:npmAudit` — its remaining high-severity chain is the suppression
  `showcase-web-ui/npm-audit-ignores.json` documents, and the task exits 0.
- `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`.

## Claims

| Claim                                                  | Source                                                                                                                                                                          |
| ------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Eight declared dependencies have in-range updates      | `npm outdated` before the update, plus the declared names in `showcase-web-ui/package.json`                                                                                     |
| Only the two majors remain afterwards                  | `npm outdated` after the update — `@hookform/resolvers` 6.0.0 and `typescript` 7.0.2                                                                                            |
| 51 packages moved, one entry removed, ranges unchanged | a per-entry comparison of `git show HEAD:showcase-web-ui/package-lock.json` against the working copy; `git diff` for `package.json`'s only change being the `overrides` removal |
| `vite` is among the moves                              | the same comparison: `vite` 8.3.4 → 8.3.5                                                                                                                                       |
| `handlebars` resolves `4.7.10` without the override    | `npm ls handlebars` after `npm install` with the block deleted, and no `4.7.9` version entry in the lockfile                                                                    |
| The npm audit check still passes                       | `./gradlew :showcase-web-ui:npmAudit` → exit 0, with the braces chain covered by `showcase-web-ui/npm-audit-ignores.json`                                                       |
| The module's gates pass                                | `./gradlew :showcase-web-ui:check`                                                                                                                                              |
| The bundle emits                                       | `./gradlew :showcase-web-ui:build`, then the `build/dist/assets/` listing                                                                                                       |

**Dismissals.** `scripts/claim-scan.py --base origin/main` fires on shape rather than on claimhood — paths and command
invocations, numbers, words like `only`/`every`/`never`, and repository-path/`#NNN` references. A candidate is a claim
only where a claims row states it; the rest are pointers a reader resolves against the repository.
