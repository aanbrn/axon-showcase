# Proposal: Configure the IntelliJ test-class naming convention for CT and E2E suffixes

## Why

IntelliJ's `NewClassNamingConvention` inspection ("Class naming convention") checks test-class names against a
hardcoded default regex that recognizes `*Test(s|Case)`, `Test*`, `*IT`, and `*ITCase` — but not the repo's `CT` and
`E2E` suffixes. Creating test classes such as `ShowcaseApiGatewayE2E` or `ShowcaseAggregateCT` therefore triggers the
"Test class name doesn't match regex" warning. The repo already documents these suffixes as conventions, so the shared
inspection profile should recognize them.

## What Changes

- Add `.idea/inspectionProfiles/Project_Default.xml` configuring the `NewClassNamingConvention` inspection's
  `JUnitTestClassNamingConvention` extension with the regex extended to accept `CT` and `E2E` suffixes:
  `[A-Z][A-Za-z\d]*(Test(s|Case)?|CT|E2E)|Test[A-Z][A-Za-z\d]*|IT(.*)|(.*)IT(Case)?`.
- Un-ignore `.idea/inspectionProfiles/` in `.gitignore` so the profile is shared with all contributors.
- No production code, build, or test changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. An IDE-configuration change with no externally observable behavior change.

## Impact

- **Code (IDE config only)**: `.idea/inspectionProfiles/Project_Default.xml` (new); `.gitignore` (un-ignore rule).
- **Docs**: none expected.
- **Build**: unchanged.
- **Tests**: no test changes; the regex is verified to match the existing `*CT`/`*E2E` test class names and was
  confirmed to clear the naming warning in the live IDE.