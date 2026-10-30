#!/usr/bin/env python3
"""Tests for the IntelliJ settings helper and its Python-analysis stage. Stdlib only.

Run with `python3 scripts/test-ensure-idea-settings.py`. The helper's kebab-case filename is not an importable
identifier, so it is loaded by path, the way `scripts/test-review-routing.py` loads its tool.

Each case builds a temporary repository root and a temporary IntelliJ configuration, so no case touches this checkout's
`.idea/` or the real SDK table. The interpreter probe is replaced by a fixed report except where a case runs the real
`python3`, and every outcome the contract defines has a case that reaches it.
"""

import contextlib
import importlib.util
import io
import re
import shutil
import subprocess
import tempfile
import unittest
import unittest.mock
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
TOOL_PATH = Path(__file__).resolve().parent / "ensure-idea-settings.py"
_spec = importlib.util.spec_from_file_location("ensure_idea_settings", TOOL_PATH)
ensure = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(ensure)

INTERPRETER = Path.home() / ".local" / "share" / "example" / "bin" / "python3"
VERSION = "3.12.16"


def sdk_table(*entries: tuple) -> str:
    """Render an `options/jdk.table.xml` holding one Python SDK per (name, home) pair."""
    rendered = "".join(
        '<jdk version="2">'
        f'<name value="{name}" />'
        '<type value="Python SDK" />'
        '<version value="Python 3.12.16" />'
        f'<homePath value="{home}" />'
        "</jdk>"
        for name, home in entries
    )
    return f'<?xml version="1.0" encoding="UTF-8"?><application>{rendered}</application>'


def module_xml(*filepaths: str) -> str:
    """Render a `modules.xml` registering one module per file path."""
    entries = "".join(
        f'<module fileurl="file://{filepath}" filepath="{filepath}" />' for filepath in filepaths
    )
    return (
        '<?xml version="1.0" encoding="UTF-8"?>'
        '<project version="4"><component name="ProjectModuleManager">'
        f"<modules>{entries}</modules></component></project>"
    )


