## MODIFIED Requirements

### Requirement: A pre-commit guard blocks a mechanically defective staged set

The repository SHALL provide a tracked git `pre-commit` hook that inspects the staged set before a commit is created and
refuses the commit when it detects a mechanically defective state: the project's formatter check fails; a staged path is
a generated artifact that must not be committed; a path is staged and then modified again so the index no longer matches
the working tree; a staged file carries a merge conflict marker; a `captured:` marker is misplaced in a staged guidance
file (`AGENTS.md`, or a project-authored `.opencode/agent/*.md` or `.opencode/skills/*/SKILL.md`); or a change dir under
`openspec/changes/archive/**` is staged while the spec sync `openspec archive` wrote under `openspec/specs/**` is left
unstaged or untracked. The hook SHALL name each offending path and the reason and SHALL exit non-zero. The hook's path
parsing SHALL be quote-safe: every path it reads from git SHALL be the real path rather than git's C-quoted rendering,
so a path containing non-ASCII bytes is inspected exactly like any other and no check is silently skipped for it.

#### Scenario: A failing formatter check refuses the commit

- **WHEN** the project's formatter check fails while a formatter-owned file is staged
- **THEN** the guard refuses the commit and reports the formatter check's failure

#### Scenario: A force-staged generated artifact refuses the commit

- **WHEN** a generated artifact that the repository excludes (for example a Python bytecode file or a build output) is
  staged
- **THEN** the guard refuses the commit and names the offending path

#### Scenario: A path staged and then edited refuses the commit

- **WHEN** a path is staged and then modified again so the index and the working tree differ for that path
- **THEN** the guard refuses the commit and names the path

#### Scenario: A clean staged rename is not reported

- **WHEN** a path is renamed and staged with no further modification (`git mv`, nothing edited afterwards)
- **THEN** the guard does not report the rename as a path staged and then edited, and names no spurious path from the
  rename's old name

#### Scenario: A staged conflict marker refuses the commit

- **WHEN** a staged file carries a merge conflict branch marker
- **THEN** the guard refuses the commit and names the offending file and line

#### Scenario: A misplaced captured marker refuses the commit

- **WHEN** a staged guidance file — `AGENTS.md`, or a project-authored `.opencode/agent/*.md` or
  `.opencode/skills/*/SKILL.md` — carries a `captured:` marker that is not inside a rule block (for example one sitting
  on a plain `- Text` bullet)
- **THEN** the guard refuses the commit and names the offending marker's location

#### Scenario: A clean staged set proceeds

- **WHEN** the staged set carries none of the detected defects
- **THEN** the guard passes and the commit proceeds

#### Scenario: A non-ASCII path is inspected rather than skipped

- **WHEN** a staged path contains non-ASCII bytes
- **THEN** the guard reads the real path rather than git's quoted rendering, so every check treats that path exactly as
  it would an ASCII path instead of skipping it

#### Scenario: A non-ASCII formatter-owned file reaches the formatter check

- **WHEN** a staged formatter-owned file has a non-ASCII name (e.g. `café.md`)
- **THEN** it is selected for the formatter check, so its formatting is verified rather than silently passed

#### Scenario: A non-ASCII generated artifact is refused

- **WHEN** a staged path that the repository excludes (a generated artifact) has a non-ASCII name
- **THEN** the guard reports it as a force-staged artifact and refuses the commit, so the escape check completes for a
  non-ASCII path as it would for an ASCII one

#### Scenario: A non-ASCII path staged and then edited is refused

- **WHEN** a non-ASCII path is staged and then modified again so the index and the working tree differ for it
- **THEN** the guard names that path and refuses the commit

#### Scenario: An unpaired archive move refuses the commit

- **WHEN** a change dir under `openspec/changes/archive/**` that carries delta specs is staged while the spec sync
  `openspec archive` wrote under `openspec/specs/**` is left unstaged or untracked
- **THEN** the guard refuses the commit and names the archived change and the un-staged spec paths

#### Scenario: A no-delta archive move requires no spec sync

- **WHEN** a change dir under `openspec/changes/archive/**` with no delta specs (a `skip_specs` change) is staged
- **THEN** the guard does not require a spec sync and does not report the archive move

### Requirement: The guard is activated per clone and does not alter state

The repository SHALL document how a clone activates the guard by pointing `core.hooksPath` at the tracked hooks
directory, and SHALL provide an install step that applies it. The guard SHALL be bypassable with
`git commit --no-verify` for a deliberate exception and SHALL NOT modify the working tree or the index.

#### Scenario: A clone activates the guard

- **WHEN** a contributor runs the documented install step
- **THEN** `core.hooksPath` points at the tracked hooks directory and the guard runs on subsequent commits

#### Scenario: A deliberate exception can bypass the guard

- **WHEN** a contributor commits with `--no-verify`
- **THEN** the guard does not run and the commit proceeds

#### Scenario: The guard does not change repository state

- **WHEN** the guard runs
- **THEN** the working tree and the index are unchanged by it

### Requirement: Captured marker placement is verified by the build

The standard `check` task SHALL verify that each `captured:` provenance marker in `AGENTS.md` and in the
project-authored guidance files (`.opencode/agent/*.md` and `.opencode/skills/*/SKILL.md`) lies within a rule block — a
bold-lead bullet or a bold-lead paragraph — and never on a plain `- Text` bullet, so a misplaced marker fails the CI
`build` gate even when no local hook is installed or the hook was bypassed. A backticked mention of the token in prose
(`` `captured:` ``) is not a marker and SHALL NOT be treated as one. The marker's existence rule (each captured rule
carries a greppable `captured: <unit>` origin) is specified in `showcase/quality/agent-skills`.

#### Scenario: The standard check verifies marker placement

- **WHEN** the standard `check` task runs
- **THEN** it includes the `captured:` marker-placement verification for `AGENTS.md` and each project-authored guidance
  file

#### Scenario: A marker outside a rule block fails the build

- **WHEN** a `captured:` marker in `AGENTS.md` or a project-authored guidance file sits on a plain `- Text` bullet
  rather than inside a rule block
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A backticked prose mention is not a marker

- **WHEN** a guidance file mentions the token in prose as a backticked `` `captured:` ``
- **THEN** the marker-placement verification does not treat that mention as a marker

#### Scenario: Correctly placed markers pass

- **WHEN** every `captured:` marker in `AGENTS.md` and the project-authored guidance files lies within a rule block
- **THEN** the marker-placement verification passes

#### Scenario: A misplaced marker in a project guidance file fails the build

- **WHEN** a `captured:` marker in a project-authored `.opencode/agent/*.md` or `.opencode/skills/*/SKILL.md` file sits
  on a plain `- Text` bullet rather than inside a rule block
- **THEN** the marker-placement verification fails and names that file and the offending location
