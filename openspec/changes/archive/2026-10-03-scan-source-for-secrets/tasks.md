# Tasks

## 1. The scan config and the gate step

- [x] 1.1 Add `gitleaks.toml` at the repository root: `[extend] useDefault = true` plus a global `[[allowlists]]` whose
      `paths` cover the generated/downloaded trees (`build/`, `.gradle/`, `node_modules/`), with a comment stating the
      policy (scan source; the excluded trees are gitignored, so their bytes cannot be committed). Note `gitleaks dir`
      scans the filesystem including untracked files, so the exclude is what scopes it to committable source. Verify
      with `gitleaks dir . --config gitleaks.toml` reporting no leaks over the current tree.
- [x] 1.2 Add a `Secret scan (gitleaks)` step to `.github/workflows/ci.yml`, **after** the quality gates (the fast
      `./gradlew check` runs first; the scan is a secondary guard, not a reason to delay the fast path): download the
      pinned gitleaks release asset (`gitleaks_<version>_linux_x64.tar.gz`) with the checksum verified, then run
      `gitleaks dir . --config gitleaks.toml --no-banner --redact`, failing the step on a finding. Borrow actionlint's
      **retry shape** (a bounded loop) for the download; note the pin + checksum is a new CI mechanism (no workflow
      downloads a release asset or verifies a checksum today). Verify `./gradlew workflowLint` passes and the step's
      shell is syntactically valid.
- [x] 1.3 Register a `gitleaks-cli` entry in the root `build.gradle.kts` `toolingUpdates` task (a `ToolingUpdateCheck`
      with `workflowFile = "ci.yml"`, a `pinPattern` matching the pinned version, `source = GITHUB_RELEASE`,
      `sourceRef = "gitleaks/gitleaks"`), and add `ci.yml` to the task's `pinFiles` if not already there. Verify
      `./gradlew toolingUpdates` runs and reports the gitleaks pin.

## 2. Documentation

- [x] 2.1 Refresh the docs the change falsifies, naming each site (derive them by grepping, not from memory):
  - `AGENTS.md`'s Continuous Integration section — add the secret scan to the fast-gate description (~~`:640`), and the
    tooling-updates pin enumerations that name "the OpenSpec, Snyk and `pack` CLIs" (~~`:772` "What each covers", and
    the pinned-tool update rule ~`:2494`) — those gain `gitleaks`.
  - `README.md`'s `Technologies` security line (~~`:182`, "Snyk and `npm audit` — dependency security scanning") and its
    GitHub Actions line (~~`:187`, "tooling updates, security scans"), and the tooling-updates section (~`:752`).
  - The two `e2e.yml` "no secrets" phrases (`README.md:728`, `AGENTS.md:706`) — their meaning ("no secrets" = e2e needs
    no secrets to run) is now ambiguous beside a source scanner; reword for clarity.
  - `openspec/config.yaml`'s `context:` block — check whether it should name the source scan (the un-gated copy of the
    project facts); add it only if the block describes the CI gate set.
  - Remove the implemented idea from `docs/ideas.md` (the `## 2026-09-24` "Scan the source tree for secrets" entry — the
    section holds other entries, so keep the heading).
  - **Deliberately unchanged** (record the boundary, do not silently skip): `scripts/doctor.sh`'s probe table and the
    README **Prerequisites** table — gitleaks is CI-only, not a machine prerequisite (see design Non-Goals). Verify by
    reading each edited site and `grep -n "gitleaks" AGENTS.md README.md` hitting the intended lines, and
    `grep -n "Scan the source tree for secrets" docs/ideas.md` returning nothing.
- [x] 2.2 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck` and
      `openspec validate --changes`, and confirm all pass.

## 3. Verification

- [x] 3.1 Prove the scan with a known-bad and a known-good input: plant a fake secret in a source file (a high-entropy
      token matching a gitleaks rule), confirm `gitleaks dir . --config gitleaks.toml` fails naming the rule and file,
      remove it, and confirm the scan passes clean. Record both runs' output; leave the tree clean.
- [x] 3.2 (local, informational) Confirm the scan's scope: with the tree clean, the scan reports no leaks in ~2 s over
      the source-only allowlisted paths, and the same tree without the allowlist reports the generated-output false
      positives it excludes — evidencing the allowlist is what scopes the scan. Record the counts.
- [x] 3.3 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task.
- [x] 3.4 Add a scenario-preservation check: confirm the delta's `MODIFIED` block carries the main spec's "Pull requests
      run the fast quality gate" description and all six existing scenarios verbatim by name and order, with only the
      source-secret clause and the two new scenarios added. Verify against
      `openspec/specs/showcase/quality/merge-governance/spec.md`.
