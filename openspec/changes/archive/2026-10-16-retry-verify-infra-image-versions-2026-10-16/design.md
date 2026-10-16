# Design

## Context

See `proposal.md` — Why. The state the fix answers was deliberate on its own terms and is worth stating, because the
obvious lever is the wrong one:

- `VerifyInfraImageVersionsTask` calls `addHelmRepositories(repos)` with `tolerateFailures` at its default of `false`,
  while `HelmUpdatesTask` passes `true`. Both are correct: the report exists to _report_ an unreachable repository as
  no-data, and the check exists to _fail_ on drift — so tolerating here would turn a real failure into a false pass. The
  gap was never toleration; it was that a **transient** failure was treated as a permanent one.
- The task is `@CacheableTask` on the pinned coordinates and values files, which is why the failure only bites on the
  cold path: unchanged inputs restore the verification without touching the network, and a coordinate or values change
  re-runs the resolution over it.
- The repository already chose the remedy for the same class in its CI twin: #463 (`69d26b02`) wrapped the actionlint
  install in three bounded attempts with a short pause, leaving the error visible. This change does the same at the one
  point the Helm network call happens.

## Goals / Non-Goals

**Goals:**

- Stop a transient repository failure from failing the drift check, without weakening it: a repository that stays
  unreachable still fails, after the bounded attempts.
- Keep the retry in one place for both callers, and testable without a Gradle build or a network.

**Non-Goals:**

- Toleration: an unreachable repository must still fail the drift check (that is the difference from `HelmUpdatesTask`).
- Retrying the chart resolution (`helm show values`) or the Helm client download: the recorded failure, and the network
  write, are the repository add/update — the resolution reads what the update refreshed.
- A configurable attempt count: the bound is a constant, as it is in the CI twin.

## Decisions

**A pure `BoundedRetry` object, not a retry inside the task.** `build-logic` has no `gradleTestKit()`, so a plugin's
Gradle DSL cannot be unit-tested there; its `src/test` holds tests of plain helper objects. A pure helper therefore
carries the logic, is unit-testable, and — being a class reference rather than script state — stays configuration-cache
serializable inside a task action.

**Retry the add/update pair per repository, the last attempt's failure propagating.** That is the granularity the
failure has (the recorded message names the repository), and it keeps the exhausted case's error the real one rather
than a wrapper's.

**Report each transient failure and keep going.** The CI twin's principle is that the error stays visible; here that is
the warn line naming the repository, so a retry that eventually succeeds is still observable in the log.

## Risks / Trade-offs

- **The retry hides a real outage** → the bound is three attempts with a two-second pause (~4s worst case per
  repository), and a repository that stays unreachable still fails the check — verified live by pointing a repository at
  an unreachable host: two retry warnings, then `verifyInfraImageVersions FAILED`.
- **The retry weakens the gate** → the evidence is the exhausted-case test plus that live run; `tolerateFailures` is
  unchanged and stays `false` for the check.
- **A helper nobody calls** → the two callers are the task types the shared base class serves, and the base class
  changed in the same unit.

## Migration Plan

None: the retry takes effect on the next cold resolution; cached results are unaffected.

## Claims table

| Claim                                                                   | Source                                                                                                                                                                                                                                                                                                                      |
| ----------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The setup retried nothing, and a transient failure failed the check     | `build-logic/src/main/kotlin/AbstractHelmRepositoriesTask.kt` before this change (a single `add`/`update` pair per repository, no retry), and the parked entry's record at its pre-change state (`git show HEAD:docs/ideas.md`, the 2026-10-15 entry: the failing repository differing between runs, the task passing warm) |
| `tolerateFailures` is `false` for the check and `true` for the report   | the two call sites: `VerifyInfraImageVersionsTask`'s `addHelmRepositories(repos.get())` and `HelmUpdatesTask`'s `addHelmRepositories(repoUrls.get(), tolerateFailures = true)`                                                                                                                                              |
| The task is cacheable on its inputs, so the failure bites the cold path | `VerifyInfraImageVersionsTask`'s annotations, and the `infra-image-versions` requirement's "Unchanged inputs skip the Helm resolution" scenario                                                                                                                                                                             |
| The CI twin already shipped this remedy                                 | `69d26b02` (#463), whose `ci.yml` step carries three bounded attempts with a short pause                                                                                                                                                                                                                                    |
| The unit test is evidence (the retry is what it verifies)               | neutering `BoundedRetry` (`repeat(attempts - 1)` → `repeat(0)`) fails 2 of its 4 cases and restoring passes all 4                                                                                                                                                                                                           |
| A persistent failure still fails the check                              | the live control: a repository pointed at an unreachable host gives 2 retry warnings and then `verifyInfraImageVersions FAILED` (exit 1)                                                                                                                                                                                    |
| The actionlint entry was stale                                          | `69d26b02` (#463) implements the retry its 2026-09-21 entry still described as parked                                                                                                                                                                                                                                       |
