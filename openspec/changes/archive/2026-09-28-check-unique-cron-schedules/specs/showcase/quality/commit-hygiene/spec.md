## ADDED Requirements

### Requirement: Workflow cron schedules do not collide

The standard `check` task SHALL verify that no two `cron` schedules among the workflow files under `.github/workflows/`
collide — two jobs firing in the same minute undo a deliberate stagger — so a collision fails the CI `build` gate,
whether the two schedules live in different workflow files or in the same one. A pair of schedules collide, as the
verification determines it, when their minute and hour fields are equal and their day-of-week fields overlap (equal, or
either field is `*`, since a `*` day-of-week runs daily and so runs on the other schedule's day); two schedules naming
different days of the week do not collide, because they never run on the same day. A day-of-week field of `7` is treated
as `0`, since both name Sunday. A schedule that restricts day-of-month or month is compared with another schedule only
by exact cron-string equality, because the verification does not compute whether two such day constraints intersect. The
verification SHALL name each colliding pair of schedules, the workflow file or files carrying them, and the shared cron,
and SHALL NOT treat a commented-out `cron` as a schedule.

#### Scenario: The standard check verifies the workflow schedules

- **WHEN** the standard `check` task runs
- **THEN** it includes the workflow-schedule verification

#### Scenario: Two workflows sharing a cron string fail the build

- **WHEN** two workflow files carry the same `cron` string
- **THEN** the workflow-schedule verification fails and names both files and the shared cron

#### Scenario: Two schedules in one workflow file sharing a cron string fail the build

- **WHEN** one workflow file carries two `cron` schedules with the same `cron` string
- **THEN** the workflow-schedule verification fails and names the file and the shared cron

#### Scenario: A daily and a weekly workflow sharing a minute fail the build

- **WHEN** a workflow whose day-of-week field is `*` and a workflow naming a weekday carry the same minute and hour
- **THEN** the workflow-schedule verification fails and names both files and the shared minute

#### Scenario: Weekly workflows on different days sharing a minute pass

- **WHEN** two workflows naming different days of the week carry the same minute and hour
- **THEN** the workflow-schedule verification passes for that pair, because the two schedules never run on the same day

#### Scenario: A restricted day constraint is matched by exact cron string

- **WHEN** two workflow files carry different `cron` schedules that both restrict day-of-month or month and share the
  same minute and hour
- **THEN** the workflow-schedule verification does not report them as colliding unless their cron strings are identical

#### Scenario: A commented-out cron is not a schedule

- **WHEN** a workflow file contains a commented-out `cron` line
- **THEN** the workflow-schedule verification does not treat that line as a schedule

#### Scenario: A clean set of workflow schedules passes

- **WHEN** no two workflow schedules collide under the verification
- **THEN** the workflow-schedule verification passes
