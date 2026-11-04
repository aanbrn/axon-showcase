- [over-claim] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:18 — the dashboard description
  claims every panel states what an empty result means, while only ten did.
- [coherence] openspec/changes/repair-dashboard-panels-and-descriptions/design.md:60-61 — decision 5 says the ratio is
  rebuilt from the list counter alone, while the code unions both fetch families.
- [claim-unreproducible] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:3253 — Event Age's
  description says the age is read when the projector wrote the event; the metric is recorded at ingest.
- [claim-unreproducible] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:452 — Memory Usage
  describes per-pod fractions while the expression sums each service's pods.
- [claim-unreproducible] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:172 — Processing Ratio
  calls the command bus's counters event counters; they count commands.
- [claim-unreproducible] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:1591 — the bus failure
  panel claims handler business rejections are excluded, which no wiring does.
- [claim-unreproducible] openspec/changes/repair-dashboard-panels-and-descriptions/tasks.md:88-90 — task 5.3's verify
  requires a one-sentence description while the field carries two.
