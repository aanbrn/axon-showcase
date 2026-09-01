## 1. Concrete pins

- [x] 1.1 Update `prometheus-community-stack`, `grafana-tempo`, and `bitnami-common` in `gradle/libs.versions.toml` to
      concrete versions (`77.14.0`, `1.24.4`, `2.41.0`) and verify no floating `x.x` chart coordinates remain in the
      catalog
- [x] 1.2 Verify the pinned common subchart resolves: `helm dependency build` on `helm/chart` (or the equivalent Gradle
      helm task) succeeds with `common` at `2.41.0`, and `helmInstallToLocal` still renders the releases

## 2. Documentation

- [x] 2.1 Document in `AGENTS.md` (next to the infra image-version note) that every Helm chart coordinate in the
      catalog is a concrete version — never a floating `x.x` pin — keeping lines within 120 characters
- [x] 2.2 Add `--version` flags matching the catalog pins to the manual `helm install` commands in `AGENTS.md` and
      `README.md` so the documented manual path no longer contradicts the verified pins

## 3. Verification

- [x] 3.1 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      infra gate, all module checks, and spec validation pass with the concrete chart pins