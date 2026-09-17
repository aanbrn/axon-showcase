## 1. Hold the pin back

- [x] 1.1 Pin `paketo-nginx` to `1.2.0` in `gradle/libs.versions.toml`; the builder (`0.4.642`) and procfile (`5.15.0`)
      pins are unchanged.
- [x] 1.2 Record the reason in `AGENTS.md` with the upstream reference
      ([paketo-buildpacks/nginx#1340](https://github.com/paketo-buildpacks/nginx/issues/1340)) and the close-out, and
      update the version it quotes.

## 2. Verify

- [x] 2.1 Build the web UI image with the held-back pin: `./gradlew :showcase-web-ui:dockerBuildImage` succeeds.
- [x] 2.2 Run it, not just build it: the container starts, `GET /` returns HTTP 200, and `GET /config.js` returns the
      rendered API base URL.
- [x] 2.3 Confirm the failure is gone rather than merely different: the same build with `1.2.1` fails in this
      environment before the change, which is what the pin addresses.
- [x] 2.4 `openspec validate --all` passes.
