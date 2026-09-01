## 1. Build configuration

- [x] 1.1 Wire the `local` release target's `kubeContext` in `build.gradle.kts` from `helm.local.kubeContext`, falling
      back to the current kube context when unset, and verify `./gradlew help` (or any task) resolves the build with no
      configuration errors
- [x] 1.2 Confirm the per-target `kubeContext` is respected by rendering/running a Helm task for the `local` target and
      verifying it targets the developer's local cluster (no `--kube-context` other than the resolved one)

## 2. Machine-local property

- [x] 2.1 Replace `helm.kubeContext=colima` with `helm.local.kubeContext=colima` in `~/.gradle/gradle.properties` on
      this machine and verify `./gradlew helmInstallToLocal` still deploys to the local colima cluster

## 3. Documentation

- [x] 3.1 Document the per-target kube-context convention in `AGENTS.md` (and `README.md` where the local-cluster
      deployment is described), including the per-machine `helm.local.kubeContext` property and the fallback to the
      current context, keeping lines within 120 characters
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      build and spec validation pass
