# Tasks — restructure the spec corpus per the audit findings

## 1. The seven header renames (delete-and-add)

- [x] 1.1 `showcase/clients/web-ui` — six requirement headers retitled to noun phrases, each as a `REMOVED` + `ADDED`
      pair carrying the full body and every scenario (a `MODIFIED` block cannot rename a header): `Browse showcases` →
      `Showcase browsing`; `Reconcile the list with the eventually-consistent read model` →
      `Read-model reconciliation of the showcase list`; `Drive lifecycle actions` → `Lifecycle action dispatch`;
      `Validate the form before submission` → `Create-form validation`; `Render a per-showcase history timeline` →
      `Per-showcase history timeline`; `Display live events over SSE` → `Live event display over SSE`.
- [x] 1.2 `showcase/gateway/live-events` — `Stream domain events over SSE` → `Domain event streaming over SSE`, same
      route.

## 2. The three requirement merges in `showcase/deployment/helm-chart`

- [x] 2.1 Fold `Horizontal autoscaling details` and `Vertical autoscaling details` into
      `Horizontal and vertical autoscaling` (2 core scenarios + 2 + 1 details scenarios = 5), as a `MODIFIED` carrying
      every scenario.
- [x] 2.2 Fold `Pod disruption budget details` into `Pod disruption budgets` (1 + 1 = 2 scenarios), same route.
- [x] 2.3 Add the matching `REMOVED` blocks for the three details requirements — without them a `MODIFIED` would leave
      each subject with both the merged core requirement and its still-present details requirement.

## 3. The two misplaced Helm requirements (a move)

- [x] 3.1 `showcase/quality/merge-governance` — `REMOVED` blocks for `Helm release namespaces are declared in the build`
      (2 scenarios) and `Each Helm release target declares its kube context` (3 scenarios), each naming the reason and
      the migration.
- [x] 3.2 `showcase/deployment/helm-chart` — the matching `ADDED` blocks, carrying both requirements verbatim.

## 4. Verification

- [x] 4.1 Every delta path is under `specs/showcase/…`, matching the archived convention — an earlier layout put two of
      them at `specs/clients/…` and `specs/gateway/…`, which the CLI reported as different capability ids, so the move
      would have targeted the wrong spec.
- [x] 4.2 `openspec show … --deltas-only` reports four spec ids and 23 deltas, all under `showcase/`.
- [x] 4.3 Scenario counts preserved: the renames carry every scenario of the headers they replace (verified per header);
      the merges carry the core's plus the details' (5 and 2).
- [x] 4.4 `./gradlew spotlessCheck` green; no line over 120 in the touched files.
- [x] 4.5 `openspec validate --all` 23/23 with the deltas accepted.
- [ ] 4.6 Refresh `helm-chart`'s `## Purpose` in the archive commit (it gains the two release-target requirements and
      absorbs the details requirements); a delta cannot carry a Purpose. `merge-governance`'s Purpose is **unchanged** —
      the two removed requirements were never described by it, so verify rather than rewrite.
