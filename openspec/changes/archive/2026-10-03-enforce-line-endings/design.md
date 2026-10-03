# Design

## Context

See `proposal.md` — Why. Verified against the repository and git:

- **No `.gitattributes` exists**, and `core.autocrlf`/`core.eol` are unset, so normalization is per-machine.
- **The tree is already LF in the index**: `git ls-files --eol` reports 1774 files `i/lf`, one `i/crlf` (`gradlew.bat`),
  one `i/-text` (`gradle/wrapper/gradle-wrapper.jar`), and one `i/none` (`showcase-web-ui/Procfile`). So the policy's
  byte effect reaches only `gradlew.bat`.
- **`gradlew.bat` is CRLF by design in the working tree**; adding the policy re-stores its **index** blob as LF
  (`eol=crlf` governs checkout, not the stored blob), leaving the working checkout CRLF. This is git's standard
  normalization, and the one file the change's bytes reach.
- **A `.gitattributes` is enforced by git, not the build** (reproduced in a scratch clone): a CRLF text file added under
  `* text=auto eol=lf` makes git warn "CRLF will be replaced by LF" and stores the blob as LF (`git ls-files --eol` →
  `i/lf w/crlf`).
- **Renormalization is a one-file change**: with the attributes applied, `git add --renormalize .` alters only
  `gradlew.bat` (index `i/crlf` → `i/lf`, working `w/crlf`), not the 1774 already-LF files.

## Goals / Non-Goals

**Goals:**

- Commit a repository-declared line-ending policy so a text file is stored as LF in the index regardless of a
  contributor's `core.autocrlf`, and the Windows batch file is checked out as CRLF.
- Keep the change's byte effect to the one file that normalization legitimately touches (`gradlew.bat`).

**Non-Goals:**

- Enforcing line endings from the Gradle build — git already enforces a `.gitattributes` at `add`/`checkout`, and a
  build check reading every tracked file's endings would duplicate it.
- Changing Spotless or the `.editorconfig` — they are orthogonal (Spotless formats owned files; `.editorconfig` is the
  IDE hint). The `.gitattributes` is the git-level policy.
- Adding per-language `eol` rules beyond text-default-LF and the batch checkout exception — none is needed.

## Decisions

### D1: `* text=auto eol=lf` with a `*.bat text eol=crlf` exception

`eol=lf` makes every `text`-marked file LF in the index; `text=auto` marks every path `text: auto` and detection happens
at add-time (the wrapper jar is added unnormalized as binary). The `*.bat` line gives the one Windows batch file
`eol=crlf`, which governs its **checkout** — its stored blob is still LF, like every other text file.

- **Alternative — `* text=auto` alone (no `eol`):** leaves the working-tree ending to `core.autocrlf`, which is the
  per-machine dependence this change removes.
- **Alternative — `* text eol=lf` (forcing text):** would treat the wrapper jar as text; `text=auto` keeps detection.
- **Alternative — per-extension rules (`.java eol=lf`, etc.):** enumerates what `*` already covers and drifts as file
  types are added.

### D2: Renormalize `gradlew.bat` in this change

Applying the attributes re-stores `gradlew.bat`'s index blob from CRLF to LF (its working checkout stays CRLF via the
`eol=crlf` rule). Running `git add --renormalize gradlew.bat` in this change lands that consistently rather than leaving
it flagged; no other file changes.

- **Alternative — leave the tree and only add the file:** a contributor's next `git add` would show `gradlew.bat` as
  modified, an unexplained churn the change can absorb once.

### D3: The owning requirement is a `commit-hygiene` ADDED requirement

The `.gitattributes` is a committed repository file that shapes the tracked set's line endings — the same family as the
capability's tracked-set checks. The requirement states it is enforced by git (at `add`/`checkout`), not by a build
gate, so the spec does not imply a `check` member that does not exist.

- **Alternative — a `code-quality` requirement:** that capability's requirements are "enforced by the build" (Spotless,
  actionlint, checkstyle); a git-enforced policy does not fit its shape.

## Risks / Trade-offs

- [A Windows contributor's working tree shows a one-time `gradlew.bat` re-normalization] → absorbed here by the
  renormalize; afterwards the rule is stable.
- [A future Windows-only text file needs CRLF] → add a path-specific `eol=crlf` line then; the `*.bat` exception is the
  pattern.
- [The policy silently does not apply because a clone predates it] → git reads `.gitattributes` from the working tree
  and the index; a clone that has the file applies it, which is the intent.

## Migration Plan

The `.gitattributes` takes effect on the next `git add`; this change renormalizes `gradlew.bat` so no file is left
flagged. Rollback is deleting the file.

## Open Questions

None.
