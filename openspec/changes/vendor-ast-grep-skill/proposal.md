# Proposal

## Why

The agent's search tool is text matching, so a question about code structure is answered by guessing a regex, `rg` is
kept for text, identifiers, and paths, and a structural question — a `@Test` that also calls `block()`, a class carrying
`@ConfigurationProperties`, a `useQuery` call with its options — is answered by reading files. The `ast-grep` CLI is
already installed, and upstream ships a skill that teaches writing its rules; upstream also documents that installing a
skill makes it _discovered_, not _preferred_, so the preference has to be stated in `AGENTS.md` or the text tool wins by
default.

## What Changes

- **Vendor the skill verbatim** — `ast-grep/skills/ast-grep/` from `ast-grep/agent-skill` (plugin `ast-grep` 1.0.0,
  commit `f2175af`, MIT) into `.opencode/skills/ast-grep/`, carrying its `SKILL.md` entry point and the
  `references/rule_reference.md` that entry point names. Copied byte-for-byte, with no project-specific edit.
- **Keep the vendored tree out of the project's own gates** — the Spotless markdown target, the marker task's input file
  tree, `VENDORED_SKILL_PREFIXES` in `scripts/commit-hygiene.py` (the `captured:` marker and executable-bit checks), and
  `EXCLUDED_SKILL_PREFIXES` in `UpstreamReferenceScope.kt` (the upstream-reference scan), so a refresh diff is only ever
  an upstream change.
- **State the tool choice once** — a rule under `AGENTS.md`'s `### Evidence and search`: a search whose answer depends
  on code structure loads the skill and searches structurally, while `rg` stays the tool for text, identifiers, and
  paths; a pattern matches whole AST nodes, a zero result needs the pattern proven first, and ast-grep says nothing
  about types or behavior.
- **Make the CLI part of the toolchain** — an optional `ast-grep` probe in `scripts/doctor.sh`, its README Prerequisites
  row and install columns, and the `scripts/test-doctor.sh` mapping the agreement check needs; plus a README sentence in
  the agentic-process section naming the two vendored skill sets.
- **Record the second vendored set** — the `AGENTS.md` vendored-skills entry, plus the deltas its membership owes on
  `showcase/quality/agent-skills`, `showcase/quality/code-quality`, and `showcase/quality/commit-hygiene`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- **`showcase/quality/agent-skills`** — "Vendored agent skills are available to agents" now covers both sets (and states
  that the structural-search skill works while its CLI is missing), "Vendored skills carry version provenance" becomes
  per-set and adds the commit a tagless upstream needs, and the auditor's exclusion covers the vendored skill trees
  rather than one set.
- **`showcase/quality/code-quality`** — the formatter's requirement excludes the vendored skill trees rather than naming
  one of them, so a set gaining a member no longer makes the requirement stale.
- **`showcase/quality/commit-hygiene`** — the executable-bit requirement's exclusions take the same form.
- **`showcase/quality/toolchain-check` needs no delta**: its optional class already names "the agent CLIs", and its
  requirements state the probe mechanism rather than enumerating the probe set.

## Impact

- **No production code.** Nothing on a Gradle module's classpath and nothing in a Docker image. No new gate: no build
  task runs a structural search, and the only contact `check` has with the CLI is the optional `--version` probe the
  doctor runs for every declared tool that names a binary (`compose` names none) — through `testDoctorCharter`, so a
  machine without ast-grep still passes; nothing depends on the version it prints. That check asserts the declaration's
  shape, that every declared probe is reported, and the declaration's agreement with the README and the required set —
  which is where this probe's row, install hints, and README mapping are asserted. The probe reports; it does not
  require.
- **No replacement of anything.** `rg` stays for text, the existing linters are untouched, and the skill is guidance the
  agent loads on demand — it adds a tool choice, not a gate.
- **Verified against this repository's sources**, with a positive control for every zero result and a before/after
  discovery probe (see `design.md`).
