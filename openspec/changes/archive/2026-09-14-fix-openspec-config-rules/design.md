## Context

`openspec/config.yaml` declares a `rules:` block with one list per artifact (`proposal`, `specs`, `design`, `tasks`) and
an `operations:` block; the CLI feeds an artifact's rules into that artifact's creation instructions.

The verified state, checked against both the installed CLI (1.13.0) and the CI-pinned CLI (1.11.0) with a real probe
change — both behave identically:

- `proposal`'s first rule parses as `{"Keep the \"Why\" section concrete" => "what breaks today without the change"}`
  and `tasks`' third as `{"Group into sections" => "implementation tasks, then a verification section"}`: YAML's `: `
  rule turns an unquoted item into a mapping, so each list mixes strings and mappings and the CLI rejects the whole
  artifact's rules.
- `openspec instructions proposal --change <probe> --json` and `... tasks ...` **omit the `rules` key entirely**, while
  `... specs ...` returns its three rules — the control showing the cause is the shape, not the config being unread.
- `specs` and `design` are all strings and are applied.

CI installs the pinned `openspec` CLI and runs `openspec validate --all` in the same `build` job for both pull requests
and pushes to `main` — and that command emits **no** rules warning (verified on both CLI versions), so CI has never
surfaced this defect. The warning appears only where the config's rules are read — `openspec new change` and
`openspec instructions` — and has been read as noise on every such run since the config was written.

## Goals / Non-Goals

**Goals:**

- All four artifacts' declared rules are read by the CLI.
- A silently ignored rule set fails the CI gate rather than warning only.
- The pitfall and the misread-warning lesson are recorded.
- The `openspec/config.yaml` `context:` block — the un-gated second copy of the project facts — is audited for drift
  while the file is open. Its headline facts still match (Java 21, Spring Boot 3.5.16, Gradle 9.7.1, 19 modules — 18 JVM
  plus `showcase-web-ui`), but a Build-&-test item is stale: the Docker-image dependency belongs to
  `showcase-api-gateway`'s `e2eTest` suite rather than its `integrationTest` (as AGENTS.md's own gotcha states), and its
  "must run after the client integration tests" ordering has no premise left — the client modules register only
  `componentTest` — and its architecture header double-counts the web UI ("4 services + API gateway + web UI" for the
  five components it lists).

**Non-Goals:**

- Changing what the rules say — only their YAML shape.
- Regenerating the instruction files against the installed CLI — a separate `/opsx-tool-update` unit (this change must
  hold under the pinned CLI too, which is why both versions were checked).

## Decisions

### Quote the two items rather than reword them

The wording carries the intent; single-quoting preserves it and is the minimal change. Rejected alternative: rephrasing
to avoid `: ` — it loses the explanatory clause (`… concrete: what breaks today without the change`) to dodge a YAML
detail.

### The requirement belongs to `merge-governance`, not `code-quality`

`code-quality` specifies quality "through the build" — every requirement there is a Gradle `check` gate. This check runs
in the CI workflow beside `openspec validate --all`, and `merge-governance` owns "the continuous-integration gates that
run on pull requests and pushes", already enumerating the `build` check's OpenSpec validation. So the delta modifies
`merge-governance`'s two gate requirements rather than adding a requirement to `code-quality`.

_Alternative considered_: implementing the check as a Gradle task in `check` so it lands locally beside `workflowLint`.
Out of scope for this change — the `openspec` CLI is a CI-only tool in this repository (CI installs it; `check` does not
require it), and adding it as a hard `check` prerequisite is a larger change than the defect warrants. The accepted
trade-off is that the check is not exercised by a local `check` run.

### Detect through the CLI's own read path

The check creates a probe change and fails if the CLI reports that it is ignoring an artifact's rules. That is the same
path artifact creation uses, so the check cannot drift from the tool's behavior.

_Alternatives considered_: (a) linting the YAML shape ourselves — a second parser to maintain, encoding our assumption
about a contract the CLI owns; (b) asserting `openspec instructions <artifact> --change <probe> --json` carries a
populated `rules` key for each declared artifact — equivalent and arguably more robust, but more steps and a JSON parse
in the check.

_Trade-off accepted_: the check keys off the CLI's warning text, so a CLI that renames the warning would stop it
detecting. That is why `/opsx-tool-update` — run on each CLI upgrade — must carry the check **with a positive control**
(temporarily unquote a rule and confirm the check fails), so the coupling is re-verified rather than assumed; a renamed
warning would otherwise pass silently, which is the "prove the lookup resolves before trusting a clean run" gotcha.

### The probe cleans up on every path

The probe change is removed with a shell `trap` on `EXIT`, so a failing check does not leave it behind for a later step
in the same job.

## Risks / Trade-offs

- **[The check creates a probe change in the repository]** → it runs on a throwaway runner, a `trap` removes it on any
  exit, and the step follows `openspec validate --all` in the job, so a probe left behind would fail that validation on
  the next run rather than pass unnoticed.
- **[The check stops detecting if the CLI's warning text changes]** → `/opsx-tool-update`'s positive control re-verifies
  the coupling on each upgrade; the miss is a missed detection, not a broken build.
- **[Quoting is applied sloppily and changes a rule's meaning]** → tasks verify the CLI's parsed output before and after
  (`rules` present, wording unchanged), not just that the file changed.
