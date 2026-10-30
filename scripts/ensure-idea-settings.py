#!/usr/bin/env python3
"""Ensure the repository's IntelliJ settings are present, and provide Python analysis for `scripts/`.

Upserts each `config/idea/*.xml` component into the corresponding `.idea/` file — replacing only that component and
preserving every other component — and upserts the test-tier naming inspection into the inspection profile, preserving
every other inspection. Reconciling (not create-if-absent) is what lets a re-run repair a configuration that has drifted
(never applied cleanly, or IntelliJ overwrote it).

The Python stage writes the module that makes IntelliJ analyze `scripts/*.py`: it resolves the interpreter the build
would run, reads the SDK name that an identified IntelliJ configuration registered for that interpreter, and reconciles
`.idea/python/scripts.iml` plus its `.idea/modules.xml` entry. That path sits outside `.idea/modules/`, the directory
IntelliJ's Gradle import owns and rebuilds, so a Gradle sync keeps the module. Every stage reports one outcome —
`CONFIGURED`, `SKIPPED`, `NEEDS_ACTION`, or `ERROR` — and the process exits non-zero only when a stage needs the
contributor's action or could not run. Stdlib only.
"""

import argparse
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Callable, List, Optional, Sequence, Tuple

REPO_ROOT = Path(__file__).resolve().parent.parent

COMPONENTS = [
    ("config/idea/palantir-java-format.xml", ".idea/palantir-java-format.xml", "PalantirJavaFormatSettings"),
    ("config/idea/ktfmt.xml", ".idea/ktfmt.xml", "KtfmtSettings"),
    ("config/idea/codeStyleConfig.xml", ".idea/codeStyles/codeStyleConfig.xml", "ProjectCodeStyleConfiguration"),
    ("config/idea/prettier.xml", ".idea/prettier.xml", "PrettierConfiguration"),
]

PROFILE_RELATIVE = ".idea/inspectionProfiles/Project_Default.xml"

CANONICAL_INSPECTION = (
    '    <inspection_tool class="NewClassNamingConvention" enabled="true" level="WARNING" enabled_by_default="true">\n'
    '      <extension name="JUnitTestClassNamingConvention" enabled="true">\n'
    '        <option name="m_regex" value="'
    "[A-Z][A-Za-z\\d]*(Test(s|Case)?|CT|E2E)|Test[A-Z][A-Za-z\\d]*|IT(.*)|(.*)IT(Case)?"
    '" />\n'
    '        <option name="m_minLength" value="5" />\n'
    '        <option name="m_maxLength" value="255" />\n'
    "      </extension>\n"
    "    </inspection_tool>"
)

SKELETON_HEADER = (
    '<?xml version="1.0" encoding="UTF-8"?>\n<component name="InspectionProjectProfileManager">\n'
    '  <profile version="1.0">\n    <option name="myName" value="Project Default" />\n'
)
SKELETON_FOOTER = "  </profile>\n</component>\n"

INSPECTION_RE = re.compile(
    r'[ \t]*<inspection_tool class="NewClassNamingConvention"[^>]*>.*?</inspection_tool>',
    re.DOTALL,
)

CONFIGURED = "CONFIGURED"
SKIPPED = "SKIPPED"
NEEDS_ACTION = "NEEDS_ACTION"
ERROR = "ERROR"

PYTHON_MINIMUM = (3, 12)
MODULE_RELATIVE = ".idea/python/scripts.iml"
LEGACY_MODULE_RELATIVE = ".idea/modules/scripts.iml"
MODULES_XML_RELATIVE = ".idea/modules.xml"
CONTENT_URL = "file://$MODULE_DIR$/../../scripts"
MODULE_FILEPATH = "$PROJECT_DIR$/" + MODULE_RELATIVE
LEGACY_MODULE_FILEPATH = "$PROJECT_DIR$/" + LEGACY_MODULE_RELATIVE
SDK_TABLE_RELATIVE = "options/jdk.table.xml"
IDE_CONFIG_PREFIXES = ("IntelliJIdea", "IdeaIC")
IDE_CONFIG_ROOTS = {
    "darwin": ["Library/Application Support/JetBrains"],
    "linux": [".config/JetBrains"],
    "win32": ["AppData/Roaming/JetBrains"],
}

