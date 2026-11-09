- [stale-base] review-handover.md — `HEAD` `21b100bf` is behind `origin/main` `2b4c9a00`, so the diff against
  `origin/main` reads 30 files and 709 deletions of work the unit did not touch.
- [over-claim] design.md:99 — "the deployed gateway's healthcheck is the image's own probe" claims more than its source
  (`docker-compose.yml:68-76`); the Helm gateway's probes are `httpGet` on the actuator paths.
