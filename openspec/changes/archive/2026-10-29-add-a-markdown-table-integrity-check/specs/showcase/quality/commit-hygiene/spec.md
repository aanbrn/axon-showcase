## ADDED Requirements

### Requirement: A markdown table's rows carry the delimiter row's column count

The standard `check` task SHALL verify the tables in the markdown the Spotless format owns. A table SHALL be a row
carrying an unescaped pipe whose next line is a delimiter row that itself carries an unescaped pipe, running until a
blank line or a line that begins another block — a heading, a fence, a thematic break, a list item, a blockquote, or an
HTML block — and each of its lines SHALL carry the delimiter row's count of cells, where a cell is what splitting on the
unescaped pipes yields after one optional leading and one optional trailing pipe are removed. The verification SHALL
read the file set the build passes it, SHALL fail when it is given no path or a path it cannot read, and SHALL name each
offending file and line with the line's count and the delimiter row's.

A line inside a table carrying no unescaped pipe SHALL fail as a wrapped row. A pipe SHALL separate cells unless it
follows an odd number of backslashes. A fenced block SHALL NOT be read as a table. A table whose opening row begins with
a blockquote or list prefix is outside this requirement's coverage, and a line beginning with `<` SHALL be read as an
HTML block, so a row that begins with one ends the recognized table and the rows after it are not examined. This
requirement covers the recognized tables, never every line a renderer might lay out as a row.

#### Scenario: The standard check verifies table rows

- **WHEN** the standard `check` task runs
- **THEN** it includes the markdown table-row verification, over the same file set the markdown format owns

#### Scenario: A wrapped table row fails the build

- **WHEN** a table row is wrapped onto a second line, which carries no unescaped pipe
- **THEN** the verification fails and names the file, the line, and that it carries no separator

#### Scenario: A bare pipe inside a cell fails the build

- **WHEN** a table row carries more unescaped pipes than its delimiter row because a cell holds a bare `|`
- **THEN** the verification fails and names the file, the line, its count, and the delimiter row's count

#### Scenario: An escaped pipe is content

- **WHEN** a cell carries `\|` in place of a bare pipe
- **THEN** the verification passes that row

#### Scenario: A fenced block is not a table

- **WHEN** a fenced block contains pipe characters, as a diagram or a command does
- **THEN** the verification does not read it as a table

#### Scenario: A clean table passes

- **WHEN** every row of a table carries the delimiter row's count of unescaped pipes
- **THEN** the verification reports no failure for that table

#### Scenario: A table without outer pipes passes

- **WHEN** a table's rows carry no leading or trailing pipe, and each row's separators leave the delimiter row's cell
  count
- **THEN** the verification passes that table

#### Scenario: A supplied file that cannot be read fails

- **WHEN** a path the build passes the verification cannot be read
- **THEN** the verification fails and names that path

#### Scenario: A verification given no path fails

- **WHEN** the table-row verification runs with no path to read
- **THEN** it fails rather than reporting a clean scan
