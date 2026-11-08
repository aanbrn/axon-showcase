- [claim-unreproducible] pr-body:claims-row-2 — the row claimed both assets are version-pinned and checksummed;
  actionlint passes `latest` to an installer with no checksum
- [premise] docs/ideas.md:44-45 — "keeping the pinned version and checksum" cannot hold for actionlint, which has
  neither
- [contradiction] docs/units/park-the-download-retry-idea/unit.md:20 — "Seven re-runs" contradicted the run's
  `run_attempt: 10` in the same sentence
- [claim-unreproducible] docs/ideas.md:41-42 — the 504 and 92-byte body were attributed to both steps' logs; the runner
  log shows the 92-byte non-gzip body for actionlint only, and the 504 was observed by the caller's curl
- [claim-missing] docs/ideas.md:42 — the duration and the intermittent 200s had no claims row, and "over half an hour"
  overstated the 08:53–09:23Z span
- [wording] docs/ideas.md:42-43 — "all systems operational" carried no time scope, though a later incident that morning
  postdates the failures
- [claim-unreproducible] pr-body:14 — `./gradlew check` was ticked while the completed-checks list named no such run
