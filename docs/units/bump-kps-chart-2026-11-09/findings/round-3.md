- [claim-missing] design.md:17 — the CRD fallback rests on a new mechanism fact (Helm installs a `crds/`-declared CRD
  only when the cluster has none and skips one that already exists) that the claims table records nowhere, and the row
  stating its consequence cites a source that cannot evidence it.
- [incomplete-enumeration] design.md:111 — the "subcharts move" row omits prometheus-windows-exporter `0.12.*` →
  `0.14.0`, the fourth moved dependency its cited `Chart.yaml` blocks carry; the list reads as complete.
- [incomplete-enumeration] proposal.md:40 — the same omission in the proposal's subchart summary.
