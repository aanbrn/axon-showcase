# Tasks

## 1. Publish the images from the release workflow

- [x] 1.1 Extend `.github/workflows/release.yml`: grant `packages: write` and derive the GHCR owner (lowercased) and the
      image list; verify with `./gradlew workflowLint` and by reading the file's `permissions:` block.
- [x] 1.2 Add the build setup and step mirroring `.github/workflows/deployment-smoke.yml`: JDK 21
      (`actions/setup-java`), the Gradle cache with `nodejs` in `gradle-home-cache-includes`, the `~/.npm` cache keyed
      on `showcase-web-ui/package-lock.json`, the `pack` CLI (`buildpacks/github-actions/setup-pack`), the `overlay2`
      Docker step, then `./gradlew bootBuildImage :showcase-web-ui:dockerBuildImage -Pversion=<version>`; verify
      `workflowLint` passes, the step order mirrors the smoke, and the build uses the released version.
- [x] 1.3 Add the login and push: `docker login ghcr.io` with the run's `GITHUB_TOKEN`, then `docker tag` and
      `docker push` each of the five images as `ghcr.io/<owner>/<image>:<version>` and `:latest` (`linux/amd64`); verify
      the tag/push loop covers all five images and both tags.
- [x] 1.4 Order the tag and Release creation after the push, so a failed publish creates neither; verify by reading the
      step order and that `gh release create` remains last.
- [x] 1.5 Add the `dry_run` `workflow_dispatch` input and scope the `main`-branch guard to the release-creation path
      only, so `dry_run=true` runs from a branch and pushes only a throwaway tag (no `latest`, `<version>` tag, or
      Release); verify with `workflowLint` and by reading the guard's condition.
- [x] 1.6 Route the new `pack` pin into the `toolingUpdates` check, holding the invariant that every check's
      `workflowFile` is also registered in `pinFiles` (an unregistered file fails the task): add
      `.github/workflows/release.yml` and `.github/workflows/deployment-smoke.yml` (closing the smoke's pre-existing
      gap) to `pinFiles`; add `pack-cli (release.yml)` and `pack-cli (deployment-smoke.yml)` entries (pattern
      `pack-version:\s*(v?[0-9][^\s]*)`, source `buildpacks/pack`); and rename the existing `pack-cli` entry to
      `pack-cli (e2e.yml)` so the report names one row per file; verify `./gradlew toolingUpdates` runs with a row per
      registered file and, as the known-bad control, temporarily lower the `release.yml` pin and confirm the report
      names it, then revert.

## 2. Documentation and records

- [x] 2.1 Add `docs/adr/0018-publish-service-images-to-ghcr.md` recording GHCR, release-only trigger, `<version>` +
      `latest` tags, `linux/amd64`, and the public-visibility consequence; verify it follows the ADR format in
      `docs/adr/README.md` (`Status:` vocabulary included).
- [x] 2.2 Add a "run from the published images" path to `README.md` (`docker pull ghcr.io/aanbrn/axon-showcase-*`), and
      refresh every README description of the release workflow — derive them by grepping `release.yml`: the Docker
      Images / Getting Started material and the Continuous Integration release paragraph (~line 850) — to name image
      publication and the dry-run input, and note that the packages must be made public for anonymous pulls; verify the
      documented command matches the published tag scheme and the visibility note is present.
- [x] 2.3 Refresh every live `AGENTS.md` description of the release and every set `release.yml` joins — derive them by
      grepping `release.yml` and the workflow-list members it joins: the `release.yml` bullet (~line 790 — qualify its
      "requires the `main` ref" for the dry-run input and name image publication), the web-UI/nodejs-cache workflow
      enumeration (~line 670 — add `release.yml`), the Docker Images section, and the Kubernetes Deployment sentence
      "The chart's images are published to no registry" (~line 1488); verify by grepping `release.yml` and confirming
      every hit now holds.
- [x] 2.4 Refresh the `Docker images:` line in `openspec/config.yaml`'s `context:` block to name the registry; verify
      the file still parses (`openspec list --json`).
- [x] 2.5 Remove the "Nothing is published to a container registry" entry from `docs/ideas.md`; verify the entry is gone
      and the file's headings remain well-formed.
- [x] 2.6 Refresh the `showcase/quality/releases` `## Purpose` in `openspec/specs/showcase/quality/releases/spec.md` to
      cover image publication — a delta cannot carry a Purpose for an existing capability, so this edit lands in the
      archive commit; record the deferral in the change's report. Verify the refreshed Purpose names the images.
- [x] 2.7 Run `./gradlew spotlessApply` and `./gradlew spotlessCheck` after the final edit; verify both pass.

## 3. Verification

- [x] 3.1 Build the images locally with the exact workflow command
      (`./gradlew bootBuildImage :showcase-web-ui:dockerBuildImage -Pversion=<version>`) and confirm the five
      `aanbrn/axon-showcase-*:<version>` images exist in the daemon.
- [x] 3.2 Confirm the delta validates: `openspec validate publish-images-to-ghcr` and `./gradlew workflowLint` both
      pass.
- [x] 3.3 On the pushed branch, exercise the publish path end to end:
      `gh workflow run release.yml --ref <branch> -f version=<v> -f dry_run=true`, then confirm the run succeeds, the
      five images appear under a throwaway tag, and no `v<version>` tag or GitHub Release exists. (Verified: run
      37875427614 pushed all five `ghcr.io/aanbrn/axon-showcase-*:0.2.0-dryrun-37875427614`; no `v0.2.0` tag or Release.
      The throwaway tags can't be deleted with the local token — no `delete:packages` scope — so that cleanup is parked
      below.)
- [x] 3.4 Confirm the failure path creates nothing: dispatch (or read the guard) for a non-dry-run from a branch and a
      malformed version, and confirm each run fails without creating a tag or Release.
- [x] 3.5 Run the `lesson-capture` subagent over the diff and the review findings, apply its durable proposals, and
      record the applied net `AGENTS.md` delta on this task (applied: ≈ +4 lines — the `toolingUpdates` multi-file entry
      clause and the guarded-dispatch `dry_run` bypass; `AGENTS.md` overall +58/−44 including the doc sweep).

## Workflow follow-up

- Make the `ghcr.io/aanbrn/axon-showcase-*` packages public once after the first publish (a per-package setting, no file
  diff) and name the enabling in the change's report.
- Delete the dry-run throwaway package versions (`0.2.0-dryrun-37875427614`, one per package); requires the
  `delete:packages` scope.
- Archive the change after the project's review requirements are satisfied.
- The next real release from `main` exercises the `latest` + tag/Release path.
