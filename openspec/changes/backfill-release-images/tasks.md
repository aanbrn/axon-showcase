# Tasks

## 1. Add the backfill mode to the release workflow

- [x] 1.1 In `.github/workflows/release.yml`, add an optional `publish_tag` input and make `version` optional
      (`required: false`); verify with `./gradlew workflowLint` and by reading the `on.workflow_dispatch.inputs` block.
- [x] 1.2 Add the mode selection and validation: a `publish_tag` must match `v<MAJOR.MINOR.PATCH>` and name an existing
      tag, the release path requires `version` (validated against `gradle.properties` as today), and the run is rejected
      when neither `version` nor `publish_tag` is given, when both are given, when `publish_tag` is malformed or names
      no existing tag, and when `publish_tag` is combined with the dry-run input; verify with `workflowLint` and by
      reading the guard conditions.
- [x] 1.3 Check out the tag on the backfill path (`actions/checkout` `ref: ${{ inputs.publish_tag }}`) and build with
      the version derived from the tag
      (`./gradlew bootBuildImage :showcase-web-ui:dockerBuildImage -Pversion=<derived>`); verify by reading the checkout
      `ref` and the build step.
- [x] 1.4 On the backfill path, push only `<version>` (skip `latest`), skip tag/Release creation, and let the `main`-ref
      guard pass for it (the `dry_run` precedent); verify with `workflowLint` and by reading the push loop and the
      guard.

## 2. Documentation

- [x] 2.1 Document the backfill dispatch in `README.md`, deriving the sites by grepping for the workflow name and the
      registry phrase: the Continuous Integration release paragraph and the "Run from the Published Images" section;
      verify the documented command matches the input name.
- [x] 2.2 Refresh every live `AGENTS.md` description of the release's image publishing and of the workflow's `main`-ref
      bypasses — the `release.yml` bullet, the Docker Images section, and the guarded-dispatch paragraph (~line 755,
      which names only the `dry_run` bypass) — verifying by grepping `publish_tag` and reading each site.
- [x] 2.3 Update `docs/adr/0018-publish-service-images-to-ghcr.md` — both its Decision ("On a release dispatch … tagged
      with the released version and `latest`") and its Consequences ("release-only") — to record the backfill as a
      second publishing trigger that omits `latest`; check `docs/adr/0016-releases-and-versioning.md` and record that
      its tag-exists rejection still holds for the release path; verify both ADRs by reading them.
- [x] 2.4 Refresh `openspec/config.yaml`'s Docker-images context line to name the backfill trigger; verify it parses by
      running `openspec instructions proposal --change backfill-release-images` (which loads the config) and confirming
      it emits no `could not parse` / `must be an array of strings` warning, as the CI probe checks.
- [x] 2.5 Run `./gradlew spotlessApply` and `./gradlew spotlessCheck` after the final edit; verify both pass.

## 3. Verification

- [x] 3.1 Confirm the delta validates (`openspec validate backfill-release-images --strict`) and
      `./gradlew workflowLint` passes.
- [ ] 3.2 On the pushed branch, run the real backfill for the existing first release:
      `gh workflow run release.yml --ref <branch> -f publish_tag=v0.1.0`, then confirm the run builds from the `v0.1.0`
      tag and pushes all five `ghcr.io/aanbrn/axon-showcase-*:0.1.0` images, and that no `latest` tag or GitHub Release
      is created or moved.
- [ ] 3.3 Confirm every failure path creates nothing: dispatch with an unknown tag, a malformed tag, no input, both
      inputs, and a `publish_tag` plus the dry-run input, and confirm each run fails before publishing — no image
      pushed, no tag or release created.
- [x] 3.4 Run the `lesson-capture` subagent over the diff and the review findings, apply its durable proposals, and
      record the applied net `AGENTS.md` delta on this task (verdict: nothing durable — net 0 lines; the recurring
      review classes restated existing rules rather than revealing a missing one).

## Workflow follow-up

- Make the `ghcr.io/aanbrn/axon-showcase-*` packages public once for anonymous pulls (a per-package setting, no file
  diff); the README documents it.
- Archive the change after the project's review requirements are satisfied.
