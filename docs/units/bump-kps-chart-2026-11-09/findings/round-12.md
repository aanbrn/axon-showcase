- [claim-unreproducible] design.md:16 — the sentence treats the check's run and the host `helm search repo` listing as
  one refreshed index, but the plugin's client keeps its repository file under `build/helm/` while the host CLI reads
  its own, so the run neither refreshes nor re-confirms the listing.
