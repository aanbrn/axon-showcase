# Process defects — expose-the-jfr-recording-property

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

No process defect observed.

The unit's measurement briefly mixed two populations — the first comparison ran the whole component suite, whose delta
included the JFR control that only executes when the recording is on, and the isolated re-measurement on one test class
replaced it before any figure was reported. That is the probe-correction working inside the unit, not a defect with a
consequence to record.
