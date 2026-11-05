#!/usr/bin/env python3
"""Bumps the Gradle wrapper with its distribution checksum, and rolls back when the bump does not complete.

Gradle's `wrapper` task rewrites four files from its own templates and, once the properties carry a
`distributionSha256Sum`, refuses a version bump that does not supply a value for the new release. The value it needs is
published beside the distribution and is not derivable from the repository, so the bump has two steps that must land
together — the version and its checksum — plus a rollback, because the first pass may already have rewritten the URL
when the second fails.

This tool performs that transaction: it resolves the checksum for the requested version, captures the four files' bytes
and executable modes, runs both `wrapper` passes, verifies the result, and restores the capture when anything fails. It
never chooses a version itself — the caller owns that decision (`/gradle-update` reads it from the dependency
report).

Usage: `python3 scripts/update-gradle-wrapper.py --version <version> [options]`

Options:
  --version <v>            the Gradle version to pin (required)
  --root <dir>             the repository root (default: the tool's parent's parent)
  --gradlew <path>         the wrapper script to run (default: <root>/gradlew); the tests point it at a fixture
  --checksum-url <url>     the checksum source, with `{version}` as the placeholder

Exit status: 0 when the wrapper is pinned to the version and checksum. On 1 the report states which outcome holds: the
four files were restored to the bytes and modes the run found; the restore is incomplete, and the snapshot directory it
names holds those bytes; or the run stopped before touching anything. A killed process writes no final report at all, so
its only recovery material is the snapshot path announced before the passes.
"""

import argparse
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.error
import urllib.request
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent

CHECKSUM_URL = "https://services.gradle.org/distributions/gradle-{version}-all.zip.sha256"

VERSION_PATTERN = re.compile(r"^\d+\.\d+(\.\d+)?$")
HEX_64 = re.compile(r"^[0-9a-fA-F]{64}$")

PROPERTIES = Path("gradle") / "wrapper" / "gradle-wrapper.properties"
WRAPPER_FILES = (
    PROPERTIES,
    Path("gradle") / "wrapper" / "gradle-wrapper.jar",
    Path("gradlew"),
    Path("gradlew.bat"),
)

DOWNLOAD_TIMEOUT_SECONDS = 30


def fetch_checksum(url, version):
    """Fetches the published checksum of a distribution.

    :param url: the checksum URL template, with `{version}` as the placeholder
    :param version: the Gradle version
    :return: the 64-hexadecimal checksum
    :raises ValueError: when the response is absent, empty, or not a checksum
    """
    target = url.format(version=version)
    try:
        with urllib.request.urlopen(target, timeout=DOWNLOAD_TIMEOUT_SECONDS) as response:
            body = response.read().decode("utf-8").strip()
    except (urllib.error.URLError, OSError) as failure:
        raise ValueError("the checksum could not be fetched from {0}: {1}".format(target, failure))
    if not body:
        raise ValueError("the checksum response from {0} is empty".format(target))
    if not HEX_64.match(body):
        raise ValueError(
            "the checksum response from {0} is not 64 hexadecimal characters: {1!r}".format(target, body)
        )
    return body


def capture(root, directory):
    """Captures the four wrapper files' bytes and executable modes.

    :param root: the repository root
    :param directory: the directory to capture into
    :return: the captured paths, relative to the root
    """
    captured = []
    for relative in WRAPPER_FILES:
        source = root / relative
        if not source.is_file():
            raise ValueError("the wrapper file {0} is missing".format(relative))
        target = directory / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        captured.append(relative)
    return captured


def restore(root, directory, captured):
    """Restores the captured bytes and executable modes.

    :param root: the repository root
    :param directory: the directory the capture was taken into
    :param captured: the captured paths, relative to the root
    :return: whether every captured file was restored
    """
    restored = True
    for relative in captured:
        try:
            shutil.copy2(directory / relative, root / relative)
        except OSError as failure:
            restored = False
            print("The file {0} could not be restored: {1}".format(relative, failure))
    return restored


