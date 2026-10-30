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
import sys
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

    def write_table(self, *entries: tuple, config: Path = None) -> Path:
        """Write an SDK table for the given (name, home) pairs and return its configuration directory."""
        target = config or self.config
        options = target / "options"
        options.mkdir(parents=True, exist_ok=True)
        (options / "jdk.table.xml").write_text(sdk_table(*entries), encoding="utf-8")
        return target

    def probe(self, working_dir: Path) -> tuple:
        """Report a fixed interpreter, standing in for the real `python3` run."""
        return INTERPRETER, VERSION

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
        self.assertIn("Project Structure", message)
        self.assertIn("SDKs", message)
        self.assertFalse((self.root / ensure.MODULE_RELATIVE).exists())
        self.assertEqual(1, self.main_status("--idea-config-dir", str(self.config)))

    def test_several_matching_entries_need_action(self) -> None:
        """Two entries for one interpreter are ambiguous rather than silently ordered."""
        self.write_table(("Test Python", str(INTERPRETER)), ("Duplicate Python", str(INTERPRETER)))

        outcome, message = self.stage()

        self.assertEqual(ensure.NEEDS_ACTION, outcome, message)
        self.assertIn("several Python SDK entries", message)
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

        outcome, message = self.stage(probe=lambda _working: (INTERPRETER, "3.9.18"))

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
        """The real probe reports an existing interpreter whose own version matches the version it printed."""
        interpreter, version = ensure.probe_interpreter(self.root)

        self.assertTrue(interpreter.is_file())
        reported = subprocess.run(
            [str(interpreter), "-c", "import sys; print('%d.%d' % sys.version_info[:2])"],
            capture_output=True,
            text=True,
            check=True,
        ).stdout.strip()
        self.assertEqual(".".join(version.split(".")[:2]), reported)

    def test_the_probe_asserts_its_program_and_parses_its_output(self) -> None:
        """The probe runs the declared `-c` program and parses the two lines it prints."""
        with unittest.mock.patch.object(ensure.subprocess, "run") as run:
            run.return_value = subprocess.CompletedProcess(
                args=[], returncode=0, stdout=f'{INTERPRETER}\n{VERSION}\n', stderr=""
            )

            interpreter, version = ensure.probe_interpreter(self.root)

        self.assertEqual(INTERPRETER, interpreter)
        self.assertEqual(VERSION, version)
        command = run.call_args.args[0]
        self.assertEqual(ensure.INTERPRETER_PROGRAM, command[2])
        self.assertEqual(str(self.root), run.call_args.kwargs["cwd"])


if __name__ == "__main__":
    unittest.main()
