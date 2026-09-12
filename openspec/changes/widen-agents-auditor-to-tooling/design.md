## Context

`agents-auditor` audits `AGENTS.md` for consistency and conciseness. The project-owned agent tooling under `.opencode/`
shares the same failure mode — it is loaded on every invocation and accretes without a whole-set reconciliation — and
the two artifacts are coupled: a subagent appears in a `.opencode/agent/*.md` definition, an `AGENTS.md` convention
bullet, the README agent table, and the `showcase/quality/agent-skills` spec, so drift in one is often visible only
against the others. The `specs-auditor` (separate subagent) already covers the spec corpus, which has its own gate; this
change keeps the third artifact — the repo's own agent tooling — under a single auditor.

## Goals / Non-Goals

**Goals:**

- Audit `AGENTS.md` **and** the project-authored `.opencode/` files (subagents, commands, skills) as one scope.
- Draw the boundary by **provenance**, so it does not go stale: audit what the repo authors; skip what is generated (the
  files `openspec update` writes) or vendored (`axon4to5-*` skills).
- Keep the existing contract: two axes (consistency, conciseness), verify against the repository, report without
  editing.

**Non-Goals:**

- Auditing `AGENTS.md`'s companion config: `.opencode/opencode.json` is project-authored but is a short JSON config, not
  prose guidance — out of scope here (a config-consistency check, if wanted, is a different audit).
- Auditing the spec corpus (`specs-auditor` owns it) or the codebase behavior (the change workflow's review loop owns
  it).
- A new subagent. One auditor with both artifacts in view catches the cross-artifact drift (definition ↔ AGENTS.md ↔
  README ↔ spec) that two single-artifact auditors would each see only half of.

## Decisions

- **Widen `agents-auditor`, do not add a `tooling-auditor`.** The decisive factor is cross-artifact consistency: the
  same agents are described across `AGENTS.md`, the README, the spec, and their definitions, so an auditor holding all
  of those catches drift a per-artifact split cannot. It is also less machinery (a third auditor for one repo is an
  inventory of its own).
- **Provenance partition, not a file list or a name glob.** A hand-maintained skip list drifts, and a name prefix
  over-captures: `opsx-tool-update.md` is project-authored (it regenerates the OpenSpec files and syncs the CI pin), so
  the `opsx-*` prefix would wrongly exclude it. The rule is by source — skip what a generator owns (the files
  `openspec update` writes: `opsx-apply/archive/explore/propose/sync/update`, and the `openspec-*` skills) or what the
  repo vendors (`axon4to5-*` skills); audit the rest of `.opencode/agent/`, `.opencode/commands/`, and
  `.opencode/skills/`, including `opsx-tool-update.md`. A new project-authored file is therefore in scope by default,
  and a new generated/vendored file is recognized by its source rather than by name.
- **Retitle via delete-and-add, not MODIFY.** The requirement header "AGENTS.md is audited for consistency and
  conciseness" no longer describes the scope, and a delta cannot rename a MODIFIED main-spec header (it matches by
  header). The delta uses `## REMOVED Requirements` for the old header and `## ADDED Requirements` for the new one,
  carrying the full three-scenario set.

## Risks / Trade-offs

- **A larger read per run** (8 subagent definitions + 12 project commands + 2 skills + `AGENTS.md`) — acceptable for an
  on-demand pro-model pass.
- **Naming kept.** `agents-auditor` / `/audit-agents` now under-describe the scope slightly; keeping them is deliberate
  — one auditor across `AGENTS.md` and the tooling that describes the same agents, and a rename would churn the
  invocation surface for a naming gain. The "different artifact/property ⇒ own auditor" test (which gave `specs-auditor`
  its place) is satisfied together with the existing auditor, not by a new one.
- **Boundary judgment**: a file that is neither clearly generator-owned nor vendored (e.g. a future tool that scaffolds
  a command) needs a call. The definition states the principle (generator-owned or vendored ⇒ skip) so the caller can
  judge rather than match names.
- **Cross-references between a skipped and an audited file** (e.g. a project command invoking an `opsx-*` command) are
  still audited from the audited side — skipping a file means not auditing _its_ internal consistency, not ignoring it
  as a referenced target.
