## 1. Release DSL

- [x] 1.1 Set `namespace = "axon-showcase"` and `createNamespace = true` on the four app/infra releases in
      `build.gradle.kts` (db-events, kafka, os-views, axon-showcase), keeping the file within 120 characters
- [x] 1.2 Verify `helmInstallToLocal` renders all six releases into their declared namespaces: kps/tempo in
      `monitoring`, the four app/infra in `axon-showcase`

## 2. Documentation

- [x] 2.1 Update the manual `helm install` commands in `AGENTS.md` and `README.md` to pass `--namespace axon-showcase
      --create-namespace` for the four app/infra releases, keeping lines within 120 characters
- [x] 2.2 Note in `AGENTS.md` the declared namespace layout (observability in `monitoring`, app and infra in
      `axon-showcase`)

## 3. Spec and verification

- [x] 3.1 Sync the merge-governance spec with the new "Helm release namespaces are declared in the build" requirement
      and its scenarios, keeping lines within 120 characters
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      build, the Helm release rendering, and spec validation pass