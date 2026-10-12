# Tasks

## 1. The paired bump

- [x] 1.1 Inspect the new builder (`pack builder inspect paketobuildpacks/builder-jammy-base:0.4.659`) and record what
      it bundles for `nginx` and `procfile`.
- [x] 1.2 Set `paketo-builder-jammy-base` to `0.4.659` and `paketo-procfile` to the version the builder bundles
      (`5.15.3`); **leave `paketo-nginx` at `1.3.0`** (the builder's bundled version), recording why.
- [x] 1.3 Refresh `AGENTS.md`'s buildpack-pin gotcha, which names the pins as the current versions (`procfile` `5.15.3`,
      `builder-jammy-base` `0.4.659`; `nginx@1.3.0` unchanged) — the docs-refresh convention's version-bump clause.

## 2. Verification

- [x] 2.1 Build the image (`./gradlew :showcase-web-ui:dockerBuildImage -PimagePlatform=linux/amd64`).
- [x] 2.2 **Run** the built image: the container comes up on the amd64 image (`docker exec uname -m` → `x86_64`, image
      platform `linux/amd64`), `GET /` returns the UI (HTTP 200), and the log shows the bumped `Procfile 5.15.3`. The
      binary architecture was read directly from its layer (`/layers/paketo-buildpacks_nginx/nginx/sbin/nginx`; the ELF
      header's machine field is `3e 00` = **x86-64**, where a mismatched pin would read `b7 00`) — a
      `file $(which nginx)` probe on the runtime `PATH` returns not-found because the binary lives in the layer. The
      metrics chain was checked too — `/stub_status` on `9090` returns 200 with the stub_status body (`/` on that port
      is a 404, which is why the path matters).
- [x] 2.3 Re-run `./gradlew buildpackUpdates` and confirm the builder and procfile rows clear, and that the
      `paketo-nginx 1.3.0 -> 1.3.1` row remains (deliberately not actionable).
- [x] 2.4 Run a `review-quick` pass over the proposal and the implementation, repeating until clean.

## 3. Capture

- [x] 3.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: **net +3 rendered lines** (the raw churn is the Docs-refresh paragraph
      reflowing) — the buildpack-verification bullet gained the _method_ its check needs: read the binary's architecture
      from the buildpack's layer, not the runtime `PATH`, and decode the ELF `e_machine` (`3e 00` = x86-64, `b7 00` =
      AArch64); the recipe had existed only inside an archived change's `tasks.md`, which nothing reads. Also fixed the
      previous unit's Docs-refresh clause, which shipped without its `captured:` marker (now
      `captured:     bump-dependencies-2026-10-12`). No retirements proposed. The capture judged the report's nginx row
      (already covered by the pairing rule), the missed docs refresh (a non-compliance with the clause #555 added — not
      a rule gap) and the over-claim (covered by the claim-strength rule; its proximate cause is the missing method) as
      already covered.
- [x] 3.2 Ask the owner for the manual review pass, after 2.4 is clean and 3.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm and Snyk-tooling findings remain separate units.
