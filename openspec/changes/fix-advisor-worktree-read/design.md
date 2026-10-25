# Design

## Context

The unit started from a narrow incident: during `standardize-pr-conventions`, two consultations (the owner's account;
the sessions are not in the repository) reported that the advisor could not read the unit's planning artifacts, and the
caller inlined every excerpt by hand. The premise taken from that was "the advisor cannot read the assigned worktree". A
controlled probe falsifies it, and what remains is a different, smaller defect.

### The probe

One fresh advisor session (`ses_e8e93a425ffeNz1FiZmI4Cp6Jk`), invoked from this unit's worktree, was told to read four
paths with its `read` tool and report each outcome verbatim, nothing else:

| Path                                                                          | Outcome                                      |
| ----------------------------------------------------------------------------- | -------------------------------------------- |
| `AGENTS.md` (relative)                                                        | `OK`, first line `# AGENTS.md`               |
| the absolute path of the same file inside this worktree                       | `OK`, first line `# AGENTS.md`               |
| `/Users/afanasyev/Development/axon-showcase/README.md` (the primary checkout) | `FAIL Permission denied: external_directory` |
| a path in another sibling worktree                                            | `FAIL Permission denied: external_directory` |

That probe alone reads as "the invoking session's directory". Two later observations correct it: the root belongs to the
**advisor session's creation**, not to the caller's current directory.

- **A continued session kept its original workspace.** `ses_e8f042f11ffeVbkLsn9NBhfKxK`, created while the caller worked
  in the primary checkout and continued from this worktree, read a relative `AGENTS.md` that turned out to be the
  primary's copy, and was refused this worktree's absolute path.
- **A fresh session took the caller's directory.** `ses_e8e5c8f7effeccHv8Hg6Ob7HyK`, invoked from this worktree, read
  the worktree's `AGENTS.md` through both the relative and the absolute path — each carrying this unit's uncommitted
  `Refreshing a branch:` heading, which the primary's copy does not — and was refused the primary's.

So the readable root is the **workspace the consultation was created in**: a fresh consultation inherits the caller's
directory and usually shares the tree the work lives in, while a continued session keeps where it started, which may be
a different tree. That is what these probes establish — the two workspaces and the outside paths C1 lists, not the
boundary's whole shape (the Risks name the untested cases, and `AGENTS.md` records a harness default grant for
`<tmpdir>/opencode` that this unit did not exercise). The one missing path the probes touched was a removed sibling
worktree, and it answered with the same permission error rather than a not-found one: an outside path is refused as
`external_directory` whether or not it exists, which is what the tool reports — the order of the checks behind that
report is not observed.

### What actually leaked into the artifacts

`opencode debug agents` resolves the advisor's effective ruleset. The definition's own block contributes `*/* deny`,
then `read`/`glob`/`grep`/`skill` allows and the `read` `.env` denies; the resolved entry also carries entries the
engine and the project config add (`shell`, `external_directory` grants for `$HOME/.config/gh/*` and the OpenSpec
package, a `browser` deny, `doom_loop` deny). **No rule in the definition allows `external_directory`**, and none of the
resolved grants covers a sibling worktree or the primary. Since the engine resolves to the last match, an outside read
falls through the definition's catch-all deny, and no resolved grant covers those two paths.

