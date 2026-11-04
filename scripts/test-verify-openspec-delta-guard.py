#!/usr/bin/env python3
"""Tests for the OpenSpec delta guard (`scripts/verify-openspec-delta-guard.py`). Stdlib only.

Run with `python3 scripts/test-verify-openspec-delta-guard.py`. The tool's kebab-case filename is not an importable
identifier, so it is loaded by path, the way `scripts/test-claim-scan.py` loads the claim scanner.

The predicate is asserted over recorded CLI output, so `check` exercises it without the CLI; the CLI-dependent half is
the probe, which CI runs where the workflow installs the CLI.
"""

import importlib.util
import io
import subprocess
import tempfile
import unittest
from contextlib import redirect_stderr
from pathlib import Path
from unittest import mock

TOOL_PATH = Path(__file__).resolve().parent / "verify-openspec-delta-guard.py"
_spec = importlib.util.spec_from_file_location("verify_openspec_delta_guard", TOOL_PATH)
guard = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(guard)

SECTION_WARNING = (
    '  \u26a0 [WARNING] showcase/quality/agent-skills/spec.md: Requirement "X" is under "## DEPRECATED Requirements", '
    'which is not a delta section, so it is ignored.'
)
PROSE_WARNING = (
    '  \u26a0 [WARNING] showcase/quality/agent-skills/spec.md: Text above the first "## " section is not carried into '
    "the main spec by archive."
)
LONG_REQUIREMENT_WARNING = '  \u26a0 [WARNING] requirements[9]: Requirement text is very long (>500 characters).'
CLEAN = "\u2713 change/probe\n\u2714 spec/showcase/quality/agent-skills\nTotals: 26 passed, 0 failed (26 items)"


class DroppedLinesTests(unittest.TestCase):
    """The predicate recognises the two loss-producing warnings and nothing else."""

    def test_a_malformed_section_is_reported(self) -> None:
        self.assertEqual(guard.dropped_lines(SECTION_WARNING), [SECTION_WARNING.strip()])

    def test_stray_prose_is_reported(self) -> None:
        self.assertEqual(guard.dropped_lines(PROSE_WARNING), [PROSE_WARNING.strip()])

    def test_a_long_requirement_warning_is_not_reported(self) -> None:
        self.assertEqual(guard.dropped_lines(LONG_REQUIREMENT_WARNING), [])

    def test_a_clean_run_is_not_reported(self) -> None:
        self.assertEqual(guard.dropped_lines(CLEAN), [])

    def test_empty_output_is_not_reported(self) -> None:
        self.assertEqual(guard.dropped_lines(""), [])


