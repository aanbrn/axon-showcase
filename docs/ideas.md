# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file
is a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a
change); only open, not-yet-implemented ideas remain.

## 2026-09-01

- Free managed-k8s staging — explored, parked (no change yet). Goal: a managed Kubernetes staging env for the
  chart. Findings: (1) the two real free-control-plane paths are AKS Free (control plane free, but you pay for nodes —
  not $0 ongoing) and OKE (control plane free + 2 Always Free ARM nodes = 12 GB, genuinely $0); (2) Oracle halved the
  Always Free Ampere A1 to 2 OCPU / 12 GB in June 2026, so the full stack (~11-12 GiB with the `kps` + `tempo`
  observability stack, 5 services, Kafka, OpenSearch, Postgres) does **not** fit OKE's free budget with observability
  enabled — it fits only if staging trims observability/single-replica/smaller OpenSearch, and images must be
  ARM (`-PimagePlatform=linux/arm64` already supported); (3) there is no free managed k8s that runs the full chart
  as-is, always-on, at $0 — every free path needs either trimming (OKE), paying for nodes (AKS, ~$40-80/mo), or
  accepting ephemerality. Reference: `nce/oci-free-cloud-k8s` runs OKE free but on a leaner stack. Open decision:
  how faithful staging must be to the observability stack (the real decider between OKE-free and AKS-paid), and
  whether always-on vs on-demand node-pool stop/start changes the budget.
- Web-based UI — build a beautiful web-based UI for the showcase so the CQRS/Event-Sourcing pipeline can be
  interacted with and demonstrated visually rather than only via the REST API / `curl`. Parked; no change yet. Open
  questions for later: single-page app served by the API gateway, what it interacts with (create/browse showcases,
  event timeline), and how it fits the existing read/write-side separation.