class SettingsStageTests(unittest.TestCase):
    """Cases for the settings merge and the Python-analysis stage."""

    def setUp(self) -> None:
        """Build a temporary repository root holding the committed settings templates."""
        self.tmp = Path(tempfile.mkdtemp())
        self.addCleanup(shutil.rmtree, self.tmp, True)
        self.root = self.tmp
        shutil.copytree(REPO / "config" / "idea", self.root / "config" / "idea")
        self._saved_root = ensure.REPO_ROOT
        ensure.REPO_ROOT = self.root
        self.addCleanup(setattr, ensure, "REPO_ROOT", self._saved_root)
        self.config = self.tmp / "IntelliJIdea2026.2"
        self.config.mkdir()

    def write_table(self, *entries: tuple, config: Path | None = None) -> Path:
        """Write an SDK table for the given (name, home) pairs and return its configuration directory."""
        target = config or self.config
        options = target / "options"
        options.mkdir(parents=True, exist_ok=True)
        (options / "jdk.table.xml").write_text(sdk_table(*entries), encoding="utf-8")
        return target

    def python_install(self) -> tuple[Path, Path]:
        """Build a Python install and a venv wrapping it; return the install directory and the venv's interpreter."""
        install = self.tmp / "pythons" / "3.12.1"
        binaries = install / "bin"
        binaries.mkdir(parents=True)
        (binaries / "python3.12").write_text("", encoding="utf-8")
        (binaries / "python3").symlink_to("python3.12")
        environment = install / "envs" / "example"
        (environment / "bin").mkdir(parents=True)
        (environment / "bin" / "python").symlink_to("python3.12")
        (environment / "bin" / "python3.12").symlink_to(binaries / "python3.12")
        versions = self.tmp / "pythons" / "versions"
        versions.mkdir(parents=True)
        (versions / "example").symlink_to(environment)
        return install, versions / "example" / "bin" / "python"

    def probe(self, working_dir: Path) -> tuple:
        """Report a fixed interpreter, standing in for the real `python3` run."""
        return INTERPRETER, VERSION, False

    def stage(self, **overrides) -> tuple:
        """Run the Python stage against the temporary root with fixed defaults."""
        arguments = {
            "idea_config_dir": self.config,
            "roots": [self.tmp / "no-roots"],
            "probe": self.probe,
        }
        arguments.update(overrides)
        return ensure.python_stage(self.root, **arguments)

    def main_status(self, *argv: str) -> int:
        """Run the module's entry point, discarding its report."""
        with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            return ensure.main(list(argv))

    def test_fresh_tree_gains_the_module_and_its_registration(self) -> None:
        """A tree with no Python configuration gains both files the analysis needs."""
        self.write_table(("Test Python", str(INTERPRETER)))

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('type="PYTHON_MODULE"', iml)
        self.assertIn(ensure.CONTENT_URL, iml)
        self.assertIn('jdkName="Test Python"', iml)
        self.assertIn('jdkType="Python SDK"', iml)
        self.assertIn("<exclude-output />", iml)
        modules = (self.root / ensure.MODULES_XML_RELATIVE).read_text(encoding="utf-8")
        self.assertEqual(1, modules.count(f'filepath="{ensure.MODULE_FILEPATH}"'), modules)

    def test_a_module_at_the_gradle_owned_path_is_relocated(self) -> None:
        """A module left where IntelliJ's Gradle import owns it is moved to the path a sync keeps, and its entry too."""
        self.write_table(("Test Python", str(INTERPRETER)))
        legacy = self.root / ensure.LEGACY_MODULE_RELATIVE
        legacy.parent.mkdir(parents=True)
        legacy.write_text("<module />\n", encoding="utf-8")
        modules = self.root / ensure.MODULES_XML_RELATIVE
        modules.parent.mkdir(parents=True, exist_ok=True)
        modules.write_text(module_xml(ensure.LEGACY_MODULE_FILEPATH), encoding="utf-8")

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        self.assertFalse(legacy.exists())
        self.assertTrue((self.root / ensure.MODULE_RELATIVE).is_file())
        updated = modules.read_text(encoding="utf-8")
        self.assertNotIn(ensure.LEGACY_MODULE_FILEPATH, updated)
        self.assertEqual(1, updated.count(f'filepath="{ensure.MODULE_FILEPATH}"'), updated)

    def test_second_run_writes_nothing(self) -> None:
        """A second run over the first run's output leaves both files byte-identical."""
        self.write_table(("Test Python", str(INTERPRETER)))
        self.stage()
        iml = self.root / ensure.MODULE_RELATIVE
        modules = self.root / ensure.MODULES_XML_RELATIVE
        before = (iml.read_bytes(), modules.read_bytes())

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        self.assertEqual(before, (iml.read_bytes(), modules.read_bytes()))
        self.assertIn("already in place", message)

    def test_existing_module_keeps_a_foreign_component(self) -> None:
        """A component another plugin wrote into the module file survives the reconcile."""
        self.write_table(("Test Python", str(INTERPRETER)))
        iml = self.root / ensure.MODULE_RELATIVE
        iml.parent.mkdir(parents=True)
        iml.write_text(
            '<?xml version="1.0" encoding="UTF-8"?>\n'
            '<module type="PYTHON_MODULE" version="4">'
            '<component name="CheckStyle-IDEA-Module"><option name="keep" value="me" /></component>'
            '<component name="NewModuleRootManager"><content url="file://$MODULE_DIR$/../../elsewhere" /></component>'
            "</module>\n",
            encoding="utf-8",
        )

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        updated = iml.read_text(encoding="utf-8")
        self.assertIn("CheckStyle-IDEA-Module", updated)
        self.assertIn('value="me"', updated)
        self.assertIn(ensure.CONTENT_URL, updated)
        self.assertNotIn("elsewhere", updated)

    def test_existing_module_list_keeps_its_other_modules(self) -> None:
        """Registering the managed module leaves the tree's other modules in place."""
        self.write_table(("Test Python", str(INTERPRETER)))
        modules = self.root / ensure.MODULES_XML_RELATIVE
        modules.parent.mkdir(parents=True, exist_ok=True)
        modules.write_text(
            module_xml("$PROJECT_DIR$/a.iml", "$PROJECT_DIR$/b.iml"), encoding="utf-8"
        )

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        updated = modules.read_text(encoding="utf-8")
        self.assertIn("$PROJECT_DIR$/a.iml", updated)
        self.assertIn("$PROJECT_DIR$/b.iml", updated)
        self.assertEqual(1, updated.count(f'filepath="{ensure.MODULE_FILEPATH}"'), updated)

    def test_a_drifted_interpreter_reference_is_restored(self) -> None:
        """An SDK name changed by hand is restored to the registered entry's own name."""
        self.write_table(("Test Python", str(INTERPRETER)))
        self.stage()
        iml = self.root / ensure.MODULE_RELATIVE
        iml.write_text(iml.read_text(encoding="utf-8").replace("Test Python", "Something Else"), encoding="utf-8")

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        self.assertIn('jdkName="Test Python"', iml.read_text(encoding="utf-8"))
        self.assertNotIn("Something Else", iml.read_text(encoding="utf-8"))

    def test_a_removed_registration_is_restored(self) -> None:
        """A registration dropped from the module list is put back exactly once."""
        self.write_table(("Test Python", str(INTERPRETER)))
        self.stage()
        modules = self.root / ensure.MODULES_XML_RELATIVE
        modules.write_text(module_xml("$PROJECT_DIR$/other.iml"), encoding="utf-8")

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        updated = modules.read_text(encoding="utf-8")
        self.assertIn("$PROJECT_DIR$/other.iml", updated)
        self.assertEqual(1, updated.count(f'filepath="{ensure.MODULE_FILEPATH}"'), updated)

    def test_both_home_spellings_match_the_interpreter(self) -> None:
        """`$USER_HOME$` and `~` spellings resolve to the same interpreter as an absolute path."""
        home = str(Path.home())
        first = self.tmp / "products" / "IntelliJIdea2025.3"
        second = self.tmp / "products" / "IntelliJIdea2025.2"
        first.mkdir(parents=True)
        second.mkdir(parents=True)
        self.assertTrue(str(INTERPRETER).startswith(home), str(INTERPRETER))
        relative = str(INTERPRETER)[len(home) :]

        self.write_table(("Home Spelling", f"$USER_HOME${relative}"), config=first)
        self.write_table(("Tilde Spelling", f"~{relative}"), config=second)

        for config, expected in ((first, "Home Spelling"), (second, "Tilde Spelling")):
            with self.subTest(config=config.name):
                outcome, message = self.stage(idea_config_dir=config)
                self.assertEqual(ensure.CONFIGURED, outcome, message)
                self.assertIn(f'jdkName="{expected}"', (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8"))

    def test_a_virtual_environment_sdk_does_not_match_the_interpreter_it_wraps(self) -> None:
        """A venv's SDK resolves to its base interpreter, so that interpreter must not match the SDK."""
        install, entry = self.python_install()
        probe = install / "bin" / "python3"
        self.assertEqual(entry.resolve(), probe.resolve(), "the fixture must reproduce the aliasing")
        self.write_table(("Virtual Environment", str(entry)))

        outcome, message = self.stage(probe=lambda _working: (probe, VERSION, False))

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertIn("is not registered as a Python SDK", message)
        self.assertNotIn("several Python SDK entries", message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())

    def test_a_virtual_environment_entry_leaves_the_base_interpreter_unambiguous(self) -> None:
        """A venv entry beside the base install's entry leaves exactly one match: the base install's."""
        install, entry = self.python_install()
        probe = install / "bin" / "python3"
        self.write_table(("Virtual Environment", str(entry)), ("Base Python", str(probe)))

        outcome, message = self.stage(probe=lambda _working: (probe, VERSION, False))

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        self.assertIn("Base Python", message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('jdkName="Base Python"', iml)

    def test_an_entry_symlinked_to_a_sibling_interpreter_matches(self) -> None:
        """`bin/python3` and `bin/python3.12` are one install, so an entry naming either matches the other."""
        install, _entry = self.python_install()
        self.write_table(("Pyenv Python", str(install / "bin" / "python3")))

        outcome, message = self.stage(probe=lambda _working: (install / "bin" / "python3.12", VERSION, False))

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('jdkName="Pyenv Python"', iml)

    def test_a_sibling_interpreter_in_the_same_directory_does_not_match(self) -> None:
        """A different interpreter installed beside the probe's is not the probe's install, though its directory is."""
        install, _entry = self.python_install()
        probe = (install / "bin" / "python3.12").resolve()
        sibling = install / "bin" / "python3.13"
        sibling.write_text("", encoding="utf-8")
        self.assertEqual(sibling.parent.resolve(), probe.parent, "the fixture must share the directory")
        self.assertNotEqual(sibling.resolve(), probe)
        self.write_table(("Sibling Python", str(sibling)))

        outcome, message = self.stage(probe=lambda _working: (probe, VERSION, False))

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())

    def test_an_entry_spelled_through_a_symlinked_prefix_matches(self) -> None:
        """An install reached through a symlinked prefix is the same install as the probe's real path."""
        real = self.tmp / "pythons" / "real"
        binaries = real / "3.12.1" / "bin"
        binaries.mkdir(parents=True)
        (binaries / "python3.12").write_text("", encoding="utf-8")
        prefix = self.tmp / "pythons" / "prefix"
        prefix.symlink_to(real, target_is_directory=True)
        self.write_table(("Prefix Python", str(prefix / "3.12.1" / "bin" / "python3.12")))

        outcome, message = self.stage(probe=lambda _working: (binaries / "python3.12", VERSION, False))

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('jdkName="Prefix Python"', iml)

    def test_an_entry_naming_the_target_of_a_link_matches_a_probe_naming_the_link(self) -> None:
        """Registering the resolved target is the same install as running the link that points at it."""
        install = self.tmp / "pythons" / "homebrew"
        binaries = install / "bin"
        binaries.mkdir(parents=True)
        cellar = install / "Cellar" / "python@3.13" / "3.13.18" / "bin"
        cellar.mkdir(parents=True)
        target = cellar / "python3.13"
        target.write_text("", encoding="utf-8")
        invoked = binaries / "python3.13"
        invoked.symlink_to(target)
        self.write_table(("Homebrew Target", str(target)))

        outcome, message = self.stage(probe=lambda _working: (invoked, VERSION, False))

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('jdkName="Homebrew Target"', iml)

    def test_the_interpreter_a_virtual_environment_runs_matches_its_own_sdk(self) -> None:
        """A venv probed through its own interpreter matches its own SDK, though both resolve to the base binary."""
        _install, entry = self.python_install()
        self.write_table(("Virtual Environment", str(entry)))

        outcome, message = self.stage(probe=lambda _working: (entry, VERSION, True))

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('jdkName="Virtual Environment"', iml)

    def test_a_virtual_environment_probe_does_not_match_the_base_install_s_sdk(self) -> None:
        """Running from a venv does not select the base installation whose binary it resolves to."""
        install, entry = self.python_install()
        base = install / "bin" / "python3.12"
        self.assertEqual(entry.resolve(), base.resolve(), "the fixture must reproduce the aliasing")
        self.write_table(("Base Python", str(base)))

        outcome, message = self.stage(probe=lambda _working: (entry, VERSION, True))

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertNotIn("several Python SDK entries", message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())

    def test_a_virtual_environment_probe_prefers_its_own_entry(self) -> None:
        """With both SDKs registered, a venv probe matches its own entry and not the base installation's."""
        install, entry = self.python_install()
        self.write_table(("Virtual Environment", str(entry)), ("Base Python", str(install / "bin" / "python3.12")))

        outcome, message = self.stage(probe=lambda _working: (entry, VERSION, True))

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('jdkName="Virtual Environment"', iml)

    def test_an_entry_in_the_directory_the_interpreter_was_run_from_matches(self) -> None:
        """A Homebrew-shaped entry matches: the invoked path is in `bin`, its target lives outside that directory."""
        install = self.tmp / "pythons" / "homebrew"
        binaries = install / "bin"
        binaries.mkdir(parents=True)
        cellar = install / "Cellar" / "python@3.13" / "3.13.18" / "bin"
        cellar.mkdir(parents=True)
        (cellar / "python3.13").write_text("", encoding="utf-8")
        entry = binaries / "python3.13"
        entry.symlink_to(cellar / "python3.13")
        self.assertNotEqual(entry.parent, entry.resolve().parent, "the fixture must leave the target outside bin")
        self.write_table(("Homebrew Python", str(entry)))

        outcome, message = self.stage(probe=lambda _working: (entry, VERSION, False))

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = (self.root / ensure.MODULE_RELATIVE).read_text(encoding="utf-8")
        self.assertIn('jdkName="Homebrew Python"', iml)

    def test_a_name_needing_escaping_is_written_as_valid_xml(self) -> None:
        """An SDK name with `&` and `"` produces a file that parses and keeps the name verbatim."""
        awkward = 'A & B "quoted"'
        self.write_table((awkward.replace("&", "&amp;").replace('"', "&quot;"), str(INTERPRETER)))

        outcome, message = self.stage()

        self.assertEqual(ensure.CONFIGURED, outcome, message)
        iml = self.root / ensure.MODULE_RELATIVE
        parsed = __import__("xml.etree.ElementTree", fromlist=["ElementTree"]).parse(iml)
        names = [element.get("jdkName") for element in parsed.getroot().iter("orderEntry")]
        self.assertIn(awkward, names)

    def test_a_malformed_module_file_is_an_error(self) -> None:
        """A damaged module file reports the Python stage as an error instead of raising."""
        self.write_table(("Test Python", str(INTERPRETER)))
        iml = self.root / ensure.MODULE_RELATIVE
        iml.parent.mkdir(parents=True)
        iml.write_text("<module><unclosed>", encoding="utf-8")

        outcome, message = self.stage()

        self.assertEqual(ensure.ERROR, outcome, message)
        self.assertIn("cannot reconcile", message)

    def test_the_floor_matches_the_doctors_declaration(self) -> None:
        """The interpreter floor this stage enforces is the one the doctor declares."""
        doctor = (REPO / "scripts" / "doctor.sh").read_text(encoding="utf-8")
        row = re.search(r"^python3\|required\|[^\n]*\|--version\|([0-9.]+)$", doctor, re.MULTILINE)
        self.assertIsNotNone(row, "the doctor declares no python3 floor")
        declared = tuple(int(part) for part in row.group(1).split("."))
        self.assertEqual(declared, ensure.PYTHON_MINIMUM)

    def test_an_unmatched_interpreter_needs_action_and_writes_nothing(self) -> None:
        """A table holding another interpreter's SDK reports the action and writes no module."""
        self.write_table(("Other Python", "/opt/other/python3"))

        outcome, message = self.stage()

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertIn(str(INTERPRETER), message)
        self.assertIn("Register it in IntelliJ", message)
        self.assertIn("Or run this tree on an interpreter that is already registered", message)
        self.assertIn("pyenv local", message)
        self.assertIn("Registered Python SDKs", message)
        self.assertIn("Other Python -> /opt/other/python3", message)
        self.assertIn("File -> Project Structure -> SDKs -> Add SDK -> Python SDK", message)
        self.assertLess(
            message.index("Register it in IntelliJ"),
            message.index("Registered Python SDKs"),
            "the remedy must come before the inventory",
        )
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())
        self.assertEqual(1, self.main_status("--idea-config-dir", str(self.config)))

    def test_several_matching_entries_need_action(self) -> None:
        """Two entries for one interpreter are ambiguous rather than silently ordered."""
        self.write_table(("Test Python", str(INTERPRETER)), ("Duplicate Python", str(INTERPRETER)))

        outcome, message = self.stage()

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertIn("several Python SDK entries", message)
        self.assertIn("Keep one match", message)
        self.assertIn("--idea-config-dir", message)
        self.assertIn("delete all but one", message)
        self.assertIn("Matching SDKs", message)
        self.assertLess(message.index("Keep one match"), message.index("Matching SDKs"))
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())

    def test_another_product_s_configuration_is_not_an_intellij_configuration(self) -> None:
        """A sibling product's SDK table can never stand in for IntelliJ's, so its match configures nothing."""
        products = self.tmp / "products"
        other = products / "PyCharm2026.1"
        other.mkdir(parents=True)
        self.write_table(("Test Python", str(INTERPRETER)), config=other)

        outcome, message = self.stage(idea_config_dir=None, roots=[products])

        self.assertEqual(ensure.SKIPPED, outcome, message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())

    def test_a_configuration_without_an_sdk_table_needs_action(self) -> None:
        """A configuration with no SDK table yet asks for the registration rather than reporting a skip."""
        outcome, message = self.stage(idea_config_dir=None, roots=[self.tmp])

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertIn(ensure.SDK_TABLE_RELATIVE, message)
        self.assertIn("Project Structure", message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())

    def test_several_matching_configurations_need_action(self) -> None:
        """Two configurations that both register the interpreter are ambiguous without a selection."""
        self.write_table(("Test Python", str(INTERPRETER)), config=self.config)
        second = self.tmp / "IntelliJIdea2026.1"
        self.write_table(("Test Python", str(INTERPRETER)), config=second)

        outcome, message = self.stage(idea_config_dir=None, roots=[self.tmp])

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertIn("several Python SDK entries", message)

    def test_no_configuration_found_skips_and_exits_zero(self) -> None:
        """No IntelliJ configuration at all is a skip: the settings merge still applies and the exit is zero."""
        outcome, message = self.stage(idea_config_dir=None, roots=[self.tmp / "absent"])

        self.assertEqual(ensure.SKIPPED, outcome, message)
        self.assertIn(str(INTERPRETER), message)
        with unittest.mock.patch.object(ensure, "default_roots", lambda: [self.tmp / "absent"]):
            self.assertEqual(0, self.main_status())
        self.assertTrue((self.root / ".idea" / "palantir-java-format.xml").is_file())

    def test_an_unusable_explicit_configuration_is_an_error_without_fallback(self) -> None:
        """An explicit directory with no SDK table fails, and a discoverable one is not used instead."""
        self.write_table(("Test Python", str(INTERPRETER)))
        empty = self.tmp / "empty-config"
        empty.mkdir()

        outcome, message = self.stage(idea_config_dir=empty, roots=[self.tmp])

        self.assertEqual(ensure.ERROR, outcome, message)
        self.assertIn(ensure.SDK_TABLE_RELATIVE, message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())
        self.assertEqual(1, self.main_status("--idea-config-dir", str(empty)))

    def test_an_interpreter_below_the_floor_is_an_error(self) -> None:
        """A 3.9 interpreter fails the stage, writes nothing, and makes the run exit non-zero."""
        self.write_table(("Test Python", str(INTERPRETER)))

        outcome, message = self.stage(probe=lambda _working: (INTERPRETER, "3.9.18", False))

        self.assertEqual(ensure.ERROR, outcome, message)
        self.assertIn("below the required 3.12", message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())
        self.assertEqual(1, self.main_status("--idea-config-dir", str(self.config)))

    def test_an_unparsable_table_is_an_error(self) -> None:
        """A corrupt SDK table is an error rather than a table with no match."""
        options = self.config / "options"
        options.mkdir(parents=True)
        (options / "jdk.table.xml").write_text("<application><jdk>", encoding="utf-8")

        outcome, message = self.stage()

        self.assertEqual(ensure.ERROR, outcome, message)
        self.assertIn("cannot read", message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())

    def test_a_failing_settings_merge_does_not_stop_the_python_stage(self) -> None:
        """A template defect reports the settings stage as an error while the Python stage still runs."""
        self.write_table(("Test Python", str(INTERPRETER)))
        (self.root / "config" / "idea" / "ktfmt.xml").write_text("<project />\n", encoding="utf-8")
        captured = io.StringIO()

        with unittest.mock.patch.object(ensure, "probe_interpreter", self.probe):
            with contextlib.redirect_stdout(captured), contextlib.redirect_stderr(captured):
                status = ensure.main(["--idea-config-dir", str(self.config)])

        report = captured.getvalue()
        self.assertEqual(1, status)
        self.assertIn(ensure.ERROR, report)
        self.assertIn(ensure.CONFIGURED, report)
        self.assertTrue((self.root / ensure.MODULE_RELATIVE).is_file())

    def test_the_real_probe_reports_the_interpreter_it_ran(self) -> None:
        """The real probe reports an existing interpreter whose own version and environment match its report."""
        interpreter, version, in_environment = ensure.probe_interpreter(self.root)

        self.assertTrue(interpreter.is_file())
        reported = subprocess.run(
            [str(interpreter), "-c", "import sys; print('%d.%d' % sys.version_info[:2]); "
             "print('environment' if sys.prefix != sys.base_prefix else 'base')"],
            capture_output=True,
            text=True,
            check=True,
        ).stdout.strip().splitlines()
        self.assertEqual(".".join(version.split(".")[:2]), reported[0])
        self.assertEqual("environment" if in_environment else "base", reported[1])

    def test_the_probe_asserts_its_program_and_parses_its_output(self) -> None:
        """The probe runs the declared `-c` program and parses the three lines it prints."""
        with unittest.mock.patch.object(ensure.subprocess, "run") as run:
            run.return_value = subprocess.CompletedProcess(
                args=[], returncode=0, stdout=f"{INTERPRETER}\n{VERSION}\nenvironment\n", stderr=""
            )

            interpreter, version, in_environment = ensure.probe_interpreter(self.root)

        self.assertEqual(INTERPRETER, interpreter)
        self.assertEqual(VERSION, version)
        self.assertTrue(in_environment)
        command = run.call_args.args[0]
        self.assertEqual(ensure.INTERPRETER_PROGRAM, command[2])
        self.assertEqual(str(self.root), run.call_args.kwargs["cwd"])


if __name__ == "__main__":
    unittest.main()
