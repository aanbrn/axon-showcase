# Tasks

## 1. Apply the deterministic index lifecycle

- [x] 1.1 In `showcase-query-service/src/integrationTest/java/showcase/query/ShowcaseQueryControllerIT.java`, add
      `properties = "showcase.query.index-initialization-enabled=false"` to `@SpringBootTest`, and set `@BeforeEach` to
      assert `exists() == false`, `createWithMapping() == true`, then `exists() == true`; verify it compiles
- [x] 1.2 In the same class, change `@AfterEach` to assert `exists() == true`, `delete() == true`, then
      `exists() == false` (dropping the old delete+recreate), and verify the suite passes locally
      (`:showcase-query-service:integrationTest`)
- [x] 1.3 In `showcase-projection-service/src/integrationTest/java/showcase/projection/ShowcaseProjectorIT.java`, set
      `@BeforeEach` to assert `exists() == false` before `createWithMapping() == true` and assert `exists() == true`
      after; set `@AfterEach` to assert `exists() == true` before `delete() == true` and assert `exists() == false`
      after; verify the suite passes locally (`:showcase-projection-service:integrationTest`)

## 2. Verify in CI

- [x] 2.1 Push both IT changes on a branch with the temporary spike workflow and confirm both suites pass on GitHub
      Actions (Linux + OpenSearch `:3`), then remove the temporary workflow
- [x] 2.2 Run `openspec validate opensearch-it-index-lifecycle` and confirm the change is valid with all artifacts
      consistent