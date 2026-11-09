- [claim-unreproducible] `openspec/changes/bump-dependencies-2026-11-09/tasks.md:43-45` — task 2.5's Done note
  attributes the `@vitest/istanbul-lib-*` `1.0.2→2.1.0` pair to `vitest` `5.1.0`; the lockfile shows
  `@vitest/coverage-v8` 5.1.0 declares them and `vitest` declares neither.
- [claim-unreproducible] `openspec/changes/bump-dependencies-2026-11-09/design.md:69` — the Non-Goal names
  `@hookform/resolvers` `7.0.3`, while the live report and `npm view` read `7.0.5`.
- [claim-unreproducible] `openspec/changes/bump-dependencies-2026-11-09/tasks.md:75-78` — task 3.6's Done note says the
  120-character recipe flags only the record rows; the catalog file also carries 33 pre-existing lines over 120, none of
  them on a line the change touched.
- [incomplete-enumeration] `openspec/changes/bump-dependencies-2026-11-09/tasks.md:71-74` — task 3.5's sweep enumeration
  omits the `dependency-security` spec's `guava` `33.7.2-jre` minimum, which the same grep hits.
