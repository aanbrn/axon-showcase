# Design

## Context

See `proposal.md` — Why. Verified against the repository and the tools:

- **No source scan exists**: `dependencySecurityCheck` is `snyk test --all-sub-projects` (dependencies only); no gate
  reads the repository's own files for secrets.
- **gitleaks is available and current**: `gitleaks` `v8.30.1` (latest release), installable from a versioned release
  asset (`gitleaks_8.30.1_linux_x64.tar.gz` + a checksums file). No workflow today downloads a release asset or verifies
  a checksum (actionlint installs an unpinned `latest`), so the step's pin-and-checksum install is **new** here (D1).
- **The `dir` scan command**: `gitleaks dir <path>` scans a directory's files (v8.19+ renamed `detect` → `dir`/`git`);
  `git` mode scans history. The merge gate scans the **working tree** (`dir`), which is the PR's files — no history
  needed, and no `fetch-depth: 0`.
- **The tree is clean once generated paths are excluded**: a `dir` scan of the whole tree reports **5**
  `generic-api-key` findings — all in `showcase-api-gateway/build/reports`, `build/test-results`, and
  `showcase-web-ui/.gradle/nodejs/…/v8-internal.h` (generated/downloaded). With `build/`, `.gradle/`, and
  `node_modules/` allowlisted the scan is **clean** and much faster (a point-in-time local probe measured ~20 s / ~140
  MB → ~2.3 s / ~18 MB; the byte figure scales with the local build output a fresh CI checkout lacks, so it is a probe,
  not a constant).
- **The owner is a personal account** (`aanbrn` is a `User`), so `gitleaks-action`'s organization license would not be
  needed — but the CLI is chosen anyway (below).
- **Upstream status**: gitleaks is "feature complete" (security patches only) and its maintainer is shifting focus to
  `Betterleaks`. A deliberate choice to record, not a blocker.

## Goals / Non-Goals

**Goals:**

- Fail the `ci.yml` `build` check when the pull request's files contain a secret, naming the rule and location.
- Keep the false-positive surface to a committed, reviewable allowlist of generated/dependency paths.

**Non-Goals:**

- Scanning git **history** for already-committed secrets — a history scan (`gitleaks git`) is a different scope, catches
  a leak a lockfile removal cannot fix (the bytes stay in history), and needs the full history; the merge gate guards
  new commits, and a history sweep is a separate decision.
- A `check`-member Gradle task — gitleaks is not a local build prerequisite (like the `opencode` binary in the config
  probe), so the scan is a `ci.yml` step, not wired into `check`.
- A pre-commit hook — the scan runs in CI where it cannot be bypassed; a local hook would duplicate it and need every
  contributor to install gitleaks.
- **Wiring the scan into `check` or the toolchain doctor (`scripts/doctor.sh`).** gitleaks runs CI-only: no local build,
  test, or deployment task needs it, so it is not a machine prerequisite. `doctor.sh`'s own scope is "the toolchain the
  build, tests, and deployment depend on", and its rows are anchored to the README's **Prerequisites** table (the tools
  a machine needs: actionlint for `check`, Snyk for `dependencySecurityCheck`, `pack` for the image). gitleaks joins
  neither — it is the same class as the `opencode` binary the config probe uses, a CI-step tool, not a local one — so no
  probe row and no Prerequisites entry are added, and the omission is deliberate.
- Replacing or re-scoping `dependencySecurityCheck` — that scans dependencies; this scans source. Two scanners, two
  subjects.
- **Scanning on the `main` push path.** The scan is a pull-request check only, matching the fast-gate placement: a
  secret is caught at the PR that introduces it, and a `push` to `main` is not blockable by a required check anyway — so
  the "Pushes to main run the full quality gate" requirement is deliberately **not** extended, because a secret catches
  nothing there that the PR gate did not already catch.

## Decisions

### D1: The gitleaks **CLI**, not `gitleaks-action`, installed by a pinned, checksum-verified download

The scan runs the CLI binary fetched from a versioned release asset, not the `gitleaks-action`. The action adds a
GitHub-API comment surface (`GITHUB_TOKEN`), a license-key requirement for organization accounts, and an automatic
`fetch-depth: 0` — none of which the merge gate needs, and the last of which would slow every checkout.

The **install mechanism is new to this repository**, and the design states it as such rather than borrowing it: no
workflow today downloads a release asset or verifies a checksum (`grep` over `.github/workflows/` is empty), and the
closest precedent, actionlint, installs an **unpinned** `latest` via the tool's own script. This step pins the version
(`gitleaks_<version>_linux_x64.tar.gz`) and verifies the published checksum, because a scanner's rule set is
security-relevant and an unpinned scan is a moving target; it borrows only actionlint's **retry shape** (a bounded loop
around the download, for the same transient-failure reason).

