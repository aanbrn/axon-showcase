## MODIFIED Requirements

### Requirement: A baseline run reports its measurements

A `baseline` run SHALL produce a report of its measurement: the target's shape, the calibration knee, the operating
point and duration, the plateau's response times, and the per-service resource usage during the plateau. It SHALL also
compare the plateau's per read and write request mean, 95th, and 99th percentile response times against the reference
recorded for the target it measured, reporting each figure alongside the recorded value and their delta, and record
those figures as the baseline reference the performance profiles assert against. It SHALL write that reference unless a
figure regresses beyond the configured tolerance and no refresh is intended; when one does, the run SHALL fail, naming
the regressed figures, and leave the reference unchanged. A measurement that records no request figures SHALL also fail
and leave the reference unchanged, even when a refresh is intended, since a figure-less reference would silently send
the performance profiles to their absolute thresholds. The run SHALL write a dated record of itself, identified by the
environment it measured when that is not the default, ready to be annotated and committed, whether or not its comparison
failed. The report SHALL take the calibration knee from the `calibrate` run's written output rather than re-deriving it
at run time. When the calibration finds no sustained departure, the report SHALL state that the knee was not measured
and give the operating point as a fraction of the calibration ceiling.

#### Scenario: The run writes a report

- **WHEN** a `baseline` run completes
- **THEN** it writes a report describing the run under the module's build output

#### Scenario: The run records the baseline reference

- **WHEN** a `baseline` run completes and no figure regresses beyond the configured tolerance, or a refresh is intended,
  and the run measured at least one request figure
- **THEN** it writes the baseline reference — the plateau's response times per read and write request, with the
  derivation policy and the target measured — for the performance profiles to assert against

#### Scenario: The run writes a committable record

- **WHEN** a `baseline` run completes
- **THEN** it writes a dated record of the run's numbers under `docs/load-tests/`, identified by the environment it
  measured when that is not the default, ready to be annotated and committed, whether or not the run's comparison failed

#### Scenario: The report names the run's method and numbers

- **WHEN** the report is written
- **THEN** it states the target shape, the calibration knee, the operating point, the plateau duration, the plateau's
  response times, and the per-service resource usage

#### Scenario: An unmeasured knee is labeled as the ceiling

- **WHEN** the calibration finds no sustained departure
- **THEN** the report states that the knee was not measured and gives the operating point as a fraction of the
  calibration ceiling

#### Scenario: The run reports its figures against the recorded reference

- **WHEN** a `baseline` run completes and a reference is recorded for the target it measured, and the run measured at
  least one request figure
- **THEN** it reports each read and write request's mean, 95th, and 99th percentile response time alongside the recorded
  value and their delta

#### Scenario: A regression beyond the tolerance fails the run and keeps the reference

- **WHEN** a figure regresses beyond the configured tolerance and no refresh is intended
- **THEN** the run fails, naming the regressed figures, and the recorded reference is left unchanged

#### Scenario: A measurement with no figures withholds the reference

- **WHEN** the plateau records none of the read and write requests
- **THEN** the run fails, reports that no figures were measured, and leaves the recorded reference unchanged, even when
  a refresh is intended

#### Scenario: An intended refresh accepts a regression

- **WHEN** a refresh is intended and the run measured at least one request figure
- **THEN** the run reports the deltas, including any beyond the configured tolerance, and writes the reference it
  measured

#### Scenario: Nothing to compare records the measurement

- **WHEN** no reference is recorded for the target the run measured, or the recorded one names another target, and the
  run measured at least one request figure
- **THEN** the run reports that there is nothing to compare and writes the reference it measured

## ADDED Requirements

### Requirement: The recorded baseline runs are trended

A trend view SHALL report the dated records under `docs/load-tests/` as a chronological series: each record's date, its
target, its knee and operating point, its plateau duration, and the plateau's mean, 95th, and 99th percentile response
time and mean throughput. It SHALL read the records as written, annotations included, rather than requiring a second,
machine-only format, and SHALL name each entry's target so records from more than one environment stay comparable. A
record whose figures it cannot read SHALL be reported as unreadable rather than failing the view.

#### Scenario: The trend reports each recorded run in order

- **WHEN** the trend view runs and dated records exist
- **THEN** it reports, per record in date order — a day's records ordered by their filename slug — the date, target,
  knee and operating point, plateau duration, and the plateau's mean, 95th, 99th percentile, and throughput

#### Scenario: Records for more than one environment stay distinguishable

- **WHEN** records exist for more than one target
- **THEN** each reported entry names the target it measured

#### Scenario: An unreadable record is reported, not fatal

- **WHEN** a record carries no readable plateau figures
- **THEN** the trend view reports it as unreadable and still reports the others

#### Scenario: An empty record set is reported

- **WHEN** no dated records exist
- **THEN** the trend view reports that there is nothing to trend
