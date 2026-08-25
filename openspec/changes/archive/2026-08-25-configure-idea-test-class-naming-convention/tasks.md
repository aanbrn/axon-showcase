## 1. Add the shared inspection profile

- [x] 1.1 Create `.idea/inspectionProfiles/Project_Default.xml` configuring the `NewClassNamingConvention` inspection's
      `JUnitTestClassNamingConvention` extension with the `CT`/`E2E`-aware regex, and verify the XML is well-formed and
      matches the profile serialization format
- [x] 1.2 Add `.idea/inspectionProfiles/` un-ignore rules to `.gitignore`, and verify `git status` shows the profile
      as trackable

## 2. Verify the change

- [x] 2.1 Verify the extended regex matches the repo's `*CT`/`*E2E` test class names and run `openspec validate` on
      the change