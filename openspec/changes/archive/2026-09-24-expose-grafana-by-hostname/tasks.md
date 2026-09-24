# Tasks

## 1. Implementation

- [x] 1.1 Enable `grafana.ingress` in `helm/values/kps/values-local.yaml` (`enabled: true`, `hosts`:
      `[axon-showcase-grafana]`, no `ingressClassName`). Verify the rendered Ingress with
      `helm template kps prometheus-community/kube-prometheus-stack --version 91.4.1 -f helm/values/kps/values-local.yaml -n monitoring`
      piped to a grep for `kind: Ingress` and `axon-showcase-grafana`, and read that output. — Verified: the render
      produces Ingress `kps-grafana` (`namespace: monitoring`) with host `axon-showcase-grafana` and no
      `ingressClassName`.
- [x] 1.2 Add `axon-showcase-grafana` to `HOSTNAMES` in `setup-hosts.sh` and extend the header/usage comment to mention
      the Grafana hostname. Verify `bash -n setup-hosts.sh` passes and `grep axon-showcase-grafana setup-hosts.sh` finds
      it in `HOSTNAMES`. — Verified: `bash -n` OK; the hostname is in `HOSTNAMES`; the header comment names it.
- [x] 1.3 In `README.md`, replace the port-forward block (`README.md:753-759`) with the hostname access path
      (`./setup-hosts.sh setup`, then open `http://axon-showcase-grafana`), preserving the trailing "Traces are
      available in the Tempo data source under Grafana → Explore." sentence; the default-login detail is in the
      untouched Grafana bullet above. Also add `axon-showcase-grafana` to the "Access the Deployed System" enumeration
      (`README.md:635-636` and the recap at `644-645`). Verify `grep -n "kps-grafana" README.md` returns nothing; both
      `Tempo data source under Grafana` and `axon-showcase-grafana` are present; and the access section names all three
      hostnames. — Verified: no `kps-grafana`; `axon-showcase-grafana` at README 636/646/753; the traces sentence is
      preserved at 754.
- [x] 1.4 In `AGENTS.md`, update the "surface human-visible capabilities" example that names the observability access
      path as "the Grafana port-forward" to the hostname, and the local-values sentence at `AGENTS.md:1198-1201` that
      enumerates the managed ingress hostnames (`axon-showcase-api`, `axon-showcase-ui`) to include
      `axon-showcase-grafana`. Verify both name the Grafana hostname and the `svc/tempo` trace-fetch note is untouched.
      — Verified: AGENTS.md names the hostname (line 1199); the `svc/tempo 3200` note is intact at 1388.
- [x] 1.5 Remove the "Grafana ingress + hostname instead of port-forward" idea from `docs/ideas.md`. Verify
      `grep -rn "Grafana ingress + hostname" docs/ideas.md` returns nothing. — Verified: no match.

## 2. Verification

- [x] 2.1 Sweep for stale copies of the recipe: `grep -rn "port-forward" README.md AGENTS.md` — only the intentional
      `svc/tempo 3200` trace-fetch note should remain; read the full output. — Verified: the only match is
      `AGENTS.md:1388` (`svc/tempo 3200`), intentionally kept.
- [x] 2.2 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck`; both pass (the change-dir markdown is in the
      markdown target). — Verified: both pass.
- [x] 2.3 Live check (owner-run; requires a local cluster): `./gradlew helmInstallKpsToLocal` then
      `./setup-hosts.sh setup`, and confirm `http://axon-showcase-grafana` loads Grafana (browser or
      `curl -I http://axon-showcase-grafana`). Record the observed result. If no local cluster is available, say so in
      the report and park the check in `docs/ideas.md` (naming it in the change's report) rather than ticking it. —
      Verified live on colima+k3s: `helmInstallKpsToLocal` succeeded; Ingress `kps-grafana` (class `traefik`, address
      `192.168.64.2`) and Grafana pod `3/3 Running`; `curl -H "Host: axon-showcase-grafana" http://192.168.64.2/login` →
      `200` (title `Grafana`) and `/api/health` → `ok`. `./setup-hosts.sh setup` resolved the address and echoed the
      three hostnames, then stopped at the `sudo` boundary (no terminal for the password); the `/etc/hosts` write is
      left for the owner to run.
