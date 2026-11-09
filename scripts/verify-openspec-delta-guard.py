#!/usr/bin/env python3
"""Reject OpenSpec content the CLI would drop at archive.

Text outside any delta section is content the pinned CLI drops at archive while only warning and exiting 0, when the
change also carries content it applies, so `archive` applies the valid part and leaves an intended requirement
unchanged. A delta section the CLI does not recognise is no longer that shape under the pinned `1.16.1`: `validate`
rejects it with its own error and a non-zero exit, and `archive` refuses. The guard therefore keeps failing on the
stray-text warning, while the probe accepts the newer rejection only when the CLI's own diagnostic is present.

Two modes:

- `--validate-output <file>` reads a `openspec validate --all` transcript and fails when a line reports dropped
  content, printing the matched line.
- `--probe` builds four fixtures — the three loss-producing shapes and a fully valid control — runs the CLI over
  each, and asserts the outcomes: the valid control validates and archives cleanly, a non-zero exit counts as the
  upstream fix only when the CLI's own rejection diagnostic is present, and the retirement note fires only when both
  loss-producing shapes are rejected. A CLI release that rewords the messages therefore fails the probe instead of
  silently disarming the guard. It exits 3 with a stated reason when the CLI is absent; CI installs the CLI, so there
  it fails.

Exit status: 0 clean, 1 the guard fired or the probe's expectations no longer hold, 2 a usage error, 3 the probe found
no CLI.
"""

import argparse
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

DROP_PHRASES = ("not a delta section", "not carried into the main spec by archive")

REJECTION_PHRASES = (
    "No delta sections found",
    "Delta parsing found no operations",
    "which is not a delta section, so it is ignored",
)
CLI = "openspec"

VALID_SECTION = """## ADDED Requirements

### Requirement: Probe requirement

The probe SHALL exist so the change carries content the CLI applies.

#### Scenario: The probe holds

- **WHEN** the probe runs
- **THEN** it holds
"""

MALFORMED_SECTION = """## DEPRECATED Requirements

### Requirement: Ignored requirement

This requirement sits under a section the CLI does not recognise.
"""

STRAY_PROSE = """Prose above the first delta section is not applied by archive.
"""

FIXTURES = {
    "malformed-only": MALFORMED_SECTION,
    "mixed": VALID_SECTION + "\n" + MALFORMED_SECTION,
    "prose": STRAY_PROSE + "\n" + VALID_SECTION,
    "valid-only": VALID_SECTION,
}


def dropped_lines(text: str) -> list[str]:
    """The lines of a validation transcript that report content the CLI would drop."""
    return [line.strip() for line in text.splitlines() if any(phrase in line for phrase in DROP_PHRASES)]


def fixture_dir(root: Path, shape: str) -> Path:
    """Write one fixture change under `root` and return the project directory it belongs to."""
    project = root / shape
    spec = project / "openspec" / "changes" / "probe" / "specs" / "probe" / "spec.md"
    spec.parent.mkdir(parents=True)
    spec.write_text(FIXTURES[shape], encoding="utf-8")
    return project


def run_cli(arguments: list[str], cwd: Path) -> subprocess.CompletedProcess[str]:
    """Run the CLI in `cwd`, capturing both streams."""
    return subprocess.run([CLI, *arguments], cwd=cwd, capture_output=True, text=True, check=False)


def probe(runner=run_cli) -> int:
    """Assert every fixture's outcome, so a rewording CLI fails here rather than disarming the gate."""
    if shutil.which(CLI) is None:
        print(
            "openspec-delta-guard: {0} is not on PATH; the probe needs the CLI CI installs".format(CLI),
            file=sys.stderr,
        )
        return 3
    failures: list[str] = []
    rejected_shapes: set[str] = set()
    with tempfile.TemporaryDirectory(prefix="openspec-delta-guard-") as directory:
        root = Path(directory)
        for shape in FIXTURES:
            project = fixture_dir(root, shape)
            validation = runner(["validate", "--all"], project)
            output = validation.stdout + validation.stderr
            rejected = any(phrase in output for phrase in REJECTION_PHRASES)
            if shape == "valid-only":
                if validation.returncode != 0:
                    failures.append(
                        "valid-only: validation exited {0}, so the fixture environment is broken rather than "
                        "strict: {1}".format(validation.returncode, output.strip()[-200:])
                    )
                    continue
                archived = runner(["archive", "probe", "--yes"], project)
                if archived.returncode != 0:
                    failures.append("valid-only: archive exited {0}".format(archived.returncode))
                elif dropped_lines(output):
                    failures.append("valid-only: the CLI warned about dropping content in a fully valid delta")
                continue
            if validation.returncode != 0:
                if not rejected:
                    failures.append(
                        "{0}: validation exited {1} without the CLI's rejection diagnostic, so this is a broken "
                        "fixture rather than the upstream fix: {2}".format(
                            shape, validation.returncode, output.strip()[-200:]
                        )
                    )
                    continue
                if shape == "malformed-only":
                    continue
                rejected_shapes.add(shape)
                continue
            if shape == "malformed-only":
                failures.append(
                    "malformed-only: validation passed, but the CLI should refuse a delta "
                    "with no recognised section"
                )
                continue
            if not dropped_lines(output):
                failures.append(
                    "{0}: validation passed without the drop warning, so the guard would not fire".format(shape)
                )
                continue
            archived = runner(["archive", "probe", "--yes"], project)
            if archived.returncode != 0:
                failures.append("{0}: archive exited {1}".format(shape, archived.returncode))
                continue
            applied = (project / "openspec" / "specs" / "probe" / "spec.md").read_text(encoding="utf-8")
            if "Probe requirement" not in applied:
                failures.append("{0}: the recognised requirement was not applied".format(shape))
            if "Ignored requirement" in applied or STRAY_PROSE in applied:
                failures.append("{0}: content the CLI should drop was applied".format(shape))
    for failure in failures:
        print("openspec-delta-guard: {0}".format(failure), file=sys.stderr)
    if not failures and {"mixed", "prose"} <= rejected_shapes:
        print(
            "openspec-delta-guard: the CLI rejects both loss-producing shapes itself; the guard can retire, and "
            "Fission-AI/OpenSpec#2195 can close"
        )
    return 1 if failures else 0


def check_transcript(path: Path) -> int:
    """Fail when the validation transcript reports content the CLI would drop."""
    try:
        text = path.read_text(encoding="utf-8")
    except OSError as error:
        print("openspec-delta-guard: cannot read {0}: {1}".format(path, error), file=sys.stderr)
        return 2
    matches = dropped_lines(text)
    for line in matches:
        print(
            "openspec-delta-guard: the validation output reports content the CLI would drop: {0}".format(line),
            file=sys.stderr,
        )
    return 1 if matches else 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--validate-output", type=Path, help="a `openspec validate --all` transcript to check")
    mode.add_argument(
        "--probe",
        action="store_true",
        help="run the CLI over the four fixtures, including a valid control",
    )
    arguments = parser.parse_args(argv)
    if arguments.probe:
        return probe()
    return check_transcript(arguments.validate_output)


if __name__ == "__main__":
    sys.exit(main())
