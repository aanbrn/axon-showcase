#!/usr/bin/env python3
"""Tests for the wrapper-checksum check (`scripts/verify-wrapper-checksum.py`). Stdlib only.

Run with `python3 scripts/test-verify-wrapper-checksum.py`. The tool's kebab-case filename is not an importable
identifier, so it is loaded by path, the way `scripts/test-claim-scan.py` loads the claim scanner.

Each arm the check decides — a missing pin, a short value, a non-hexadecimal value, a non-`-all` archive, a missing
URL — carries its own case, and a valid file passes, so a rule that stops deciding its arm fails here rather than
reading as a clean wrapper.
"""

import importlib.util
import tempfile
import unittest
from pathlib import Path

TOOL_PATH = Path(__file__).resolve().parent / "verify-wrapper-checksum.py"
_spec = importlib.util.spec_from_file_location("verify_wrapper_checksum", TOOL_PATH)
verify_wrapper_checksum = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(verify_wrapper_checksum)

VALID = (
    "distributionBase=GRADLE_USER_HOME\n"
    "distributionPath=wrapper/dists\n"
    "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.8.2-all.zip\n"
    "networkTimeout=10000\n"
    "zipStoreBase=GRADLE_USER_HOME\n"
    "zipStorePath=wrapper/dists\n"
    "distributionSha256Sum=a1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf\n"
)


class VerifyWrapperChecksumTests(unittest.TestCase):
    """The check's arms, each against its own properties fixture."""

    def verdict(self, body):
        """Runs the check over the given properties body.

        :param body: the properties file's content
        :return: the defects the check reports
        """
        with tempfile.TemporaryDirectory() as directory:
            properties = Path(directory) / "gradle-wrapper.properties"
            properties.write_text(body, encoding="utf-8")
            return verify_wrapper_checksum.defects(verify_wrapper_checksum.read_properties(properties))

    def test_a_valid_pin_passes(self):
        """The repository's own pin shape is accepted, URL colon unescaped."""
        self.assertEqual([], self.verdict(VALID))

    def test_the_repository_pin_passes(self):
        """The committed properties file passes, so the check is not vacuous on the real artifact."""
        self.assertEqual(
            [],
            verify_wrapper_checksum.defects(
                verify_wrapper_checksum.read_properties(verify_wrapper_checksum.PROPERTIES)
            ),
        )

    def test_a_missing_checksum_fails(self):
        """A file without the pin is named as an unverified download."""
        body = "\n".join(line for line in VALID.splitlines() if not line.startswith("distributionSha256Sum="))
        defects = self.verdict(body + "\n")
        self.assertEqual(1, len(defects))
        self.assertIn("is missing", defects[0])
        self.assertIn("unverified download", defects[0])

    def test_a_short_checksum_fails(self):
        """A value that is not 64 characters is named with its length problem."""
        body = VALID.replace(
            "a1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf",
            "a1bb7e96",
        )
        defects = self.verdict(body)
        self.assertEqual(1, len(defects))
        self.assertIn("not 64 hexadecimal characters", defects[0])

    def test_a_non_hexadecimal_checksum_fails(self):
        """A 64-character value with a non-hex character is rejected rather than counted as a checksum."""
        body = VALID.replace(
            "a1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf",
            "z1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf",
        )
        defects = self.verdict(body)
        self.assertEqual(1, len(defects))
        self.assertIn("not 64 hexadecimal characters", defects[0])

    def test_an_uppercase_checksum_is_accepted(self):
        """The requirement says 64 hexadecimal characters, so an uppercase value is a checksum too."""
        body = VALID.replace(
            "a1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf",
            "A1BB7E9651EF09E4CD9A62D471564E0EE23B5757E5F21FD7358D7CFC21018FDF",
        )
        self.assertEqual([], self.verdict(body))

    def test_a_non_all_archive_fails(self):
        """A `-bin.zip` distribution is rejected, since the repository pins the `-all` archive."""
        body = VALID.replace("gradle-9.8.2-all.zip", "gradle-9.8.2-bin.zip")
        defects = self.verdict(body)
        self.assertEqual(1, len(defects))
        self.assertIn("-all distribution archive", defects[0])

    def test_a_missing_url_fails(self):
        """A file without a distribution URL is named rather than silently accepted."""
        body = "\n".join(line for line in VALID.splitlines() if not line.startswith("distributionUrl="))
        defects = self.verdict(body + "\n")
        self.assertEqual(1, len(defects))
        self.assertIn("distributionUrl is missing", defects[0])

    def test_both_defects_are_reported(self):
        """Two defects are both reported, so a second one is not hidden by the first."""
        body = "\n".join(line for line in VALID.splitlines() if not line.startswith("distributionSha256Sum="))
        body = body.replace("gradle-9.8.2-all.zip", "gradle-9.8.2-bin.zip")
        defects = self.verdict(body + "\n")
        self.assertEqual(2, len(defects))


if __name__ == "__main__":
    unittest.main()
