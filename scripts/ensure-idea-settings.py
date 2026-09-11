#!/usr/bin/env python3
"""Ensure the repository's IntelliJ settings are present, merging them into IntelliJ's files.

Upserts each `config/idea/*.xml` component into the corresponding `.idea/` file — replacing only that
component and preserving every other component — and upserts the test-tier naming inspection into the
inspection profile, preserving every other inspection. Reconciling (not create-if-absent) is what lets
a re-run repair a configuration that has drifted (never applied cleanly, or IntelliJ overwrote it). Stdlib only.
"""

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

COMPONENTS = [
    ("config/idea/palantir-java-format.xml", ".idea/palantir-java-format.xml", "PalantirJavaFormatSettings"),
    ("config/idea/ktfmt.xml", ".idea/ktfmt.xml", "KtfmtSettings"),
    ("config/idea/codeStyleConfig.xml", ".idea/codeStyles/codeStyleConfig.xml", "ProjectCodeStyleConfiguration"),
    ("config/idea/prettier.xml", ".idea/prettier.xml", "PrettierConfiguration"),
]

PROFILE_RELATIVE = ".idea/inspectionProfiles/Project_Default.xml"

CANONICAL_INSPECTION = """    <inspection_tool class="NewClassNamingConvention" enabled="true" level="WARNING" enabled_by_default="true">
      <extension name="JUnitTestClassNamingConvention" enabled="true">
        <option name="m_regex" value="[A-Z][A-Za-z\\d]*(Test(s|Case)?|CT|E2E)|Test[A-Z][A-Za-z\\d]*|IT(.*)|(.*)IT(Case)?" />
        <option name="m_minLength" value="5" />
        <option name="m_maxLength" value="255" />
      </extension>
    </inspection_tool>"""

SKELETON_HEADER = (
    '<?xml version="1.0" encoding="UTF-8"?>\n<component name="InspectionProjectProfileManager">\n'
    '  <profile version="1.0">\n    <option name="myName" value="Project Default" />\n'
)
SKELETON_FOOTER = "  </profile>\n</component>\n"

INSPECTION_RE = re.compile(
    r'[ \t]*<inspection_tool class="NewClassNamingConvention"[^>]*>.*?</inspection_tool>',
    re.DOTALL,
)


def component_pattern(name: str) -> re.Pattern:
    return re.compile(r'[ \t]*<component name="%s"(?:[^>]*)>.*?</component>' % re.escape(name), re.DOTALL)


def ensure_component(template_rel: str, target_rel: str, name: str) -> None:
    template = (REPO_ROOT / template_rel).read_text(encoding="utf-8")
    block_match = component_pattern(name).search(template)
    if block_match is None:
        print(f"error: {template_rel} has no <component name=\"{name}\">", file=sys.stderr)
        sys.exit(1)
    block = block_match.group(0)
    target = REPO_ROOT / target_rel

    if not target.exists():
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(template if "<project" in template else block + "\n", encoding="utf-8")
        print(f"created   {target_rel}")
        return

    content = target.read_text(encoding="utf-8")
    existing = component_pattern(name).search(content)
    if existing is not None:
        if existing.group(0).strip() == block.strip():
            print(f"unchanged {target_rel}")
            return
        content = content[: existing.start()] + block + content[existing.end() :]
        target.write_text(content, encoding="utf-8")
        print(f"restored  {target_rel}")
        return

    closing = content.rfind("</project>")
    if closing >= 0:
        content = content[:closing] + block + "\n" + content[closing:]
    else:
        content = content.rstrip() + "\n" + block + "\n"
    target.write_text(content, encoding="utf-8")
    print(f"added     {target_rel}")


def ensure_inspection() -> None:
    profile = REPO_ROOT / PROFILE_RELATIVE
    if not profile.exists():
        profile.parent.mkdir(parents=True, exist_ok=True)
        profile.write_text(SKELETON_HEADER + CANONICAL_INSPECTION + "\n" + SKELETON_FOOTER, encoding="utf-8")
        print(f"created   {PROFILE_RELATIVE}")
        return

    content = profile.read_text(encoding="utf-8")
    match = INSPECTION_RE.search(content)
    if match is not None:
        if match.group(0).strip() == CANONICAL_INSPECTION.strip():
            print(f"unchanged {PROFILE_RELATIVE}")
            return
        content = content[: match.start()] + CANONICAL_INSPECTION + content[match.end() :]
        profile.write_text(content, encoding="utf-8")
        print(f"restored  {PROFILE_RELATIVE}")
        return

    closing = content.rfind("</profile>")
    if closing < 0:
        print(f"error: {PROFILE_RELATIVE} has no </profile> to insert into", file=sys.stderr)
        sys.exit(1)
    content = content[:closing].rstrip() + "\n" + CANONICAL_INSPECTION + "\n  " + content[closing:]
    profile.write_text(content, encoding="utf-8")
    print(f"added     {PROFILE_RELATIVE}")


if __name__ == "__main__":
    for template_rel, target_rel, component_name in COMPONENTS:
        ensure_component(template_rel, target_rel, component_name)
    ensure_inspection()