That is the mechanism. The _defect_ is one step earlier and is one of omission rather than error: both artifacts that
describe the handoff give a reason that is true for history and diffs — "because you have no shell under OpenCode V2" —
and neither says which tree the read tools reach. The advisor does have those tools and does read its own workspace, so
a caller following the text inlines everything, the advisor loses the ability the definition demands of it ("you read
the evidence yourself rather than accepting the caller's summary as proof"), and the handoff costs a large forwarded
transcript it need not.

### Premise check

- **Is the boundary deliberate?** Yes, as a mechanism: the advisor is read-only by design, and its definition states the
  readable root and that a configured grant may allow a path outside it (C2 and C3 record the definition's block and the
  config's two grants). No record of widening it for a sibling worktree exists where a decision about it would live — an
  `external_directory` search over `docs/`, `openspec/specs/`, and the archive returns only the config grants and the
  retired plugin, never an advisor widening (row C9).
- **Does the spec state it?** No, but not because it is silent on evidence: `showcase/quality/agent-skills`' read-only
  requirement states the split this change keeps — read tools plus the handoff for history and diff excerpts — and its
  handoff-context scenario gives the no-shell reason. What no requirement states is a **path boundary**, so adding which
  paths the read tools reach changes no requirement or scenario (C6, Decision 6).
- **Is the stated reason load-bearing elsewhere?** Only in the two sites this change edits; `AGENTS.md`'s
  technical-advisor bullet delegates the handoff contract to the definition.

## Goals / Non-Goals

**Goals:**

- State which paths the file tools reach, beside the excerpt duty the two artifacts already give, and record the
  boundary the probes established.
- Keep that boundary single-sourced in the definition, with the command pointing at it.

**Non-Goals:**

- Widening the advisor's permissions (see Decision 3).
- Moving the boundary into `AGENTS.md`, whose bullet delegates it to the definition (Decision 5).

## Decisions

| #   | Decision                                                                                             | Rationale                                                                                                                                                                                                                                                                                     |
| --- | ---------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | State the readable root in the definition, where the handoff contract lives                          | The caller's action depends on knowing which paths it must inline, and the definition already owns that contract                                                                                                                                                                              |
| 2   | Keep the claim's reason factual: the sandbox refuses a path outside the session's creation workspace | The old text was silent on which paths the tools reach, and that silence caused the over-inlining; the no-shell limit stays, now stated as one of two                                                                                                                                         |
| 3   | Do not widen the permissions                                                                         | The common path works: the workspace rule moves the session into the unit's worktree (C8), so the advisor shares the tree the artifacts live in; widening would let a read-only consultant of an external model read the primary and every sibling tree                                       |
| 4   | Point the command's field at the definition rather than repeating the boundary                       | One owner for the contract; the command's step 3 keeps the action and drops the second copy of the reason                                                                                                                                                                                     |
| 5   | No `AGENTS.md` change for the handoff                                                                | Its bullet already delegates the handoff and boundary to the definition; the capture's separate rule rides task 3.3                                                                                                                                                                           |
| 6   | No spec delta (`skip_specs: true`)                                                                   | The read-only requirement's "read tools and the handoff for history and diff excerpts" and its scenario's no-shell reason stay as they are; the boundary is a mechanism their outcomes do not distinguish, and the capture's step correction lands where that skill's requirement mandates it |

## Alternatives considered

| Alternative                                                                        | Why not                                                                                                                                                                                                |
| ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Grant the advisor `external_directory` for `../axon-showcase-*` in its own ruleset | The common case does not need it (the session moves into the unit's tree), and the grant would let the advisor read every sibling unit's uncommitted work — a wider surface than the decision requires |
| Grant it in `.opencode/opencode.json` instead                                      | A project-wide `external_directory` grant would widen every agent's reach, not only the advisor's, and the advisor's read-only sandbox is deliberate                                                   |
| Say nothing and keep inlining                                                      | The definition requires independent verification; inlining everything turns the consultant into a reviewer of the caller's prose                                                                       |
| Record the boundary as an `AGENTS.md` gotcha                                       | It governs one handoff, whose contract the definition owns; a root bullet would restate it                                                                                                             |

## Claims table

Figures are the probe's and the tools' output; each row names what reproduces it.

| #   | Claim                                                                                                                                                                                                                                   | Source / deriving command                                                                                                                                                                                                                                                                                                                                                           |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| C1  | The advisor reads inside the workspace its session was created in, and the sampled paths outside it were refused as `external_directory`; a fresh session inherits the caller's directory, a continued one keeps its original workspace | three probes: `ses_e8e93a425ffeNz1FiZmI4Cp6Jk` (fresh, from this worktree: inside OK, primary and sibling refused), `ses_e8f042f11ffeVbkLsn9NBhfKxK` (continued, created in the primary: read the primary's relative `AGENTS.md`, refused this worktree), `ses_e8e5c8f7effeccHv8Hg6Ob7HyK` (fresh, discriminator: both worktree paths OK with this unit's heading, primary refused) |
| C2  | The definition's own `permissions` block starts with `*/*` deny, then allows `read`/`glob`/`grep`/`skill`, denies the `.env` reads, and names no `external_directory`                                                                   | `.opencode/agent/technical-advisor.md` frontmatter, lines 8–32                                                                                                                                                                                                                                                                                                                      |
| C3  | The resolved entry carries the definition's block plus the engine's and the project config's entries, and the config grants `external_directory` only for `$HOME/.config/gh/*` and `*/@fission-ai/openspec/*`                           | `opencode debug agents` (`technical-advisor`.`permissions`), `opencode debug config`, and `.opencode/opencode.json`'s `permission.external_directory`                                                                                                                                                                                                                               |
| C4  | Both artifacts attribute the excerpt duty to the absence of a shell, which is true for history and diffs, and neither states which paths the read tools reach                                                                           | `grep -rn "no shell" .opencode/agent/technical-advisor.md .opencode/commands/consult.md` before the edit: three hits (the handoff field, the operational paragraph, the command's field); the definition's paragraph names only "the working tree"                                                                                                                                  |
| C5  | The advisor's definition requires it to read evidence itself                                                                                                                                                                            | `.opencode/agent/technical-advisor.md`: "you read the evidence yourself rather than accepting the caller's summary as proof"                                                                                                                                                                                                                                                        |
| C6  | The spec states the advisor's read-tools-plus-handoff split and its no-shell reason, and states no path boundary                                                                                                                        | `showcase/quality/agent-skills`, "The technical advisor is read-only" ("It SHALL rely on its read tools and the handoff for history and diff excerpts") and its "The handoff supplies history and diff context" scenario                                                                                                                                                            |
| C7  | `AGENTS.md` delegates the handoff and boundary to the definition                                                                                                                                                                        | its technical-advisor bullet: "the definition owns the handoff, boundary, and report contract"                                                                                                                                                                                                                                                                                      |
| C8  | The Workspace rule moves a new unit's session into its own worktree, so the advisor shares the tree the artifacts live in                                                                                                               | `AGENTS.md`'s Workspace bullet ("the session moves there (`session_move`) before the first write"), which this unit followed: the probe ran from this worktree and read it                                                                                                                                                                                                          |
| C9  | No record of widening the advisor's readable root exists where such a decision would live                                                                                                                                               | `git grep -n -i "external_directory" -- docs/ openspec/specs/ openspec/changes/archive/`: the hits are the project config's grants and the retired permission plugin, none about the advisor                                                                                                                                                                                        |

## Dismissals

`python3 scripts/claim-scan.py` seeded the candidates; each is resolved into a row above or dismissed here.

| Candidate kind                                                           | Disposition                                                                                                                                                                       |
| ------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `[path]` candidates (the definition, the command, `AGENTS.md`, the spec) | Dismissed as a kind: each is a file this change reads or edits, and the changed-path set is the diff's own                                                                        |
| `[absolute]` candidates                                                  | Dismissed as a kind: each sits on a claim whose row is C1–C9, which names its scope; the scanner's class is a floor rather than the full list, so no enumeration is repeated here |
| `[count]` candidates                                                     | Rows C1–C3 and the four-path probe table                                                                                                                                          |
| `.opencode/package.json` `[cross-ref]` candidate                         | Not this unit's: the OpenCode startup install rewrites that tracked pin when a session moves between trees; it is reverted, not committed                                         |

## Risks

- **Three probes, not the boundary's whole shape.** They tested a fresh and a continued session's workspaces and the
  paths outside them; a symlink out of the tree and the `glob`/`grep` variants were not tested, and the only missing
  path tried was a removed worktree, whose refusal was the same permission error. The statement this change writes
  covers reads inside and outside the session's creation workspace, which is what was verified.
- **A caller may still over-inline** if it does not read the definition, since the command points at it rather than
  repeating it. That is the single-sourcing trade the design takes.
- **The boundary can change with an OpenCode release.** The claim is anchored to `opencode debug agents` and the probe,
  so a later release that resolves it differently is detectable from those two sources rather than from the prose.
