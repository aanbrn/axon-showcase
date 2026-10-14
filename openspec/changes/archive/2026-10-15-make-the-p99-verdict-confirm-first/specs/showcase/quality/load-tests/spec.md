## MODIFIED Requirements

### Requirement: A baseline run reports its measurements

A `baseline` run SHALL produce a report of its measurement: the target's shape, the calibration knee, the operating
point and duration, the plateau's response times, and the per-service resource usage during the plateau. It SHALL record
the operating point it measured in the baseline reference, alongside the plateau's per read and write request mean,
95th, and 99th percentile response times and the derivation policy. It SHALL compare those figures against the reference
recorded for the target it measured and for an operating point within a tenth of the recorded operating point (the
matching band), reporting each figure alongside the recorded value and their delta — a reference recorded for another
target, at an operating point beyond the matching band, or with no operating point, is not compared. It SHALL write that
reference unless a figure exceeds the configured tolerance and no refresh is intended. When a mean or 95th-percentile
figure exceeds it, the run SHALL fail, naming the regressed figures, and leave the reference unchanged. When only
99th-percentile figures exceed it, the run SHALL report them as an unconfirmed excursion, name the confirmation it
needs, and leave the reference unchanged — since a single run cannot distinguish a tail regression from the measurement
environment, and a second run at the same operating point compares against the figures it withheld. The report SHALL
name every figure that exceeded the tolerance and state which of them are regressed and which are unconfirmed
excursions. A measurement that records no request figures SHALL also fail and leave the reference unchanged, even when a
refresh is intended, since a figure-less reference would silently send the performance profiles to their absolute
thresholds. The run SHALL write a dated record of itself, identified by the environment it measured when that is not the
default, ready to be annotated and committed, whether or not its comparison failed. The report SHALL take the
calibration knee from the `calibrate` run's written output rather than re-deriving it at run time. When the calibration
finds no sustained departure, the report SHALL state that the knee was not measured and give the operating point as a
fraction of the calibration ceiling.

#### Scenario: The run writes a report

- **WHEN** a `baseline` run completes
- **THEN** it writes a report describing the run under the module's build output

#### Scenario: The run records the baseline reference

- **WHEN** a `baseline` run completes and no figure exceeds the configured tolerance, or a refresh is intended, and the
  run measured at least one request figure
- **THEN** it writes the baseline reference — the operating point it measured, the plateau's response times per read and
  write request, with the derivation policy and the target measured — for the performance profiles to assert against

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

- **WHEN** a `baseline` run completes and a reference is recorded for the target it measured at an operating point
  within the matching band, and the run measured at least one request figure
- **THEN** it reports each read and write request's mean, 95th, and 99th percentile response time alongside the recorded
  value and their delta

#### Scenario: A regression beyond the tolerance fails the run and keeps the reference

- **WHEN** a mean or 95th-percentile figure regresses beyond the configured tolerance and no refresh is intended
- **THEN** the run fails, naming the regressed figures, and the recorded reference is left unchanged

#### Scenario: A p99-only excursion is reported as unconfirmed and keeps the reference

- **WHEN** only 99th-percentile figures exceed the configured tolerance and no refresh is intended
- **THEN** the run reports them as an unconfirmed excursion, names the confirmation it needs, does not fail the run, and
  leaves the recorded reference unchanged so the next run at the same operating point compares against the same figures

#### Scenario: A measurement with no figures withholds the reference

- **WHEN** the plateau records none of the read and write requests
- **THEN** the run fails, reports that no figures were measured, and leaves the recorded reference unchanged, even when
  a refresh is intended

#### Scenario: An intended refresh accepts a regression

- **WHEN** a refresh is intended, a mean or 95th-percentile figure exceeds the configured tolerance, and the run
  measured at least one request figure
- **THEN** the run reports the deltas, including any beyond the configured tolerance, and writes the reference it
  measured

#### Scenario: An intended refresh accepts a p99-only excursion

- **WHEN** a refresh is intended, only 99th-percentile figures exceed the configured tolerance, and the run measured at
  least one request figure
- **THEN** the run reports the excursion and writes the reference it measured, with it accepted

#### Scenario: Nothing to compare records the measurement

- **WHEN** no reference is recorded for the target the run measured, or the recorded one names another target, and the
  run measured at least one request figure
- **THEN** the run reports that there is nothing to compare and writes the reference it measured

#### Scenario: A reference recorded beyond the operating-point band is not compared

- **WHEN** a reference is recorded for the target at an operating point beyond the matching band, and the run measured
  at least one request figure
- **THEN** the run reports that the reference is for another operating point, does not report a regression, and records
  the measurement it made

#### Scenario: A reference that records no operating point is not compared

- **WHEN** a reference is recorded for the target but records no operating point, and the run measured at least one
  request figure
- **THEN** the run reports that the reference records no operating point, does not report a regression, and records the
  measurement it made
