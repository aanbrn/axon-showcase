## 1. Add the ideas notes file

- [x] 1.1 Create `docs/ideas.md` with a short intro and a dated `## YYYY-MM-DD` section containing the first entry
      (the Spring Data calendar-versioning gap in the `majorOf()` heuristic), wrapped at 120 characters

## 2. Add the /ideas command

- [x] 2.1 Create `.opencode/commands/ideas.md`: a command that appends a dated bullet to `docs/ideas.md` from the given
      text, and prints the current contents when called without a note; verify the command reads coherently and the
      frontmatter matches the other commands

## 3. Verify the change artifacts

- [x] 3.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors