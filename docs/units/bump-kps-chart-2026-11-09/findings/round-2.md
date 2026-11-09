- [over-claim] tasks.md:33 — the CRD fallback assumes an absent release means absent CRDs: Helm skips a `crds/`-declared
  CRD that already exists and never upgrades it, so a fresh install leaves the old schemas in place.
- [claim-unreproducible] design.md:62 — "kube-state-metrics and grafana cross majors" does not reproduce: grafana moves
  14.3.0 → 14.4.0, a minor, and design.md:24's "across majors" lead-in covers grafana and windows-exporter too.
- [claim-missing] design.md:69 — the schema-less silent-merge fact the corrected render premise rests on has no claims
  row.
- [structured-writing] tasks.md:14 — task 1.4 joins the passing run and the known-bad control, whose expected outcomes
  are opposite.