class FixtureTests(unittest.TestCase):
    """Each fixture carries the shape its name promises."""

    def test_every_fixture_is_written_under_its_project(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            for shape in guard.FIXTURES:
                project = guard.fixture_dir(Path(directory), shape)
                spec = project / "openspec" / "changes" / "probe" / "specs" / "probe" / "spec.md"
                self.assertTrue(spec.is_file(), shape)
                self.assertEqual(spec.read_text(encoding="utf-8"), guard.FIXTURES[shape], shape)

    def test_the_mixed_and_prose_fixtures_carry_a_valid_section(self) -> None:
        for shape in ("mixed", "prose"):
            self.assertIn("## ADDED Requirements", guard.FIXTURES[shape], shape)
        self.assertIn("## DEPRECATED Requirements", guard.FIXTURES["mixed"])
        self.assertIn("Prose above the first delta section", guard.FIXTURES["prose"])


class TranscriptTests(unittest.TestCase):
    """`--validate-output` fails on a reported drop and passes on a clean run."""

    def _transcript(self, text: str) -> Path:
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        path = Path(directory.name) / "validate.log"
        path.write_text(text, encoding="utf-8")
        return path

    def test_a_reported_drop_fails(self) -> None:
        with redirect_stderr(io.StringIO()):
            self.assertEqual(guard.check_transcript(self._transcript(SECTION_WARNING)), 1)

    def test_a_clean_transcript_passes(self) -> None:
        self.assertEqual(guard.check_transcript(self._transcript(CLEAN)), 0)

    def test_an_unreadable_transcript_is_a_usage_error(self) -> None:
        with redirect_stderr(io.StringIO()):
            self.assertEqual(guard.check_transcript(Path("/nonexistent/validate.log")), 2)


class ProbeTests(unittest.TestCase):
    """The probe reports a missing CLI rather than passing silently, and discriminates a regressed fixture."""

    def _runner(
        self,
        *,
        malformed_code: int = 1,
        mixed_code: int = 0,
        prose_code: int = 0,
        valid_code: int = 0,
        diagnose: bool = True,
        warn: bool = True,
        mixed_applied: str = "### Requirement: Probe requirement\n",
        prose_applied: str = "### Requirement: Probe requirement\n",
        archive_code: int = 0,
    ):
        def runner(args: list[str], cwd: Path) -> subprocess.CompletedProcess[str]:
            shape = cwd.name
            spec = cwd / "openspec" / "specs" / "probe" / "spec.md"
            if args[0] == "validate":
                codes = {
                    "malformed-only": malformed_code,
                    "mixed": mixed_code,
                    "prose": prose_code,
                    "valid-only": valid_code,
                }
                warnings = {"mixed": SECTION_WARNING, "prose": PROSE_WARNING}
                output = warnings.get(shape, "") if warn else ""
                if diagnose and codes[shape] != 0:
                    output = "No delta sections found. Add headers such as '## ADDED Requirements'."
                return subprocess.CompletedProcess(args, codes[shape], stdout=output, stderr="")
            if archive_code == 0:
                spec.parent.mkdir(parents=True, exist_ok=True)
                spec.write_text(mixed_applied if shape == "mixed" else prose_applied, encoding="utf-8")
            return subprocess.CompletedProcess(args, archive_code, stdout="", stderr="")

        return runner

    def _probe(self, runner) -> tuple[int, str, str]:
        out, err = io.StringIO(), io.StringIO()
        with mock.patch.object(guard.shutil, "which", return_value="/usr/bin/openspec"):
            with redirect_stderr(err):
                with mock.patch("sys.stdout", out):
                    code = guard.probe(runner=runner)
        return code, out.getvalue(), err.getvalue()

    def test_a_missing_cli_is_reported_as_such(self) -> None:
        with mock.patch.object(guard.shutil, "which", return_value=None), redirect_stderr(io.StringIO()) as error:
            self.assertEqual(guard.probe(), 3)
        self.assertIn("not on PATH", error.getvalue())

    def test_the_modelled_shapes_pass(self) -> None:
        self.assertEqual(self._probe(self._runner())[0], 0)

    def test_a_cli_that_rejects_both_loss_shapes_is_reported_as_retirable(self) -> None:
        code, out, _ = self._probe(self._runner(mixed_code=1, prose_code=1))
        self.assertEqual(code, 0)
        self.assertIn("the guard can retire", out)

    def test_one_rejected_shape_is_not_yet_a_retirement(self) -> None:
        code, out, _ = self._probe(self._runner(mixed_code=1))
        self.assertEqual(code, 0)
        self.assertNotIn("the guard can retire", out)

    def test_an_unexpected_validation_failure_is_not_a_retirement(self) -> None:
        code, _, error = self._probe(self._runner(mixed_code=2, diagnose=False))
        self.assertEqual(code, 1)
        self.assertIn("without the CLI's rejection diagnostic", error)

    def test_a_broken_valid_only_fixture_fails(self) -> None:
        code, _, error = self._probe(self._runner(valid_code=1))
        self.assertEqual(code, 1)
        self.assertIn("valid-only: validation exited 1", error)

    def test_a_missing_drop_warning_fails(self) -> None:
        code, _, error = self._probe(self._runner(warn=False))
        self.assertEqual(code, 1)
        self.assertIn("would not fire", error)

    def test_content_the_cli_should_drop_fails_when_applied(self) -> None:
        code, _, error = self._probe(self._runner(mixed_applied="### Requirement: Ignored requirement\n"))
        self.assertEqual(code, 1)
        self.assertIn("content the CLI should drop was applied", error)

    def test_a_lost_recognised_requirement_fails(self) -> None:
        code, _, error = self._probe(self._runner(mixed_applied=""))
        self.assertEqual(code, 1)
        self.assertIn("recognised requirement was not applied", error)

    def test_a_failing_archive_fails(self) -> None:
        code, _, error = self._probe(self._runner(archive_code=1))
        self.assertEqual(code, 1)
        self.assertIn("archive exited 1", error)

    def test_a_malformed_only_fixture_that_validates_fails(self) -> None:
        code, _, error = self._probe(self._runner(malformed_code=0))
        self.assertEqual(code, 1)
        self.assertIn("should refuse a delta with no recognised section", error)


if __name__ == "__main__":
    unittest.main()