INTERPRETER_PROGRAM = (
    "import sys; print(sys.executable); print('%d.%d.%d' % sys.version_info[:3]); "
    "print('environment' if sys.prefix != sys.base_prefix else 'base')"
)

MODULES_TEMPLATE = """<?xml version='1.0' encoding='utf-8'?>
<project version="4">
  <component name="ProjectModuleManager">
    <modules>
      <module fileurl="file://{filepath}" filepath="{filepath}" />
    </modules>
  </component>
</project>
"""


class MergeError(Exception):
    """A managed template, or the file it merges into, cannot be processed."""


def component_pattern(name: str) -> re.Pattern:
    """Return the regular expression matching one `<component name="...">` block."""
    return re.compile(r'[ \t]*<component name="%s"(?:[^>]*)>.*?</component>' % re.escape(name), re.DOTALL)


def ensure_component(template_rel: str, target_rel: str, name: str) -> str:
    """Upsert one managed component into its `.idea/` file and report what happened."""
    template = (REPO_ROOT / template_rel).read_text(encoding="utf-8")
    block_match = component_pattern(name).search(template)
    if block_match is None:
        raise MergeError(f'{template_rel} has no <component name="{name}">')
    block = block_match.group(0)
    target = REPO_ROOT / target_rel

    if not target.exists():
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(template if "<project" in template else block + "\n", encoding="utf-8")
        return "created"

    content = target.read_text(encoding="utf-8")
    existing = component_pattern(name).search(content)
    if existing is not None:
        if existing.group(0).strip() == block.strip():
            return "unchanged"
        target.write_text(content[: existing.start()] + block + content[existing.end() :], encoding="utf-8")
        return "restored"

    closing = content.rfind("</project>")
    if closing >= 0:
        updated = content[:closing] + block + "\n" + content[closing:]
    else:
        updated = content.rstrip() + "\n" + block + "\n"
    target.write_text(updated, encoding="utf-8")
    return "added"


def ensure_inspection() -> str:
    """Upsert the test-tier naming inspection into the project's inspection profile."""
    profile = REPO_ROOT / PROFILE_RELATIVE
    if not profile.exists():
        profile.parent.mkdir(parents=True, exist_ok=True)
        profile.write_text(SKELETON_HEADER + CANONICAL_INSPECTION + "\n" + SKELETON_FOOTER, encoding="utf-8")
        return "created"

    content = profile.read_text(encoding="utf-8")
    match = INSPECTION_RE.search(content)
    if match is not None:
        if match.group(0).strip() == CANONICAL_INSPECTION.strip():
            return "unchanged"
        profile.write_text(content[: match.start()] + CANONICAL_INSPECTION + content[match.end() :], encoding="utf-8")
        return "restored"

    closing = content.rfind("</profile>")
    if closing < 0:
        raise MergeError(f"{PROFILE_RELATIVE} has no </profile> to insert into")
    updated = content[:closing].rstrip() + "\n" + CANONICAL_INSPECTION + "\n  " + content[closing:]
    profile.write_text(updated, encoding="utf-8")
    return "added"


def probe_interpreter(working_dir: Path) -> Tuple[Path, str, bool]:
    """Run `python3` in `working_dir` and report the interpreter path it names for itself, its version, and whether it
    runs from a virtual environment.

    The path is the one the interpreter reports as `sys.executable`, left unresolved on purpose: its directory is the
    install the build selected, which a resolved path would replace with whatever symlink chain's target it follows. The
    environment flag is `sys.prefix != sys.base_prefix`, the interpreter's own report of the same fact.
    """
    executable = shutil.which("python3") or "python3"
    completed = subprocess.run(
        [executable, "-c", INTERPRETER_PROGRAM],
        cwd=str(working_dir),
        capture_output=True,
        text=True,
        check=True,
    )
    lines = completed.stdout.strip().splitlines()
    if len(lines) < 3:
        raise ValueError(f"python3 reported no interpreter: {completed.stdout!r}")
    return Path(lines[0].strip()), lines[1].strip(), lines[2].strip() == "environment"


