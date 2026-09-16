## 1. Record the delta

- [x] 1.1 Add the delta spec for `showcase/quality/code-quality` carrying the new requirement (the module dependency
      graph is enforced by the build) with its three scenarios, as an ADDED block — no existing requirement in that
      capability covers module boundaries
- [x] 1.2 Confirm no existing requirement in the capability already covers it, and that the requirement header does not
      collide with an existing one

## 2. Implement

- [x] 2.1 Add the pure rule logic to `build-logic` — the explicit module classification and the edge validation — so the
      rules are testable without a Gradle runtime
- [x] 2.2 Add the task that adapts it: a `@CacheableTask` taking the edge list as an `@Input` and writing a report file,
      mirroring `VerifyInfraImageVersionsTask`
- [x] 2.3 Add `build-logic`'s first unit tests — each forbidden class, the self-edge the rules drop, an unclassified
      target, and a sanctioned contract-to-contract edge; the walk's own filters are covered by the positive control
      rather than by the pure object, which cannot see configurations
- [x] 2.4 Gather each subproject's production source sets' project edges at the root into the task's inputs — `main` and
      `testFixtures`, each by name — excluding the `platform` BOM by its declared target, and wire the task into `check`
      beside `verifyInfraImageVersions` and `workflowLint`
- [x] 2.5 Declare the test dependency in `build-logic/build.gradle.kts` and make the root `check` depend on the included
      build's test task — `build-logic` is an `includeBuild`, so its tests run in no gate otherwise

## 3. Record the decision and sweep the docs

- [x] 3.1 Add the ADR recording the sanctioned graph, the forbidden-edge classes, the walk's exclusions, and the
      `query-api` → `command-api` decision with its parked narrowing
- [x] 3.2 Remove the implemented idea from `docs/ideas.md` (the ArchUnit fitness-functions entry, promoted as #262) and
      sweep the enumeration that still lists it as a candidate; correct the parked `query-api` idea there, whose premise
      ("a direction the architecture does not sanction") the ADR decides the other way; sweep the retrospective's own
      enumeration, or state in the report why that dated record is left as recorded; refresh `AGENTS.md` where it
      describes the module graph and fix any "rests on convention / nothing enforces" claim this makes false
- [ ] 3.3 Refresh the capability's `## Purpose` in the archive commit — the new requirement widens `code-quality`'s
      scope from style conventions to the module graph — and record the finding as the change's own task
- [x] 3.4 Decide the README's gate list and the `architecture-auditor`'s boundary remit explicitly: the auditor already
      must not re-check what a gate enforces, and the README lists the build's gates — record the decision rather than
      leaving it implicit
- [x] 3.5 Check `openspec/config.yaml`'s context block and the README for a fact this moves (module count, gate list),
      and extend `AGENTS.md`'s own `check` composition enumeration — it names `workflowLint` and
      `verifyInfraImageVersions`, which this task makes incomplete — along with the prose that repeats it

## 4. Verify

- [x] 4.1 Positive control: temporarily add a forbidden edge (a module depending on a service application), confirm
      `check` fails naming it, then revert — a check is evidence only once it has been shown to fail, and the walk's
      exclusions must not be what makes the tree pass
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green with the task wired in, and the included
      build's tests observed running; `openspec validate --all`; `spotlessApply` and `spotlessCheck`
- [x] 4.3 `review-quick` clean over proposal and implementation
