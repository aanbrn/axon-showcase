---
description:
  Regenerate the OpenSpec commands and skills and sync the openspec CLI pin across every workflow that declares it,
  after a new openspec CLI release
---

Regenerate the OpenSpec instruction files when a new `openspec` CLI version has been installed, so the generated
`.opencode/commands/opsx-*.md` commands and `.opencode/skills/openspec-*.md` skills stay in sync with the CLI's built-in
templates.

**Trigger** — the owner invokes `/opsx-tool-update` after installing a newer `openspec` CLI.

**Procedure**

1. Run `openspec --version` and record the installed version, then run `openspec update` from the repository root. Do
   not pass `--force` unless the owner asks — the plain command detects up-to-date instruction files and skips.
2. Probe the config's declared list surfaces, since a newer CLI may parse the YAML differently: create a probe change
   whose name is unique to this invocation, and confirm the CLI reads it without a "must be an array of strings"
   warning, and that `openspec instructions` returns populated lists for `rules` (`--change <probe> --json`) and
   `operationGuidance` (`openspec instructions apply --change <probe> --json`).
3. Exercise the checks with one positive control at a time, since a renamed warning would otherwise pass silently:
   temporarily unquote a `rules` item containing `: ` and temporarily give an `operations.*.guidance` item an unquoted
   `: ` — each must make the probe's grep report "must be an array of strings" — then drop a closing quote from a quoted
   item, which must make `openspec validate --all` exit non-zero. For each control: confirm the source text actually
   changed (an item without `: `, or one already unquoted, no-ops), read the intended diagnostic, and only then restore
   the file.
4. Update the `openspec` pin to the installed version with a targeted replacement of `@fission-ai/openspec@<old>` in
   **every workflow the repository's `toolingUpdates` check declares for it**. Derive that inventory from the check
   itself — each `ToolingUpdateCheck` in `build.gradle.kts`'s `toolingUpdates` registration names its `workflowFile`,
   and the task's report names only the checks with an available update — and never from a hand-written list, which a
   fourth workflow gaining the pin would leave stale.
5. Inspect the regeneration's diff rather than its file list: `git diff -- .opencode/` alongside `git status` for the
   workflow files, classifying each changed file as a `generatedBy` stamp or a template content change, and reading what
   a changed template now instructs.
6. IF the command or skill inventory changed (not only the bodies), THEN check whether `AGENTS.md` and `README.md` still
   describe the available commands accurately, and whether the root `build.gradle.kts` markdown target's exclusions
   still match what `openspec update` wrote — a newly generated file would otherwise be reformatted by `spotlessApply`
   and then overwritten by the next `openspec update`.
7. Check the standing parked record this bump activates, before reporting done: `docs/ideas.md`'s 2026-09-30 entry names
   the CLI bump as its own check point, so read its watch condition, apply the removal each met clause names, and record
   the outcome on the change's task, or on the pull-request checklist when the unit has no change dir. The step retires
   with that entry, and the change that retires the entry sweeps this step.

**Decision points**

- IF nothing changed, THEN report that the instruction files are already current for the installed CLI and stop.
- IF files changed, THEN list which commands and skills were added, removed, or modified — naming each template content
  change, a stamp-only file saying so — plus the pin changes.
- The probe change is this invocation's: remove exactly that change when done, and never a change the invocation did not
  create.

**Failure handling**

- `openspec/config.yaml` is restored to its exact prior content on success, on failure, and on interruption — a control
  that edits it is never left behind. Restore from a copy taken before the first control.
- Never overwrite a pre-existing local edit to `openspec/config.yaml`: take the copy before any control runs, and
  restore from it rather than from the repository's `HEAD`.
- IF a control's edit did not change the source text, THEN the control proved nothing — fix the control rather than
  reading the check's silence as a pass.

**Verification** — `./gradlew toolingUpdates` no longer reports the `openspec` pin, and `openspec validate --all`
passes.

**Completion criteria** — the regenerated files and the pin changes are reported; or, when nothing changed, that is
reported with no edit made.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, with an ownership it cannot establish
  treated as occupied, per `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** rule.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval, and the
  regenerated files and pin changes ship as one standalone unit — not mixed with unrelated changes.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" and "Workflow: The
  review gate" own the branch, uncommitted-work, approval, and review steps, including the reviewer-session preflight;
  this command points at them rather than restating them.