def version_tuple(version: str) -> Tuple[int, ...]:
    """Return the numeric components of a dotted version string, ignoring any suffix."""
    parts: List[int] = []
    for segment in version.split("."):
        digits = re.match(r"\d+", segment)
        if digits is None:
            break
        parts.append(int(digits.group(0)))
    return tuple(parts)


def config_candidates(explicit: Optional[Path], roots: Sequence[Path]) -> List[Path]:
    """Return the IntelliJ configuration directories to consider, in a stable order."""
    if explicit is not None:
        return [explicit]
    found: List[Path] = []
    for root in roots:
        if not root.is_dir():
            continue
        for child in sorted(root.iterdir()):
            if child.is_dir() and child.name.startswith(IDE_CONFIG_PREFIXES):
                found.append(child)
    return found


def default_roots() -> List[Path]:
    """Return this platform's IntelliJ configuration roots."""
    home = Path.home()
    return [home / relative for relative in IDE_CONFIG_ROOTS.get(sys.platform, [])]


def expand_home(value: str) -> Path:
    """Expand the `$USER_HOME$` macro and a leading `~` in an IntelliJ path value, without resolving it."""
    if value.startswith("$USER_HOME$"):
        value = str(Path.home()) + value[len("$USER_HOME$") :]
    if value.startswith("~/") or value == "~":
        value = str(Path.home()) + value[1:]
    return Path(value)


def install_directory(interpreter: Path) -> Path:
    """Return the directory an interpreter is installed in, with any symlinked directory resolved.

    A virtual environment's interpreter is a symlink chain to the base interpreter it was created from, so resolving the
    file erases which install it belongs to; the directory it sits in does not.
    """
    return interpreter.parent.resolve()


def is_the_same_install(entry: Path, interpreter: Path, in_environment: bool) -> bool:
    """Report whether an SDK entry's interpreter is the installation the probe ran.

    The binaries must be the same, and the entry must sit in the directory the probe's path names. Outside a virtual
    environment an entry naming the resolved target of that path also matches, since it is the same installation. That
    allowance is withheld inside one: a virtual environment's interpreter resolves to the base binary, so accepting the
    base installation there would configure the wrong environment.
    """
    binary = interpreter.resolve()
    if entry.resolve() != binary:
        return False
    if install_directory(entry) == install_directory(interpreter):
        return True
    return not in_environment and install_directory(entry) == binary.parent


def sdk_entries(config_dir: Path) -> List[Tuple[str, Path]]:
    """Return the Python SDK entries of one configuration's table as (name, interpreter) pairs."""
    table = config_dir / SDK_TABLE_RELATIVE
    try:
        root = ET.parse(table).getroot()
    except (OSError, ET.ParseError) as error:
        raise MergeError(f"cannot read {table}: {error}") from error
    entries: List[Tuple[str, Path]] = []
    for jdk in root.iter("jdk"):
        type_element = jdk.find("type")
        name_element = jdk.find("name")
        home_element = jdk.find("homePath")
        if type_element is None or name_element is None or home_element is None:
            continue
        if type_element.get("value") != "Python SDK":
            continue
        entries.append((name_element.get("value", ""), expand_home(home_element.get("value", ""))))
    return entries


def render(tree: ET.ElementTree) -> bytes:
    """Serialize a parsed project file the way this script writes it."""
    ET.indent(tree, space="  ")
    return ET.tostring(tree.getroot(), encoding="utf-8", xml_declaration=True) + b"\n"


def write_if_changed(path: Path, payload: bytes) -> bool:
    """Write `payload` unless the file already holds exactly those bytes; report whether it was written."""
    if path.is_file() and path.read_bytes() == payload:
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(payload)
    return True


