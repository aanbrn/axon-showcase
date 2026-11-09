- [claim-unreproducible] docs/units/stabilize-deployment-smoke-startup/deep-review.md:15 — "the change's files are
  untracked (so no path or declaration signal is read)" misstates the tool, which adds untracked paths and their
  contents to the sets it scans; the absence holds because the paths match no signal prefix and none is a source file.
