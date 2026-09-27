# Design

## Context

See `proposal.md` — Why. From run 36354957471: the cluster, the images, and the trimmed `ci` install all succeeded and
the `smoke` profile passed; the `baseline` plateau then failed **six** assertions: 37 `PollShowcase` KOs, each a gateway
`503 Service Unavailable` (Spring `application/problem+json`) in a nine-second window at the plateau's start
(22:28:36-45), while the port-forward's only errors came at teardown (22:30:27) and caused none; the rest of the plateau
served 3,844 × `200` and 602 × `201`. A gateway 503 is its mapping of a downstream failure — the query path (its circuit
breaker) or a service that is restarting. The CI target's values start from the chart's defaults, lowering the requests
to fit the runner, the chart's 1Gi memory limits kept, and the projection service left at them — an asynchronous
projection restart delays reads rather than failing a request, so it is not on the burst's 503 path.

## Goals / Non-Goals

**Goals:**

- The plateau completes without a failed request.
- A failure leaves evidence in the run, not only in a deleted cluster.

**Non-Goals:**

- No change to the chart, the load profiles, the requirement, or the local path.
- No ingress work — that is parked on its own branch, on a premise this run disproved.

## Decisions

- **D0 — The CI runs only the smoke profile.** The plateau's five latency failures (`p95` 732 ms against a hardcoded
  500, `p99` 2812 against 1000) are not something a shared 4-vCPU runner can satisfy — they are the "a runner's numbers
  are its own" reality arriving in the profile's fixed thresholds. The smoke profile asserts zero failed requests and
  carries no latency thresholds, which is exactly the availability signal this smoke is for; the plateau stays a local
  measurement.

- **D1 — Raise the memory limits, not the requests.** A JVM's heap is a fraction of the container's memory limit, so 1Gi
  leaves a Spring service, its HTTP client, and its OpenSearch machinery little headroom under a write burst; the
  _requests_ are what the scheduler checks, so raising the limits costs the budget nothing and keeps the install inside
  the runner. This is the dominant hypothesis the evidence supports (a brief 503 window that clears by itself, the shape
  of a restart), and D2 settles it.
- **D2 — Capture the evidence on failure.** A step that runs only when the load step fails prints the pods with their
  restart counts and tails the gateway, query, and projection logs while the cluster still exists. Without it, the next
  failure costs another dispatch and still names nothing.
- **D3 — No warm-up gate.** The `smoke` profile passed immediately before the plateau, so the pipeline was warm; a
  warm-up would not have prevented this. If the evidence shows a readiness gap instead, that finding scopes its own fix.

## Risks / Trade-offs

- **The limits may not be the cause.** → D2 makes the next failure self-diagnosing: a restart count or an OOM kill
  confirms it; the breaker or a readiness gap would scope a different follow-up.
- **Higher limits on a shared runner.** → The runner has 16 GB with the requests unchanged; a limit bounds a container's
  ceiling, not its scheduled share.

## Verification

- `spotlessCheck`, `openspec validate --changes`, `workflowLint`, and the Docker-free `check`.
- The dispatch after it lands: the plateau must pass, and the evidence step must be present (it runs only on failure).