def ensure_module_file(sdk_name: str) -> bool:
    """Reconcile the managed Python module file for the given interpreter; report whether the tree changed."""
    path = REPO_ROOT / MODULE_RELATIVE
    if path.is_file():
        tree = ET.parse(path)
        root = tree.getroot()
    else:
        root = ET.Element("module", {"type": "PYTHON_MODULE", "version": "4"})
        tree = ET.ElementTree(root)
    root.set("type", "PYTHON_MODULE")
    component = None
    for candidate in root.findall("component"):
        if candidate.get("name") == "NewModuleRootManager":
            component = candidate
    if component is None:
        component = ET.SubElement(
            root, "component", {"name": "NewModuleRootManager", "inherit-compiler-output": "true"}
        )
    if not any(child.tag == "exclude-output" for child in component):
        component.insert(0, ET.Element("exclude-output"))
    contents = component.findall("content")
    for stale in contents[1:]:
        component.remove(stale)
    if contents:
        contents[0].set("url", CONTENT_URL)
    else:
        position = 0
        for index, child in enumerate(list(component)):
            if child.tag == "exclude-output":
                position = index + 1
        component.insert(position, ET.Element("content", {"url": CONTENT_URL}))
    jdk_entries = [entry for entry in component.findall("orderEntry") if entry.get("type") == "jdk"]
    for stale in jdk_entries[1:]:
        component.remove(stale)
    jdk = jdk_entries[0] if jdk_entries else ET.SubElement(component, "orderEntry", {"type": "jdk"})
    jdk.set("jdkName", sdk_name)
    jdk.set("jdkType", "Python SDK")
    if not any(entry.get("type") == "sourceFolder" for entry in component.findall("orderEntry")):
        ET.SubElement(component, "orderEntry", {"type": "sourceFolder", "forTests": "false"})
    written = write_if_changed(path, render(tree))
    legacy = REPO_ROOT / LEGACY_MODULE_RELATIVE
    if legacy.is_file():
        legacy.unlink()
        return True
    return written


def ensure_module_registration() -> bool:
    """Reconcile this module's entry in `.idea/modules.xml`; report whether it was written."""
    path = REPO_ROOT / MODULES_XML_RELATIVE
    if not path.is_file():
        payload = MODULES_TEMPLATE.format(filepath=MODULE_FILEPATH).encode("utf-8")
        return write_if_changed(path, payload)
    tree = ET.parse(path)
    root = tree.getroot()
    manager = None
    for candidate in root.findall("component"):
        if candidate.get("name") == "ProjectModuleManager":
            manager = candidate
    if manager is None:
        manager = ET.SubElement(root, "component", {"name": "ProjectModuleManager"})
    modules = manager.find("modules")
    if modules is None:
        modules = ET.SubElement(manager, "modules")
    for stale in [
        element
        for element in modules.findall("module")
        if element.get("filepath") == LEGACY_MODULE_FILEPATH
    ]:
        modules.remove(stale)
    registered = [element for element in modules.findall("module") if element.get("filepath") == MODULE_FILEPATH]
    for stale in registered[1:]:
        modules.remove(stale)
    if registered:
        entry = registered[0]
    else:
        entry = ET.SubElement(modules, "module")
    entry.set("fileurl", "file://" + MODULE_FILEPATH)
    entry.set("filepath", MODULE_FILEPATH)
    return write_if_changed(path, render(tree))


