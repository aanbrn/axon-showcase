#!/usr/bin/env python3
"""Assert that every service Deployment renders the update-strategy guard.

A Deployment that sets `strategy.type: Recreate` while the live release still carries `strategy.rollingUpdate` fails its
next upgrade: a server-side apply that changes only the type keeps that block, and the API rejects the pair. The chart's
`axon-showcase.deploymentStrategy` helper renders `rollingUpdate: null` beside a `Recreate` type for a Helm-owned
transition, and nothing beside any other type.

Usage: `verify-chart-strategies.py <recreate-render-dir> <rollingupdate-render-dir>`, each the output of
`scripts/render-chart-strategies.py` for the matching strategy.

Exit status: 0 every Deployment carries what its strategy requires, 1 a violation (printed), 2 a usage error.
"""

import sys
from pathlib import Path

SERVICES = ("projection-service", "command-service", "query-service", "api-gateway", "web-ui")


def deployment_file(render: Path, service: str) -> Path:
    """The rendered Deployment of `service` inside a `helm template --output-dir` tree."""
    roots = [path for path in render.iterdir() if path.is_dir()]
    if len(roots) != 1:
        raise ValueError("expected one chart directory under {0}, found {1}".format(render, len(roots)))
    return roots[0] / "templates" / service / "deployment.yaml"


def strategy_of(text: str) -> dict[str, str] | None:
    """The Deployment's `spec.strategy` mapping, comment lines excluded; None when it renders none."""
    lines = text.splitlines()
    start = next(
        (
            index
            for index, line in enumerate(lines)
            if line.startswith("  strategy:") and not line.lstrip().startswith("#")
        ),
        None,
    )
    if start is None:
        return None
    mapping: dict[str, str] = {}
    for line in lines[start + 1 :]:
        stripped = line.strip()
        if not stripped:
            continue
        if len(line) - len(line.lstrip()) <= 2:
            break
        if stripped.startswith("#"):
            continue
        key, _, value = stripped.partition(":")
        mapping[key.strip()] = value.strip()
    return mapping


def violations(recreate: Path, rolling_update: Path) -> list[str]:
    """Every Deployment whose rendered strategy contradicts its render's strategy."""
    found: list[str] = []
    for service in SERVICES:
        guarded = strategy_of(deployment_file(recreate, service).read_text(encoding="utf-8"))
        if guarded is None:
            found.append("{0}: the Recreate render carries no strategy block".format(service))
        else:
            if guarded.get("type") != "Recreate":
                found.append("{0}: the Recreate render sets type '{1}'".format(service, guarded.get("type")))
            if guarded.get("rollingUpdate") != "null":
                found.append(
                    "{0}: the Recreate render does not set rollingUpdate to null, so the upgrade would be "
                    "rejected".format(service)
                )
        plain = strategy_of(deployment_file(rolling_update, service).read_text(encoding="utf-8"))
        if plain is None:
            found.append("{0}: the RollingUpdate render carries no strategy block".format(service))
        else:
            if plain.get("type") != "RollingUpdate":
                found.append("{0}: the RollingUpdate render sets type '{1}'".format(service, plain.get("type")))
            if plain.get("rollingUpdate") == "null":
                found.append("{0}: the RollingUpdate render carries rollingUpdate: null".format(service))
    return found


def main(argv: list[str] | None = None) -> int:
    arguments = list(sys.argv[1:] if argv is None else argv)
    if len(arguments) != 2:
        print(__doc__.splitlines()[0], file=sys.stderr)
        return 2
    try:
        found = violations(Path(arguments[0]), Path(arguments[1]))
    except (OSError, ValueError) as error:
        print("verify-chart-strategies: {0}".format(error), file=sys.stderr)
        return 1
    for violation in found:
        print("verify-chart-strategies: {0}".format(violation), file=sys.stderr)
    return 1 if found else 0


if __name__ == "__main__":
    sys.exit(main())