def wrapper_command(gradlew, version, checksum):
    """Builds the `wrapper` invocation both passes run.

    :param gradlew: the wrapper script to run
    :param version: the Gradle version to pin
    :param checksum: the version's published checksum
    :return: the command line
    """
    return [
        str(gradlew),
        "wrapper",
        "--gradle-version={0}".format(version),
        "--distribution-type=all",
        "--gradle-distribution-sha256-sum={0}".format(checksum),
        "--no-daemon",
        "--console=plain",
    ]


def report_restore(root, snapshot, captured, headline):
    """Reports a restore's outcome as one of unchanged, restored, or incomplete.

    The snapshot is removed only when every file came back; an incomplete restore keeps it, since it is the only copy of
    the bytes the run found.

    :param root: the repository root
    :param snapshot: the directory the capture was taken into
    :param captured: the captured paths, relative to the root
    :param headline: the failure the restore follows
    """
    if restore(root, snapshot, captured):
        shutil.rmtree(snapshot, ignore_errors=True)
        print("{0}; the four wrapper files were restored.".format(headline))
    else:
        print(
            "{0}; the restore is incomplete, and {1} holds the bytes it found.".format(headline, snapshot),
            flush=True,
        )


def properties_of(path):
    """Reads the wrapper properties into a mapping.

    :param path: the properties file
    :return: the file's keys and values
    """
    entries = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or "=" not in stripped:
            continue
        key, value = stripped.split("=", 1)
        entries[key.strip()] = value.strip().replace("\\:", ":")
    return entries


def result_defects(root, version, checksum):
    """Returns how the resulting properties disagree with the requested pin.

    :param root: the repository root
    :param version: the Gradle version the pin should name
    :param checksum: the checksum the pin should carry
    :return: one message per disagreement, empty when the result is complete
    """
    entries = properties_of(root / PROPERTIES)
    found = []
    url = entries.get("distributionUrl", "")
    if not url.endswith("gradle-{0}-all.zip".format(version)):
        found.append("distributionUrl does not name gradle-{0}-all.zip: {1!r}".format(version, url))
    pinned = entries.get("distributionSha256Sum")
    if pinned != checksum:
        found.append("distributionSha256Sum is {0!r} where {1!r} was requested".format(pinned, checksum))
    return found


def main():
    """Runs the bump and reports its outcome."""
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--version", required=True, help="the Gradle version to pin")
    parser.add_argument("--root", type=Path, default=REPO, help="the repository root")
    parser.add_argument("--gradlew", type=Path, default=None, help="the wrapper script to run")
    parser.add_argument(
        "--checksum-url", default=CHECKSUM_URL, help="the checksum source, with {version} as placeholder"
    )
    arguments = parser.parse_args()

    version = arguments.version
    if not VERSION_PATTERN.match(version):
        print("The version {0!r} is not a stable Gradle release version. No wrapper file was touched.".format(version))
        return 1

    root = arguments.root.resolve()
    gradlew = (arguments.gradlew or (root / "gradlew")).resolve()

    try:
        checksum = fetch_checksum(arguments.checksum_url, version)
    except ValueError as failure:
        print("{0}. No wrapper file was touched.".format(failure))
        return 1

    snapshot = Path(tempfile.mkdtemp(prefix="wrapper-bump-"))
    try:
        captured = capture(root, snapshot)
    except ValueError as failure:
        shutil.rmtree(snapshot, ignore_errors=True)
        print("{0}. No wrapper file was touched.".format(failure))
        return 1
    print("Wrapper snapshot: {0}".format(snapshot), flush=True)

    command = wrapper_command(gradlew, version, checksum)
    try:
        for pass_number in (1, 2):
            completed = subprocess.run(command, cwd=str(root), capture_output=True, text=True)
            if completed.returncode != 0:
                report_restore(root, snapshot, captured, "The wrapper's pass {0} failed".format(pass_number))
                print(completed.stderr.strip() or completed.stdout.strip())
                return 1

        defects = result_defects(root, version, checksum)
        if defects:
            report_restore(root, snapshot, captured, "The bump did not complete")
            for defect in defects:
                print("  {0}".format(defect))
            return 1
    except BaseException:
        report_restore(root, snapshot, captured, "The bump was interrupted")
        raise

    shutil.rmtree(snapshot, ignore_errors=True)
    print("The wrapper is pinned to Gradle {0} with {1}".format(version, checksum))
    return 0


if __name__ == "__main__":
    sys.exit(main())
