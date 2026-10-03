# Proposal

## Why

The repository has no `.gitattributes`, so line-ending normalization depends entirely on `core.autocrlf` — a per-machine
git setting, unset here — rather than on a committed policy. Spotless normalizes only the files it owns (Java, Kotlin
DSL, build-logic Kotlin, and markdown), leaving every other tracked text file's endings to chance: a contributor on
Windows with `core.autocrlf=true` can commit CRLF endings into a file Spotless does not own, and the diff then shows
whitespace churn (or the file's bytes differ across checkouts) without a committed rule to say which is correct.

## What Changes

- Add `.gitattributes` declaring `* text=auto eol=lf`, with `*.bat text eol=crlf` for the Windows batch file — so every
  `text`-detected file is stored as LF in the index, and a `.bat` is checked out as CRLF in the working tree (`eol=crlf`
  governs checkout, not the stored blob).
- The tracked index is already LF (1774 of 1777 files); the policy re-stores `gradlew.bat`, whose index blob is CRLF
  today, as **LF** (the working checkout keeps CRLF). That is the one file the policy's byte effect reaches — a
  normalization of its stored representation, not a content change.
- Refresh the docs: `AGENTS.md`'s Formatting/line-ending prose (it currently names only Spotless) and the
  `.gitattributes` convention; remove the implemented idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: adds a requirement that the repository commits a `.gitattributes` normalizing line
  endings, so a text file's endings are repo-declared and enforced by git rather than per-machine — the capability owns
  the committed-file hygiene that shapes the tracked set (the `.gitattributes` file is a tracked repository file, like
  the hook `commit-hygiene` already covers). (Its sibling tracked-file properties — markers, conflict markers,
  executable bits, size — are verified by the build; this one is enforced by git at `add`/`checkout`, which the
  requirement states.)

## Impact

- **Files**: a new `.gitattributes`; renormalized `gradlew.bat`; `AGENTS.md`, `README.md` if either describes
  line-ending handling; `docs/ideas.md` (idea removal); plus the change dir and the `commit-hygiene` delta.
- **Build / tests / services**: no code change; no new `check` member — `.gitattributes` is read by git, and its effect
  (a CRLF text file committed as LF) is what the verification exercises.
- **Verification**: proven by `git check-attr` on representative paths (a `.java` → `eol=lf`, `gradlew.bat` →
  `eol=crlf`, the wrapper jar → treated binary), by committing a CRLF text file in a scratch clone and confirming the
  stored blob is LF, and by confirming a `git add --renormalize` alters only `gradlew.bat`.
