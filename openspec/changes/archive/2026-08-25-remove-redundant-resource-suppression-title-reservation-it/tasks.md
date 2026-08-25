## 1. Remove the redundant suppression

- [x] 1.1 Change `@SuppressWarnings({"resource", "unused"})` to `@SuppressWarnings("unused")` on the `dbEvents`
      container field in `ShowcaseTitleReservationIT`, and verify `compileIntegrationTestJava` passes and the IDE
      reports no `AutoCloseableResource` warning

## 2. Verify the change artifacts

- [x] 2.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors