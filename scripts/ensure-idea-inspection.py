#!/usr/bin/env python3
"""Ensure the test-tier naming inspection exists in the IntelliJ inspection profile.

Upserts the `NewClassNamingConvention` + `JUnitTestClassNamingConvention` block into
`.idea/inspectionProfiles/Project_Default.xml`, preserving every other inspection
and leaving unrelated content byte-for-byte untouched. Stdlib only.
"""

import re
import sys
from pathlib import Path

PROFILE_RELATIVE = Path(".idea/inspectionProfiles/Project_Default.xml")

CANONICAL_BLOCK = """    <inspection_tool class="NewClassNamingConvention" enabled="true" level="WARNING" enabled_by_default="true">
      <extension name="JUnitTestClassNamingConvention" enabled="true">
        <option name="m_regex" value="[A-Z][A-Za-z\\d]*(Test(s|Case)?|CT|E2E)|Test[A-Z][A-Za-z\\d]*|IT(.*)|(.*)IT(Case)?" />
        <option name="m_minLength" value="5" />
        <option name="m_maxLength" value="255" />
      </extension>
    </inspection_tool>"""

SKELETON_HEADER = '<?xml version="1.0" encoding="UTF-8"?>\n<component name="ProjectInspectionProfileManager">\n  <profile version="1.0">\n    <option name="myName" value="Project Default" />\n'
SKELETON_FOOTER = "  </profile>\n</component>\n"

TOOL_RE = re.compile(
    r'[ \t]*<inspection_tool class="NewClassNamingConvention"[^>]*>.*?</inspection_tool>',
    re.DOTALL,
)


def upsert(profile: Path) -> None:
    if not profile.exists():
        profile.parent.mkdir(parents=True, exist_ok=True)
        profile.write_text(SKELETON_HEADER + CANONICAL_BLOCK + "\n" + SKELETON_FOOTER, encoding="utf-8")
        print(f"created {profile}")
        return

    content = profile.read_text(encoding="utf-8")
    match = TOOL_RE.search(content)
    if match:
        if match.group(0) == CANONICAL_BLOCK:
            print(f"unchanged {profile}")
            return
        content = content[: match.start()] + CANONICAL_BLOCK + content[match.end() :]
        print(f"updated {profile}")
    else:
        closing = content.rfind("</profile>")
        if closing < 0:
            print(f"error: {profile} has no </profile> to insert into", file=sys.stderr)
            sys.exit(1)
        content = content[:closing].rstrip() + "\n" + CANONICAL_BLOCK + "\n  " + content[closing:]
        print(f"inserted into {profile}")
    profile.write_text(content, encoding="utf-8")


if __name__ == "__main__":
    upsert(PROFILE_RELATIVE)