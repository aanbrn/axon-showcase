# Tasks

## 1. Widen the CI probe

- [x] 1.1 In `.github/workflows/ci.yml`, change the configuration probe's grep to the generic list-shape pattern
      `must be an array of strings|could not parse` (matching any declared list surface, not only `rules`), update the
      `::error::` messages to name a declared list item rather than a rule, and rename the probe from
      `openspec-config-rules-probe` to `openspec-config-probe` (it no longer covers only rules); verify with
      `./gradlew workflowLint` that the workflow still lints.

## 2. Widen the tooling re-verification

- [x] 2.1 In `.opencode/commands/opsx-tool-update.md`, use the same pattern and probe name, check the `operations`
      guidance surface via `openspec instructions apply --change … --json` → `operationGuidance` as well as the four
      artifacts' `rules` via `openspec instructions <artifact> --change … --json` → `rules`, and add the guidance
      warning to its positive controls.

## 3. Documentation

- [x] 3.1 Update `AGENTS.md`'s config read-path gotcha so it reflects the widened guard: the probe now covers every
      declared list surface, retiring the "put a rule on `rules:` unless the guard is widened" clause, and naming the
      operation-guidance warning alongside the artifact-rules one; also update the CI summaries in `AGENTS.md` and
      `README.md`, which still say the probe checks the config's "rule sets", to the declared list surfaces.

## 4. Verification

- [x] 4.1 Run the probe's positive controls locally: a clean `openspec/config.yaml` passes; a malformed `rules` item and
      a malformed `operations.apply.guidance` item each fail it; restore the config and remove the probe change
      afterwards.
- [x] 4.2 Run `openspec validate widen-config-probe-to-guidance --strict` and `openspec validate --specs`, and confirm
      both pass.
- [x] 4.3 Run `./gradlew workflowLint spotlessCheck` and confirm it is green.
