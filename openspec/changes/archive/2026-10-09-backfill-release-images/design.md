# Design

## Context

See `proposal.md` for motivation and the `showcase/quality/releases` delta for the requirements.

Current state that shapes the approach:

- `.github/workflows/release.yml` has a required `version` input and an optional `dry_run` input; it validates the
  version against `gradle.properties`, refuses an existing tag, checks out the dispatched ref (default), builds
  `bootBuildImage :showcase-web-ui:dockerBuildImage -Pversion=<version>`, pushes the five images as `<version>` and
  `latest`, and then creates the tag and GitHub Release. The `main`-ref guard applies to the non-`dry_run` path.
- A release tag exists (`v0.1.0`, from before the publishing workflow) whose images were never published, and its source
  declares `0.1.0-SNAPSHOT` in `gradle.properties` — so building it with `-Pversion=0.1.0` yields `:0.1.0` images.
- A `workflow_dispatch` runs the workflow file from the dispatched ref, but `actions/checkout` can check out a different
  ref — so the workflow logic (from `main`) can build a tag's source.

## Goals / Non-Goals

**Goals:**

- Publish the images for an existing release tag, without creating or mutating a tag or a GitHub Release.
- Keep the release path unchanged, and reuse its build and push steps.
- Make the backfill verifiable before merge (its verification is the real v0.1.0 backfill).

**Non-Goals:**

- Moving `latest` on a backfill (it follows the newest release).
- Re-cutting or editing an existing release, or any git/Release mutation.
- Making an old tag build if its pinned toolchain no longer works (best effort; a failure names the cause).

## Decisions

### A `publish_tag` input selects the backfill, and `version` becomes optional

A `workflow_dispatch` requires every `required: true` input, so a backfill must not be forced to pass a `version`. Add
an optional `publish_tag` (an existing release tag) and make `version` optional (`required: false`); the script selects
the mode and validates: a backfill requires `publish_tag` to match `v<MAJOR.MINOR.PATCH>` and to exist, the release path
requires `version`, supplying both is rejected, and `publish_tag` with the dry-run input is rejected (they are opposite
intents). **Rejected:** keeping `version` required and passing the tag's version redundantly (error-prone); a separate
`backfill.yml` workflow (a second dispatcher to keep in sync with the release logic); a boolean `backfill` input plus a
separate tag input (two inputs for one mode).

### Build the tag's source with `actions/checkout ref`

The dispatch runs the workflow file from the dispatched ref (`main`), so the new backfill logic is present even though
the tag's own workflow file predates it; the checkout step uses `ref: ${{ inputs.publish_tag }}` to build the tag's
source. **Rejected:** dispatching from the tag (its workflow file has no backfill logic); a branch/tag matrix with its
own checkout (no need — one ref).

### Backfill publishes `<version>` only and never moves `latest`

`latest` follows the newest release, so a backfill of an older release must not move it backward. The backfill pushes
only `<version>`. (For `v0.1.0`, which is currently the newest release, `latest` stays absent until the next release is
cut — acceptable and consistent.) **Rejected:** also setting `latest` (moves it backward for an older tag); a
`set_latest` flag (a knob with no caller).

### A backfill may be dispatched from any ref; the release path keeps its `main` guard

A backfill mutates no tag or Release — it only pushes image tags — so the `main`-ref guard need not apply. Relaxing it
for the backfill makes the mechanism verifiable from the change branch before merge, which is also the actual v0.1.0
backfill. The existing `dry_run` relaxation is the precedent. **Rejected:** keeping the guard (the backfill would first
run post-merge, against the repo's pre-merge-verification rule).

### Reuse the release path's build and push steps

Only the guards and the tag handling branch by mode; the JDK/Gradle/`pack`/`overlay2` setup, the build command, the GHCR
login, and the push loop are shared. The push loop pushes `<version>` and `latest` on the release path, but only
`<version>` on a backfill (`latest` skipped), and the tag/Release step runs only on the release path.

## Risks / Trade-offs

- **An old tag's pinned toolchain may no longer build on the runner.** `v0.1.0` predates the buildpack-pin enforcement;
  a backfill is best effort, and a failure names the failing step rather than failing silently.
- **A backfill creates the packages privately.** The owner makes them public after the first publish (README documents
  it) — unchanged from the release path's consequence.
- **`version` becoming optional loosens the dispatch contract.** The script fails clearly when neither `version` nor
  `publish_tag` is supplied, and the release-path validation is otherwise unchanged.
