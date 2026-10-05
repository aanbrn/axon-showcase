# Design

## Context

See `proposal.md` — Why. The projector (`ShowcaseProjector.processEvents`) maps a batch of showcase events to
`BulkOperation`s and executes `BulkRequest.of(request -> request.operations(operations))` with no `refresh` parameter,
so the writes become searchable at the next scheduled index refresh (default `refresh_interval: 1s`). The web UI's
reconciliation (`showcase/clients/web-ui`) debounces events and then refetches the list until the effect is visible.

Refresh semantics that shape the decision:

| Policy            | When the write is searchable             | Extra cost                     |
| ----------------- | ---------------------------------------- | ------------------------------ |
| none (`False`)    | next scheduled refresh (≤ 1 s)           | none                           |
| `Refresh.WaitFor` | next scheduled refresh (same as `False`) | the bulk blocks until it lands |
| `Refresh.True`    | as soon as the bulk returns              | a refresh per batch            |
| shorter interval  | ≤ the interval after the write           | a fixed, higher refresh rate   |

## Goals / Non-Goals

**Goals:**

- A projected write is searchable when the projector's bulk completes, so the UI's first refetch after an event can see
  it.
- Measure the write-side cost of doing so.

**Non-Goals:**

- Changing the UI's reconciliation (it still refetches; this only removes the reason it must wait).
- Making the refresh policy configurable (a constant for now; a property can follow if a deployment needs it).
- Changing the index `refresh_interval` (the considered alternative, below).

## Decisions

**D1 — `Refresh.True`, not `Refresh.WaitFor`.** Only `Refresh.True` makes a write visible earlier: `WaitFor` returns at
the same next-refresh moment as the current default, so it would add projector latency (the ack waits) without reducing
the UI's staleness. _Alternatives:_ no refresh (status quo — the window the UI polls through); `WaitFor` (rejected —
does not change visibility); a shorter index `refresh_interval` (rejected for now — bounded refresh rate but still
interval-bound, so it does not guarantee the first refetch hits; it is the fallback if the per-batch refresh proves too
costly).

**D2 — Hardcode the policy on the bulk, not a new property.** The projector's other knobs (`batch`, `retry`, `restart`,
concurrency) are configurable and surfaced in yml and the chart, but this app has one read-your-writes consumer and a
low write volume, so a constant keeps the change small and puts the decision in the code where the trade-off is
documented. _Alternative:_ a `showcase.projector.refresh-policy` property (Java + yml + chart + tests per ADR-0002) —
deferred.

**D3 — Make the bulk request construction testable.** Extract the `BulkRequest` construction from `processEvents` into a
package-private method so a unit test can assert its refresh policy without a real OpenSearch; the existing integration
suite stays as-is (it uses a realtime `get`, which is unaffected by refresh).

## Risks / Trade-offs

- **A refresh per batch is the expensive operation at scale** → this is the trade the change makes deliberately; the
  load-test re-measure quantifies it, and a regression falls back to a shorter `refresh_interval` (bounded refresh rate)
  with no projector change. If the measured cost is unacceptable, the change does not ship as-is.
- **Refreshes raise the projector's own lag** → worth watching the existing `showcaseProjector.projectionLag` metric
  during the measurement; a rising lag is the signal that the batch-refresh is the bottleneck.
- **The unit test pins the policy, not the behavior** → the IT cannot assert search visibility deterministically (it
  reads via realtime `get`), so the unit assertion is the guard and the load test carries the behavioral acceptance.

## Migration Plan

None — a projector-only code change with no persisted state or contract change. Rollback is the code revert.

## Open Questions

None. The one genuinely open point (the measured write cost) is the change's acceptance criterion, not a hidden
assumption baked into the tasks.
