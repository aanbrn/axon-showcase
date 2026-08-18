## 1. Remove redundant @DirtiesContext

- [x] 1.1 Remove `@DirtiesContext` (and its now-unused import) from `ShowcaseProjectorIT`, verified to pass without it
- [x] 1.2 Remove `@DirtiesContext` from `ShowcaseQueryClientIT` (top-level and the three `@ActiveProfiles` nested
      classes), verified to pass without it
- [x] 1.3 Remove `@DirtiesContext` from `ShowcaseCommandClientCT` `Retry` nested class, verified to pass without it
- [x] 1.4 Keep `@DirtiesContext` on the six full-context ITs that boot JGroups/JCache
