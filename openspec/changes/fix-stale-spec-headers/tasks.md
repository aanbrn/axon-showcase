## 1. Fix the stale headers

- [x] 1.1 Update `showcase/deployment/helm-chart/spec.md` header to `# showcase/deployment/helm-chart Specification`.
- [x] 1.2 Update the three `showcase/extensions/*` specs' headers to `# showcase/extensions/<capability> Specification`.
- [x] 1.3 Update `showcase/quality/load-tests/spec.md` header to `# showcase/quality/load-tests Specification`.
- [x] 1.4 Update the two `showcase/read-side/*` specs' headers to `# showcase/read-side/<capability> Specification`.
- [x] 1.5 Update `showcase/write-side/command-service/spec.md` header to
      `# showcase/write-side/command-service Specification`.

## 2. Verify

- [x] 2.1 Confirm every `openspec/specs/showcase/*/*/spec.md` header matches its path (no stale
      `# showcase/<capability>` headers remain; the title-case `ide-config`/`infra-image-versions` style is out of
      scope).
- [x] 2.2 Run `openspec validate --all` and `openspec validate --changes`.
- [x] 2.3 Run `./gradlew spotlessApply` / `spotlessCheck` (change docs are in the markdown target).
