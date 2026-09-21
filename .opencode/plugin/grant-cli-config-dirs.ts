import { execFileSync } from "node:child_process"
import { realpathSync, existsSync, readFileSync } from "node:fs"
import { dirname, join } from "node:path"
import { homedir, tmpdir } from "node:os"

/**
 * Grants the out-of-workspace directories an unattended run needs, in one `config` hook the calling agent never has to
 * answer a prompt for: the scratch dir, the `gh` CLI's config dir, and the `openspec` package root.
 *
 * Each path differs per machine, so each is resolved rather than hard-coded. A missing grant hangs a cloud run — no
 * human can answer the `external_directory` prompt — and two of them were found that way: a `/oc` run stuck on the
 * openspec schema templates, then another on `/home/runner/.config/gh/*`.
 */

const SCRATCH_DIR_NAME = "opencode"
const OPENSPEC_PACKAGE_NAME = "@fission-ai/openspec"

/**
 * Resolves the installed `@fission-ai/openspec` package root from the `openspec` binary on PATH.
 *
 * The CLI reads its schema templates from its own package directory, which lives outside the workspace (e.g.
 * `/usr/local/lib/node_modules/@fission-ai/openspec` on a CI runner, a Homebrew Cellar path locally). An unattended run
 * cannot answer the resulting `external_directory` prompt, so the path must be granted up front — and it differs per
 * machine, so it is resolved rather than hard-coded. Returns null when `openspec` is not on PATH or the resolution
 * fails, so the plugin still loads on a machine without it.
 */
function resolveOpenspecPackageRoot(): string | null {
  try {
    const binary = execFileSync("which", ["openspec"], { encoding: "utf8" }).trim()
    let dir = dirname(realpathSync(binary))
    while (dir !== "/" && !existsSync(join(dir, "package.json"))) {
      dir = dirname(dir)
    }
    const pkg = JSON.parse(readFileSync(join(dir, "package.json"), "utf8"))
    return pkg.name === OPENSPEC_PACKAGE_NAME ? dir : null
  } catch {
    return null
  }
}

/**
 * Resolves the `gh` CLI's config directory the way `gh` itself does: `$GH_CONFIG_DIR`, else `$XDG_CONFIG_HOME/gh`, else
 * `~/.config/gh`.
 *
 * The agent invokes `gh` for read operations (reading a PR or issue, listing workflows), and `gh` consults its config
 * directory — outside the workspace — so an unattended run prompts for `external_directory` and hangs. The directory is
 * `/home/runner/.config/gh` on a CI runner and a home-directory path locally, so it is resolved rather than hard-coded.
 */
function resolveGhConfigDir(): string {
  const xdg = process.env["XDG_CONFIG_HOME"]
  return process.env["GH_CONFIG_DIR"] || join(xdg || join(homedir(), ".config"), "gh")
}

export default async () => ({
  config: (cfg: { permission?: Record<string, unknown> }) => {
    const base = tmpdir().replace(/\/+$/, "")
    const scratchDir = `${base}/${SCRATCH_DIR_NAME}`
    const openspecRoot = resolveOpenspecPackageRoot()
    const ghConfigDir = resolveGhConfigDir()

    cfg.permission ??= {}
    cfg.permission.external_directory = {
      ...((cfg.permission.external_directory as Record<string, string>) ?? {}),
      [`${scratchDir}/**`]: "allow",
      [`${ghConfigDir}/**`]: "allow",
      ...(openspecRoot ? { [`${openspecRoot}/**`]: "allow" } : {}),
    }
  },
})
