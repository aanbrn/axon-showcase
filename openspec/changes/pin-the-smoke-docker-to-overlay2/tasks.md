# Tasks

## 1. Switch the runner's Docker to overlay2

- [x] 1.1 `.github/workflows/deployment-smoke.yml` — add a step before _Build the images_ that writes
      `{"storage-driver":"overlay2"}` to `/etc/docker/daemon.json`, restarts Docker, and waits for it with a bounded
      poll (`until docker info >/dev/null 2>&1; do sleep 1; done`), with a comment naming the reason and the owning
      reference `buildpacks/pack#2527`. Verify with `./gradlew workflowLint` green and by reading the step — the file is
      the runner's, the step precedes the builds and the kind cluster, and the poll is bounded.
- [x] 1.2 `AGENTS.md` — two edits: (a) the buildpack note gains the constraint — a Docker 29 daemon (what `ubuntu-26.04`
      ships) needs `overlay2` for the `pack` web UI build, the smoke's step sets it, and the step **retires when
      `buildpacks/pack#2527` (the owning venue, **resolved: open, its body carries our exact
      `failed to fetch base layers: open /tmp/imgutil.local.image.2344428218/...` line**) lands a fix** — with
      `spring-projects/spring-boot#49251` named as the downstream thread; that reference, in a file `upstreamReferences`
      scans, is the close-out (task 1.3 removes the `docs/ideas.md` entry that currently carries `#2527`); (b) the
      `ubuntu-latest` bullet's sentence that the 26.04 break "is parked in `docs/ideas.md`" becomes false once this
      lands, so rewrite it to say the smoke now sets `overlay2` for it. Verify by reading both paragraphs and that the
      references resolve.
- [x] 1.3 Remove the implemented idea from `docs/ideas.md` (the "Fix the web UI image build on Ubuntu 26.04" entry),
      leaving the unrelated entries — the upstream reference it carried now lives in `AGENTS.md` (task 1.2). Verify the
      entry is gone and no other was disturbed. — verified: only its 23 lines went (with the `## 2026-10-02` heading);
      the surviving ideas are untouched.

## 2. Validation and close-out

- [ ] 2.1 Prove the fix on the failing image: dispatch the smoke on the branch against `ubuntu-26.04` (its `runner`
      input — the recipe is in the `AGENTS.md` note) and confirm it completes green — all five images build and the
      smoke profile passes. Record the run URL. If it still fails at the web UI build, record the failure rather than
      proceeding (this is the check the change exists for).
- [ ] 2.2 Prove the default path is unchanged: dispatch the smoke without the input (`ubuntu-latest`, Docker 28 today)
      and confirm it succeeds — the check that the daemon rewrite/restart does not disturb the Docker 28 path, which is
      expected to be `overlay2` already. Record the run URL.
- [ ] 2.3 `./gradlew spotlessApply`, then `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm green.
      Re-run `spotlessApply` after the last edit to a Spotless-owned file (a task tick included).
- [ ] 2.4 Run the `lesson-capture` subagent over the diff, review findings, and change dir; apply its durable
      `AGENTS.md` proposals and record the applied net `AGENTS.md` delta on this task.
