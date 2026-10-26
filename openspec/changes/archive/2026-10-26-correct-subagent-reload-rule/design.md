# Design

## Context

The proposal rewrites one `AGENTS.md` bullet. This design records the evidence behind the rewrite, because the rewritten
rule asserts a tool behaviour and the repository cannot check it with a gate: the rule itself becomes the record of how
to re-check it.

The bullet was introduced by `9854269d` (2026-09-12, "Capture the coverage-gate and subagent-reload gotchas (#169)") and
its incident is `openspec/changes/archive/2026-09-12-add-agents-auditor-agent/tasks.md`, whose smoke-run note reads "run
after a session restart — a subagent created mid-session is not registered until OpenCode reloads its agent list". The
repository migrated its OpenCode configuration to V2-native shapes in `7a8ccd64` (2026-09-24, #391); the bullet was
never re-verified after that, and a later edit (`172a94c6`, 2026-10-02, #479) added the neighbouring frontmatter
bullet's contrast against it without re-testing the reload claim. The reload bullet was itself extended by `69f4705a`
(2026-09-23, #368) with the edited-definition and no-proof-of-pickup clauses, so it has not been untouched since
`9854269d` either.

## Goals / Non-Goals

**Goals**

- State the behaviour the installed V2 line actually has, at the version probed.
- Replace "restart, then smoke-run" with a confirmation that can fail.
- Hold `AGENTS.md`'s growth to a measured, recorded delta (claims row 7) — the split's own lines included, rather than
  assumed away.

**Non-Goals**

- Not a general study of OpenCode's watcher: only the three definition kinds the rule governs (agents, commands, skills)
  and only the surfaces the rule's reader acts on.
- Not a re-verification of the 2026-09-12 incident: it is carried from the archived record as a dated fact (claims rows
  4–5), not re-run, and the rule states current behaviour only.
- No new exception for a mode or client the probe did not exercise.

## Evidence: the probe

Run in this session (`ses_e880ee8c5ffeSMHo0aBStdezbm`), in this worktree, against `opencode` **v2.1.4**
(`/opt/homebrew/bin/opencode` → `Cellar/opencode-v2/2.1.4/bin/opencode`, macOS arm64). No restart, no new parent
session, and no `sessionID` passed to any child.

**Target definition.** `.opencode/agent/review-quick.md` — project-authored, `model: opencode-go/deepseek-v4.1-flash`,
`temperature: 0`.

**Task text, identical in calls 1–3:** "Probe call. Do not use any tools and do not read any file. Reply with exactly
the probe token defined in your instructions, or the single word NONE if your instructions define no probe token. Reply
with one line and nothing else."

**Definition edit.** One paragraph appended to the file:

```text
**Probe token.** IF the invocation asks for the probe token, THEN your entire reply is exactly the token on the next
line and nothing else. Otherwise ignore this paragraph.

probe-token: <token>
```

| Call | Definition state                                         | Reply                               |
| ---- | -------------------------------------------------------- | ----------------------------------- |
| 1    | unedited                                                 | `NONE`                              |
| 2    | paragraph added, token `A7K2QX`                          | `A7K2QX`                            |
| 3    | token replaced with `ZR8WMD`                             | `ZR8WMD`                            |
| 4    | new file `.opencode/agent/probe-reload-check.md` created | `NEW9XQ`                            |
| 5    | the same file deleted                                    | `Unknown agent: probe-reload-check` |

Call 4's file carried its own token instruction, so its reply shows the new definition's body was live, not merely that
the name resolved. Call 5's error is the V2 wording of the not-registered failure.

**False-success channels closed.** The token appeared in no prompt and no hand-over; the child sessions were fresh, so
none inherited it; the task forbade tools and file reads, so the reply could not come from the definition on disk; and
call 1's `NONE` fixes the pre-edit baseline, so call 2's token cannot be a pre-existing instruction.

**Confound considered and excluded.** The session had been moved to this worktree immediately before call 1, so a
registry built at the move would have been built from the pre-edit file. Call 1 answered `NONE` (the pre-edit body), and
the change to `A7K2QX` came after it, so the move does not explain calls 2–3.

**Cleanup verified.** `review-quick.md` restored from the backup taken before call 2; `git hash-object` returns
`f6e69271f254f9d636353609b654a4c4d2d7f431`, the pre-edit blob; `git status --porcelain --untracked-files=all` is empty.
The probe agent file was deleted before call 5.

**What the probe does not establish** (stated, not resolved): whether the watcher is active in `opencode run` and in the
desktop/web server; whether a running session's already-advertised subagent list (the Task tool's names, the agent
switcher, the slash-command list) refreshes, as opposed to the per-invocation resolution the calls exercised; and
whether the global `~/.config/opencode/**` is watched. The skill path's directory subscription is bundle-evidenced
(mechanism below) but was not exercised by the probe. The rule therefore names no per-mode exception and no
client-specific claim.

## Evidence: the mechanism

Literal slices of the v2.1.4 bundle (bun-compiled; the JS is greppable), from the binary at the path above:

- `opencode.config.agent` loads `{agent,agents}/**/*.md` (and `{mode,modes}/*.md`), subscribes to the config source's
  change stream filtered to paths under `agent|agents|mode|modes`, debounces ~100 ms, re-reads the documents and calls
  `e.agent.reload()`; it also reloads on `config.updated`.
- `opencode.config.command` has the same shape for `{command,commands}/**/*.md` → `e.command.reload()`.
- `ConfigSkillPlugin` subscribes to each skill directory (`ConfigSkillPlugin.watchDirectory` →
  `d.subscribe({path, type})`), refreshes on a debounced change, logs `skills rescanned`, and calls `e.skill.reload()`;
  it also reloads on `config.updated`.
- The watcher primitive is `fs.watch`, and it returns early when
  `OPENCODE_FILEWATCHER_DISABLE ?? OPENCODE_DISABLE_FILEWATCHER` is `1` or `true` (case-insensitive); a setup or runtime
  failure closes it and logs `live-reload disabled`.

The probe is the behavioural half of this pair; the mechanism explains why it behaves that way and names the conditions
under which it will not.

## Decisions

### 1. Rewrite the bullet in place, keep its other obligations

Alternatives: a new bullet (rejected — the subject is the same, and a second bullet is the duplicate the single-sourcing
invariant forbids); deleting the rule (rejected — the confirmation practice, the archive precondition, and the
positive-control seeding are load-bearing and unrelated to the reload claim); leaving it and parking the correction
(rejected — the rule actively misdirects verification, so a stale rule is worse than the cost of the edit).

The rewrite keeps every retained obligation the old bullet carried, re-shaped into one bullet each; the rule is their
list, so this record does not repeat it.

### 2. The restart becomes a fallback; a marker probe becomes the confirmation

Alternatives: keep the restart as the cheap default (rejected — it discards a working reload and costs the session's
context every time); trust an echo of the new text (rejected — the rule already says why: the subagent can read its own
definition from disk); rely on the watcher's log line (rejected — the log is not an assertion a reader can see, and the
probe is two calls).

The probe shape is fixed by the two false-success channels: the token must not appear in the task text, and the task
must forbid tools and file reads. The pre-edit baseline call is part of the procedure for the same reason — without it,
a reply carrying the token proves the marker exists somewhere, not that the edit was picked up.

### 3. Name the version and the disable conditions, add no per-mode exception

A behaviour claim about a tool is pinned to what was probed: v2.1.4. The two environment variables are stated because
they are the conditions under which the rule's default does not hold; a mode the probe did not exercise is recorded as
unestablished above rather than written into the rule as an exception.

### 4. Drop the unverified V1 mechanism and message text

The bullet quoted `Unknown agent type: <name>` and asserted that V1 registered agents only at startup. The archive holds
no such string, and the archived incident evidences only that a restart was needed before its smoke-run — not the
loading mechanism, and not the binary major. The V2 probe produced `Unknown agent: <name>`. The rewrite therefore states
the current behaviour only; the 2026-09-12 incident and the migration date stay in the claims table (rows 4 and 5),
where a dated record belongs, rather than in the rule.

### 5. Split the bullet in place; the extraction stays a separate idea

The rewritten second bullet carried several obligations under one lead — the set the bullet itself stated — which the
Structured Writing Standard's one-statement-per-unit rule forbids. The first version of this decision parked the split,
arguing that both remedies were out of scope; that conflated two different fixes:

- **Splitting in place** keeps the content and its owner in `AGENTS.md` and changes only its shape, so it owes no spec
  delta, no destination contract, and no `captured:` retarget.
- **Extraction** moves the obligations to the review gate that runs the smoke-run — `.opencode/skills/review-gate/` or a
  `showcase/quality/agent-skills` requirement — and owes all three.

The standard governs the block this unit edited, and the growth bound is a discipline rather than a cap, so the split is
done here: the marker probe became a short numbered sequence, each remaining obligation became its own bullet, and no
rationale, constraint, or `captured:` marker moved away from the rule it supports. The extraction remains parked in
`docs/ideas.md` (2026-10-26) as its own change.

**Granularity is the obligation, not each internal action** — the owner's direction when directing this split, and the
reviewer's stated alternative. A bullet that states one obligation and its own verification (copy the probed definition,
then diff the copy; verify a surfaced defect, then fix the ones the owner approves) keeps that pair inline; a pair that
states two independent obligations (verifying a seed's premise, and never promoting a proposal on invented details)
becomes two bullets. The marker probe is numbered because its order is part of the instruction.

## Risks / Trade-offs

- **A future OpenCode version changes the behaviour, and no gate reads the rule.** Mitigation: the rule names the probed
  version and carries the probe procedure, so the next reader re-derives rather than trusting prose; the periodic
  `/audit-agents` pass is the sweep that re-reads guidance against the tool.
- **The probe could pass on a definition the session had already loaded.** Mitigation: the baseline call and the
  token-in-no-prompt rule, both stated in the procedure.
- **`AGENTS.md` growth.** The rewrite is measured against the pre-change file in the claims table; the rule's retained
  obligations are kept in their existing wording rather than re-expanded.

## Claims table

Derived after the final edit; every row is re-run from the artifact it names.

| #   | Claim the change states                                                                                                             | Source / command                                                                                                                                                                                                                                                                                               |
| --- | ----------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | V2.1.4 applies an edited subagent body mid-session                                                                                  | Probe calls 1–3, above (procedure recorded here)                                                                                                                                                                                                                                                               |
| 2   | V2.1.4 registers a created agent file and drops a deleted one mid-session                                                           | Probe calls 4–5, above                                                                                                                                                                                                                                                                                         |
| 3   | The watcher is disabled by `OPENCODE_FILEWATCHER_DISABLE`/`OPENCODE_DISABLE_FILEWATCHER` and logs `live-reload disabled` on failure | Bundle slices under "Evidence: the mechanism", read from the v2.1.4 binary                                                                                                                                                                                                                                     |
| 4   | The archived 2026-09-12 incident needed a restart before its smoke-run                                                              | `openspec/changes/archive/2026-09-12-add-agents-auditor-agent/tasks.md` (smoke-run note: "run after a session restart")                                                                                                                                                                                        |
| 5   | The bullet predates the V2 migration and was never re-verified after it                                                             | `git log -S "is not registered until OpenCode reloads" -- AGENTS.md` → `9854269d` (2026-09-12); `git log -1 --format=%ad 7a8ccd64` → 2026-09-24; `git show 69f4705a -- AGENTS.md` shows the edited-definition and no-proof-of-pickup clauses                                                                   |
| 6   | No capability spec states when a definition edit takes effect                                                                       | `grep -rn "takes effect\|session start\|restart\|reload" openspec/specs/` — hits are the compaction reload (`agent-skills`), the IDE settings reload (`ide-config`), and the deployment/consumer restarts; the pattern matches (positive control) and none concerns definition loading                         |
| 7   | `AGENTS.md`'s measured net growth after the change                                                                                  | `git diff --numstat 905e33a5 -- AGENTS.md` after `spotlessApply` → `48 35` (+13 lines, +624 bytes) against `905e33a5`: the rule rewrite and the advisor-directed corrections, plus the capture's one surviving clause (the tool-behavior version binding; its companion clause was removed as already covered) |
| 8   | The probe left the tree clean                                                                                                       | `git hash-object .opencode/agent/review-quick.md` → `f6e69271…`; `git status --porcelain -- .opencode/agent/` → empty                                                                                                                                                                                          |
| 9   | Upstream treats hot reload as delivered on V2                                                                                       | `gh issue view 8751`/`34492 -R anomalyco/opencode` → both `CLOSED` 2026-10-01, the second with a "watches … agent files, and reloads" comment                                                                                                                                                                  |
| 10  | V2 reloads command and skill documents, not only agent definitions                                                                  | bundle slices under "Evidence: the mechanism" (`opencode.config.command` → `e.command.reload()`; `ConfigSkillPlugin.watchDirectory` → `d.subscribe`); bundle-evidenced, not probed                                                                                                                             |
| 11  | The archive holds no `Unknown agent type` string                                                                                    | `grep -rq "Unknown agent type" openspec/changes/archive/` → exit 1, with `grep -rq "Unknown agent"` → exit 0 as the positive control                                                                                                                                                                           |

Base revision for rows 5 and 7: `905e33a5`, the `origin/main` this branch was refreshed onto mid-unit; the figures were
re-derived against it after the final edit: `48 35`, +13 lines / +624 bytes.
