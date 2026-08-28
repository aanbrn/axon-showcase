# Tasks

## 1. Filter the issue body to actionable sections

- [x] 1.1 Update the issue-body step in `.github/workflows/dependency-updates.yml` to extract the "dependencies have
      newer versions" section and the "Gradle CURRENT updates" section from `build/dependencyUpdates/report.txt`,
      dropping the non-actionable sections ("using the latest milestone version", "have later milestone versions"),
      and emit "No stable catalog updates available" when the stable-updates section is absent
- [x] 1.2 Verify the step still prefixes `cc @<owner>` and still opens or updates the same in-place issue, and that
      the YAML validates (YAML 1.2)

## 2. Verify the workflow

- [x] 2.1 Run the workflow once via `workflow_dispatch` on the merged branch and confirm the issue body contains only
      the actionable sections (or the explicit no-updates line), not the raw noise sections
- [x] 2.2 Run `openspec validate filter-dependency-updates-issue` and confirm the change is valid with all artifacts
      consistent

## 3. Docs refresh

- [x] 3.1 Update `AGENTS.md` and `README.md` only if the workflow description changes meaningfully (the filtered
      issue body), and verify the edited files respect the 120-character line limit