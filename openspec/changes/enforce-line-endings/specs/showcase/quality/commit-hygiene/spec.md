## ADDED Requirements

### Requirement: Line endings are normalized by a committed `.gitattributes`

The repository SHALL commit a `.gitattributes` that stores every `text`-detected file as LF in the index
(`* text=auto eol=lf`) and gives the Windows batch file an `eol=crlf` checkout (`*.bat text eol=crlf`), so a file's line
endings in the index are repo-declared and enforced by git at `add`/`checkout` rather than depending on a contributor's
`core.autocrlf`. `eol=crlf` governs the working-tree checkout only; a batch file is still stored as LF in the index,
like every other text file, and `text=auto` leaves a detected binary unnormalized.

#### Scenario: A text file is stored as LF regardless of its working-tree endings

- **WHEN** a text file with CRLF line endings is added to the index
- **THEN** git normalizes it to LF in the index, per the committed `.gitattributes`

#### Scenario: The Windows batch file is checked out as CRLF

- **WHEN** the repository's `gradlew.bat` is checked out
- **THEN** the committed `.gitattributes` gives it `eol=crlf`, so the working-tree file is CRLF, while its index blob is
  LF like every other text file

#### Scenario: A binary file is left unnormalized

- **WHEN** a tracked binary file (for example the Gradle wrapper JAR) is added
- **THEN** git detects it as binary and leaves it unnormalized, so its bytes are unchanged
