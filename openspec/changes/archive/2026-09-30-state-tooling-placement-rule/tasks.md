# Tasks

## 1. State the rule

- [x] 1.1 Add the placement rule to `AGENTS.md`, extending the existing tooling/`scripts/` guidance (near the
      tooling-setup bullet and the Prerequisites bullet) rather than accreting a new top-level entry. The rule states
      where each kind of tooling lives, derived from the full inventory (`git ls-files -s | awk '$1=="100755"'` — 13
      files): **`scripts/`** for repository-wide tooling (checkers, dev scripts, guards); **a module's own directory**
      for its tooling (`showcase-web-ui/scripts/outdated-report.sh`, `showcase-web-ui/start.sh`); the **repository
      root** for `gradlew` (generated) and the human-facing setup entry points `db.sh` / `setup-hosts.sh`; and
      **`.opencode/`** for the OpenCode-runtime surface (agents, commands, skills), which holds no executable — a
      command may trigger a `scripts/` tool but never hosts its implementation. Name the generated/vendored exception
      (the generated OpenSpec command and skill files, and the vendored `axon4to5-*` skills, are not the repository's to
      place). Verify: read the bullet in context — it names every location the inventory holds, and does not contradict
      the surrounding tooling guidance.
- [x] 1.2 Route the current instances to the rule in that same bullet, so the split is legible rather than inferred: the
      three script+trigger pairs (`doctor.sh`/`/check-tooling`, `setup-idea.sh`/`/setup-idea`,
      `experience-analysis.sh`/`/retrospective`), the trigger-less tools (`install-git-hooks.sh`,
      `load-test-baseline.sh`), the guard's checker (`commit-hygiene.py`, run by the tracked `git-hooks/pre-commit`
      **and** by the Gradle `verify*` tasks in `build.gradle.kts`), the module-local pair, and the root entry points.
      Verify: **check the rule's boundary against the command that lists every executable** — run
      `git ls-files -s | awk '$1=="100755"'` and confirm each of the 13 paths falls under a location the bullet names,
      and that nothing the bullet names is absent from that output. The rule is the claim; that command is its control.
- [x] 1.3 Relocate the placement rationale out of `scripts/experience-analysis.sh`'s header, leaving the header to
      describe what the script does (and keeping any operational fact it carries, e.g. how to run it). The header's
      parenthetical also names "plugins" as an `.opencode/` entry point, which is stale (there is no `.opencode/plugin/`
      directory); drop that token rather than carrying it into the rule. The header's placement clause carries one
      sub-fact the rule must retain — that `.opencode/` has **no script location**, which is why this tool is not under
      it — so confirm the rule bullet states it after the move, rather than losing it with the clause. Verify: the
      header no longer explains placement and no longer names plugins, still describes the script's purpose and usage,
      `bash -n scripts/experience-analysis.sh` is clean, and the AGENTS.md rule states the no-script-location fact.

## 2. Documentation sweep

- [x] 2.0 **Settle list ownership before editing either surface**: the rule (1.1/1.2) is the authoritative statement of
      where tooling lives, so no doc keeps a parallel inventory. `AGENTS.md` states the boundary and names the
      instances; the `README.md` tree is an orientation sketch, so its fix is to stop presenting a _partial_ inventory
      (the `scripts/` comment listing two files) rather than to grow into a complete one. Record this split in the
      report. Verify: state, in the report, which surface owns the list and why, and that the README edit does not
      reproduce the rule's enumeration.
- [x] 2.1 Reconcile `README.md`'s project-structure tree with that decision: its `scripts/` comment (line 57, the
      `└── scripts/` line under the `Scripts` heading) enumerates two of eight files, which undercuts the rule. Make
      that comment describe the location rather than list a subset. The `db.sh` / `setup-hosts.sh` lines above it are
      **already consistent with the rule** — they are the root setup entry points — so do not move or re-home them; the
      edit target is the `scripts/` comment alone. The tree shows no `showcase-web-ui/` tooling; leave that as the
      sketch's scope and state in the report that the module-local pair is covered by the rule, not by the tree. Verify:
      the report names the README line the decision is based on, the `scripts/` comment no longer enumerates a subset,
      the root-entry lines are unchanged, and the edit adds no second copy of the rule's instance list. Note: this line
      is inside a fenced tree, but the fence is formatted by Prettier — the edit is formatter-gated, so run
      `spotlessApply` after it.
- [x] 2.2 Remove the implemented idea from `docs/ideas.md`: the entry "State once where agent-only tooling lives"
      (promoted to issue #456) is implemented by this change, so it leaves the scratchpad in this change's PR. Verify:
      `git diff docs/ideas.md` shows the entry removed, and grep finds no other `docs/ideas.md` reference calling it
      proposed.
- [x] 2.3 Sweep `openspec/config.yaml`'s `context:`/`rules` blocks and any other un-gated copy for a fact this change
      moves: confirm whether the config's context describes tooling placement (it names modules and conventions, not
      layout) and refresh it only if it does. Verify: read the `context:` block against this change's diff and state the
      outcome in the report.
- [x] 2.4 Check whether the AGENTS.md rule falsifies any existing statement — a "the only X" claim about tooling
      placement, a stale enumeration of the `scripts/` contents, or the doctor bullet added by
      `check-toolchain-prerequisites`. The known stale enumeration is the README tree's `scripts/` comment (task 2.1's
      target, inside a fenced block, not greppable as prose); this sweep is for any _other_ instance. Verify: grep
      `AGENTS.md` and `README.md` for `scripts/` enumerations and `only`/`sole` placement claims, and reconcile any the
      rule makes wrong — stating in the report which the tree comment accounts for and which, if any, remain.

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` after the final edit to a Spotless-owned file (`AGENTS.md`, `README.md`,
      `docs/ideas.md`, the change dir's markdown) and then `./gradlew spotlessCheck`. Note that ticking this box is
      itself an edit, so re-run `spotlessApply` after it. Verify: both tasks succeed.
- [x] 3.2 Run the Docker-free gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm green — the
      change alters no build input, so a failure is a pre-existing/remote cause. Verify: the task completes
      successfully.
- [x] 3.3 Run `openspec validate --changes` and confirm the change validates with the `skip_specs` marker (no delta spec
      is created). Verify: exit 0, `1 passed`.
- [x] 3.4 Read the final rule as content, against the repository, not against the plan: confirm every named file exists
      where the rule says it lives, the boundary holds for each of the 8 executable `scripts/` entries and for
      `.opencode/`'s contents, the generated/vendored exception is accurate, and the relocated header still reads
      coherently. Record the reading in the change's report.
- [x] 3.5 Run the `lesson-capture` subagent over the diff, apply the durable proposals the main agent judges worth
      keeping, and record the applied net `AGENTS.md` delta (expected: an extension to an existing bullet) on this task.
      Verify: the subagent's verdict is recorded and any applied edit is visible in `git diff AGENTS.md`. **Done —
      applied net `AGENTS.md` delta: one extension to the existing documented-numbers sub-entry (its scope widened from
      "a set the build or config declares" to "a set the repository declares", plus the subset-list/instance-patch
      clause); no new bullet, no retirement.** The subagent proposed 1 durable item and rejected 4 as covered (the
      count-vs-sublist case, the inline-code-span split, the off-by-one anchor, and the risk-without-a-task case, all
      with the covering bullet named).
