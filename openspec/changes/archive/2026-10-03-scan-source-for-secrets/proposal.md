# Proposal

## Why

`dependencySecurityCheck` (Snyk) scans dependencies, not source, and no other gate reads the repository's own files for
secrets — so a committed token — an API key, a password, a private key — passes every gate and only surfaces when
someone notices. A source-secret scan is the missing guard: a defect the pull request itself introduced, and one the PR
can remove, so it belongs in the merge gate.

## What Changes

- Add a **gitleaks** source-secret scan to the `ci.yml` `build` gate: a pinned gitleaks CLI binary fetched from a
  versioned release asset with a verified checksum (a new CI mechanism — no workflow downloads a release asset today),
  running `gitleaks dir` over the working tree, failing the check on any finding.
- Add a committed `gitleaks.toml` that extends gitleaks' default rules and **allowlists the generated and downloaded
  paths** (`build/`, `.gradle/`, `node_modules/`), which are not source and produce false positives — verified: the scan
  finds five `generic-api-key` hits today, all in `build/` reports/test-results and `.gradle/nodejs` headers, and is
  clean once those paths are excluded.
- Register the gitleaks pin in the `toolingUpdates` check (a `gitleaks-cli` entry reading the workflow pin), so a newer
  release is reported like the other pinned CLIs.
- Refresh the docs (`AGENTS.md`, `README.md`) and the specs, and remove the implemented idea.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/merge-governance`: extends the "Pull requests run the fast quality gate" requirement to include the
  source-secret scan, so a committed secret fails the fast gate.

## Impact

- **Files**: `.github/workflows/ci.yml` (the scan step), a new `gitleaks.toml`, `build.gradle.kts` (the `toolingUpdates`
  pin), `AGENTS.md`, `README.md`, `docs/ideas.md`, and `openspec/config.yaml` if its `context:` block names the CI gate
  set, plus the change dir and the `merge-governance` delta. `scripts/doctor.sh` and the README Prerequisites table are
  deliberately unchanged (gitleaks is CI-only, not a machine prerequisite).
- **Build / tests / services**: no application code change; the scan runs in CI only (it needs the gitleaks binary,
  which is not a local build prerequisite, so it stays a `ci.yml` step like the OpenCode config probe, not a `check`
  member).
- **Verification**: the gate is proven against a known-bad input (a planted token fails the scan) and a known-good one
  (the current source tree passes once generated paths are allowlisted).
