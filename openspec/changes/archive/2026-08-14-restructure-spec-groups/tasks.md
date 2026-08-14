## 1. Pipeline stage groups

- [x] 1.1 `mkdir -p` the six group directories under `openspec/specs/showcase/`: `gateway`, `write-side`,
  `read-side`, `platform`, `deployment`, `quality`
- [x] 1.2 `git mv openspec/specs/showcase/api-gateway/spec.md openspec/specs/showcase/gateway/api-gateway/spec.md`
- [x] 1.3
  `git mv openspec/specs/showcase/command-service/spec.md openspec/specs/showcase/write-side/command-service/spec.md`
- [x] 1.4
  `git mv openspec/specs/showcase/projection-service/spec.md openspec/specs/showcase/read-side/projection-service/spec.md`
- [x] 1.5 `git mv openspec/specs/showcase/query-service/spec.md openspec/specs/showcase/read-side/query-service/spec.md`

## 2. Platform, deployment, and quality groups

- [x] 2.1 `git mv` the three extension specs under `openspec/specs/showcase/platform/`:
  `identifier-extension/spec.md`, `mapstruct-extension/spec.md`, `resilience4j-extension/spec.md`
- [x] 2.2 `git mv openspec/specs/showcase/helm-chart/spec.md openspec/specs/showcase/deployment/helm-chart/spec.md`
- [x] 2.3 `git mv openspec/specs/showcase/load-tests/spec.md openspec/specs/showcase/quality/load-tests/spec.md`
- [x] 2.4 Remove the now-empty old directories under `openspec/specs/showcase/`

## 3. Verification

- [x] 3.1 Confirm the new tree layout with `find openspec/specs -name spec.md | sort`
- [x] 3.2 Run `openspec validate --specs` and confirm all specs still pass (0 failed)
- [x] 3.3 Confirm `git status` shows only renames (R) with no content modifications to any spec