def python_stage(
    project_dir: Path,
    idea_config_dir: Optional[Path] = None,
    roots: Optional[Sequence[Path]] = None,
    probe: Optional[Callable[[Path], Tuple[Path, str, bool]]] = None,
) -> Tuple[str, str]:
    """Reconcile the Python analysis module and report one outcome with its message."""
    probe = probe or probe_interpreter
    try:
        interpreter, version, in_environment = probe(project_dir)
    except (OSError, subprocess.SubprocessError, ValueError) as error:
        return ERROR, f"cannot run python3: {error}"
    if version_tuple(version) < PYTHON_MINIMUM:
        required = ".".join(str(part) for part in PYTHON_MINIMUM)
        return ERROR, f"python3 at {interpreter} is version {version}, below the required {required}"

    searched = list(roots) if roots is not None else default_roots()
    candidates = config_candidates(idea_config_dir, searched)
    if idea_config_dir is not None and not (idea_config_dir / SDK_TABLE_RELATIVE).is_file():
        return ERROR, f"{idea_config_dir} holds no {SDK_TABLE_RELATIVE}"
    if not candidates:
        looked_in = ", ".join(str(root) for root in searched)
        return SKIPPED, (
            f"no IntelliJ configuration found (looked in {looked_in or 'this platform'}); "
            f"interpreter is {interpreter} ({version})"
        )
    readable = [candidate for candidate in candidates if (candidate / SDK_TABLE_RELATIVE).is_file()]
    if not readable:
        listed = "\n".join(f"    {candidate}" for candidate in candidates)
        return NEEDS_ACTION, (
            f"the IntelliJ configuration has no {SDK_TABLE_RELATIVE} yet.\n"
            "\n"
            "  Open IntelliJ once so it writes one of these:\n"
            f"{listed}\n"
            "\n"
            "  Then register this tree's interpreter in File -> Project Structure -> SDKs\n"
            "  (Add SDK -> Python SDK), save, and re-run ./scripts/setup-idea.sh:\n"
            f"    {interpreter} ({version})"
        )

    matches: List[Tuple[Path, str]] = []
    registered: List[Tuple[Path, str, Path]] = []
    for config_dir in readable:
        try:
            entries = sdk_entries(config_dir)
        except MergeError as error:
            return ERROR, str(error)
        registered.extend((config_dir, name, home) for name, home in entries)
        for name, home in entries:
            if is_the_same_install(home, interpreter, in_environment):
                matches.append((config_dir, name))
    if not matches:
        if registered:
            inventory = "\n".join(
                f"    {config_dir.name}:\n      {name} -> {home}" for config_dir, name, home in registered
            )
        else:
            inventory = "    none"
        searched = "\n".join(f"    {candidate}" for candidate in readable)
        return NEEDS_ACTION, (
            "this tree's interpreter is not registered as a Python SDK.\n"
            "\n"
            "  Register it in IntelliJ:\n"
            f"    {interpreter} ({version})\n"
            "    File -> Project Structure -> SDKs -> Add SDK -> Python SDK\n"
            "    Save the IDE settings, then re-run ./scripts/setup-idea.sh.\n"
            "\n"
            "  Or run this tree on an interpreter that is already registered:\n"
            "    If pyenv supplies one: pyenv local <installed-version>\n"
            "    Then re-run ./scripts/setup-idea.sh.\n"
            "\n"
            "  Registered Python SDKs:\n"
            f"{inventory}\n"
            "\n"
            "  Searched configurations:\n"
            f"{searched}"
        )
    if len(matches) > 1:
        listed = "\n".join(f"    {name} in {config_dir}" for config_dir, name in matches)
        return NEEDS_ACTION, (
            "several Python SDK entries match this tree's interpreter.\n"
            "\n"
            f"  {interpreter} ({version})\n"
            "\n"
            "  Keep one match:\n"
            "    Several configurations qualify: select one with --idea-config-dir <path>.\n"
            "    Several entries qualify inside one configuration: delete all but one in\n"
            "    File -> Project Structure -> SDKs, then re-run ./scripts/setup-idea.sh.\n"
            "\n"
            "  Matching SDKs:\n"
            f"{listed}"
        )

    config_dir, sdk_name = matches[0]
    try:
        written = ensure_module_file(sdk_name)
        registered = ensure_module_registration()
    except (MergeError, OSError, ET.ParseError) as error:
        return ERROR, f"cannot reconcile the Python module: {error}"
    changes = [what for what, happened in (("module", written), ("registration", registered)) if happened]
    detail = "wrote " + " and ".join(changes) if changes else "already in place"
    return CONFIGURED, f"{detail}; interpreter {interpreter} ({version}), SDK {sdk_name!r} from {config_dir}"


def main(argv: Optional[Sequence[str]] = None) -> int:
    """Run every stage, report each one, and return the aggregate exit status."""
    parser = argparse.ArgumentParser(
        description="Merge the repository's IntelliJ settings and reconcile Python analysis."
    )
    parser.add_argument(
        "--idea-config-dir",
        type=Path,
        default=None,
        help="the IntelliJ configuration directory to read",
    )
    arguments = parser.parse_args(argv)

    status = 0
    try:
        reports = [ensure_component(*component) for component in COMPONENTS]
        reports.append(ensure_inspection())
        print("Settings: " + CONFIGURED + " (" + ", ".join(reports) + ")")
    except (MergeError, OSError) as error:
        print(f"Settings: {ERROR} — {error}", file=sys.stderr)
        status = 1

    outcome, message = python_stage(REPO_ROOT, arguments.idea_config_dir)
    print(f"Python: {outcome} — {message}")
    if outcome in (NEEDS_ACTION, ERROR):
        status = 1
    return status


if __name__ == "__main__":
    sys.exit(main())