- **Alternative — `gitleaks-action@v3`:** convenient PR comments and a job summary, but it couples the gate to the
  action's release cadence, its Node-24 runtime chronology, and its license/comment surface — heavier than a source scan
  needs, and the action's own README notes a code-scanning alert a subsequent commit "resolves" even though the secret
  remains in history (a false comfort the CLI avoids).
- **Alternative — mirror actionlint's unpinned `latest` install:** the simplest precedent, but it leaves the scanner's
  rule set unpinned and untracked; a security tool's version is a deliberate pin (and joins `toolingUpdates`, D4),
  unlike actionlint which is deliberately unpinned.
- **Alternative — a Docker run of the gitleaks image:** adds an image pull to every run where a static binary suffices.

### D2: Scan the working tree (`dir`), failing the fast gate

`gitleaks dir . --config gitleaks.toml` scans the checked-out files; a finding exits non-zero and fails `build`. The
merge-gate placement follows the repository's rule — a check belongs in the pull-request gate when the change that trips
it can remediate it, and a secret the PR introduced is exactly that.

- **Alternative — a scheduled observational scan (`dependency-security.yml`, like Snyk):** a token committed on Monday
  would not fail until the weekly run, and by then it is in history; the defect is the PR's, so it is gated there.
- **Alternative — scan only the PR's changed files:** narrower and faster, but a `dir` scan of the small working tree is
  already ~2 s, and a whole-tree scan also catches a pre-existing leak a later PR touches; the extra scope is cheap.
- **Alternative — `gitleaks git` (history):** a Non-Goal above; history is unbounded and the merge gate guards new code.

### D3: The allowlist is a committed `gitleaks.toml` scoping the scan to source

A `gitleaks.toml` extends gitleaks' default rules (`[extend] useDefault = true`) and adds global path allowlists for the
generated/downloaded trees (`build/`, `.gradle/`, `node_modules/`), so the scan reads source only and the false
positives the full-tree probe found do not recur. A per-finding allowlist (a path + rule + line) is added only if a real
false-positive pattern survives the directory excludes — the directory scope is the first and bluntest filter.

- **Alternative — no allowlist, exclude at the command line (`--redact` + `--log-opts`/path args):** gitleaks has no
  "exclude path" flag; scoping is the config's job, so a committed allowlist is the mechanism.
- **Alternative — disable the `generic-api-key` rule:** it is the rule catching the false positives, but it is also the
  rule catching a real generic token; disabling it trades a false positive for a false negative. Exclude the paths, not
  the rule.
- **Alternative — a `.gitleaksignore` fingerprint list:** fingerprints pin exact findings, so every new generated-file
  false positive would need a new entry; the path allowlist covers the class.

### D4: Pin the CLI and register it in `toolingUpdates`

The version is pinned in the `ci.yml` step (a release-asset URL and a checksum), and a `gitleaks-cli` entry is added to
the `toolingUpdates` check (a `pinPattern` over `ci.yml`, `source = GITHUB_RELEASE`, `sourceRef = gitleaks/gitleaks`),
so a newer release is reported like the other pinned CLIs.

- **Alternative — a floating `latest` download:** leaves the gate's tool version unpinned and untracked; the repository
  pins its tools and tracks their currency.
- **Alternative — `brew install` in CI:** brew on the runner is slower and less reproducible than the versioned asset.

## Risks / Trade-offs

- [gitleaks is feature-complete and its maintainer is moving to `Betterleaks`] → accepted and recorded: the pinned
  version's rules are stable, security patches continue, and the upgrade path (to `Betterleaks` or a successor) is a
  future dependency decision, the same class as any tool bump. A tool whose rules go stale would be caught by a real
  leak slipping through, which is the residual risk of any scanner.
- [The allowlist could hide a real secret in an excluded path] → the excluded paths are generated/downloaded trees
  (`build/`, `.gradle/`, `node_modules/`) that are gitignored, so a secret there never reaches a commit; the residual is
  narrower but real: `gitleaks dir` scans the **filesystem** including untracked files, so a secret a developer wrote
  into an excluded path is not scanned — accepted, since the gate's subject is what can be committed (the excluded trees
  are gitignored, so those bytes cannot enter the repository).
- [A real secret is allowlisted as a false positive] → the allowlist is path-scoped, reviewed in the diff, and does not
  add a rule-level suppression, so a genuine finding in a source path still fails.
- [The download is a network dependency in the gate] → actionlint's retry shape applies (a bounded loop); the asset
  fetch is one pinned URL with a checksum, and a transient failure retries.

## Migration Plan

Not applicable — a CI-step addition with a committed config. Rollback is removing the step and the config.

## Open Questions

None.
