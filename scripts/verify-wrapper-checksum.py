#!/usr/bin/env python3
"""Validates the Gradle wrapper's pinned distribution configuration.

The wrapper verifies every downloaded distribution against `distributionSha256Sum`, so the pin is what turns a
corrupted archive into a failure instead of a silent install. Gradle refuses a version bump that would leave the pin
without a value for the new release, but it cannot catch a pin that was never added, or one a hand edit removed — this
check does, by reading the properties file the wrapper itself reads.

The check validates the pin's **configuration**: its presence, its 64-hexadecimal shape, and an `-all` distribution
archive. Whether the value names the archive the URL points at is not decidable offline — the repository owns no
independent hash source — so that belongs to the update tool and to a cold-cache bootstrap.

Usage: `python3 scripts/verify-wrapper-checksum.py [--properties <path>]`

Exit status: 0 when the configuration is valid, 1 when it is not, naming each defect.
"""

import argparse
import re
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent

PROPERTIES = REPO / "gradle" / "wrapper" / "gradle-wrapper.properties"

CHECKSUM = "distributionSha256Sum"
DISTRIBUTION_URL = "distributionUrl"

HEX_64 = re.compile(r"^[0-9a-fA-F]{64}$")


def read_properties(path):
    """Reads a properties file into a mapping, unescaping the wrapper's URL colons.

    :param path: the properties file to read
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


def defects(entries):
    """Returns the configuration defects the given properties carry.

    :param entries: the properties file's keys and values
    :return: one message per defect, empty when the pin is valid
    """
    found = []
    checksum = entries.get(CHECKSUM)
    url = entries.get(DISTRIBUTION_URL, "")
    if checksum is None:
        found.append("{0} is missing: the wrapper accepts an unverified download".format(CHECKSUM))
    elif not HEX_64.match(checksum):
        found.append("{0} is not 64 hexadecimal characters: {1!r}".format(CHECKSUM, checksum))
    if not url:
        found.append("{0} is missing".format(DISTRIBUTION_URL))
    elif not url.endswith("-all.zip"):
        found.append("{0} does not name an -all distribution archive: {1!r}".format(DISTRIBUTION_URL, url))
    return found


def main():
    """Runs the check and reports its verdict."""
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--properties", type=Path, default=PROPERTIES, help="the wrapper properties file to check")
    arguments = parser.parse_args()

    if not arguments.properties.is_file():
        print("The wrapper properties file is missing: {0}".format(arguments.properties))
        return 1

    found = defects(read_properties(arguments.properties))
    if found:
        for message in found:
            print("The Gradle wrapper's distribution pin is invalid: {0}".format(message))
        return 1
    print("The Gradle wrapper's distribution pin is valid: {0}".format(arguments.properties))
    return 0


if __name__ == "__main__":
    sys.exit(main())
