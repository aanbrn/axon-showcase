## 1. Report the Gradle version in /dependency-updates

- [x] 1.1 Extend `.opencode/commands/dependency-updates.md` so the agent also surfaces the `Gradle CURRENT updates`
      section from the report — stating the current/pinned wrapper version and whether a newer Gradle release is
      available, flagging it for attention when one exists, and pointing at `/gradle-update` when warranted; verify the
      instruction reads coherently with the existing dependency-row guidance

## 2. Add the /gradle-update command

- [x] 2.1 Create `.opencode/commands/gradle-update.md`: a command that reads the Gradle version reported by
      `dependencyUpdates`, and when a newer stable Gradle is available runs `./gradlew wrapper --gradle-version=<latest>`,
      verifies the build still passes, and reports the change; when already current, it reports that no update is
      needed
- [x] 2.2 Verify the command runs end-to-end against the current state (Gradle 9.7.1, current) and reports "no update
      needed" without mutating the wrapper

## 3. Refresh docs

- [x] 3.1 Add `/gradle-update` to the Build & Test sections of `AGENTS.md` and `README.md` alongside
      `/dependency-updates`

## 4. Verify the change artifacts

- [x] 4.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors