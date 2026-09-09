## 1. Rename the capability

- [x] 1.1 `git mv openspec/specs/showcase/gateway/api-gateway openspec/specs/showcase/gateway/rest-api`.
- [x] 1.2 Update the spec's `#` header to `# showcase/gateway/rest-api Specification` (was
      `# showcase/api-gateway     Specification`); no other content changes.
- [x] 1.3 Confirm no other file references the old capability path `showcase/gateway/api-gateway` (grep the repo,
      excluding the change's own artifacts and git history).

## 2. Verify

- [x] 2.1 Run `openspec validate --all` and `openspec validate --changes`.
- [x] 2.2 Run `./gradlew spotlessApply` / `spotlessCheck` (change docs are in the markdown target).
- [x] 2.3 Confirm the module/deployment naming (`showcase-api-gateway`, chart references) is untouched.
