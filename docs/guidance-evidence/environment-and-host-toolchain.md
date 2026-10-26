# Guidance evidence: `### Environment & host toolchain`

Incident records for the converted rules of `AGENTS.md`'s `### Environment & host toolchain`, one section per rule. A
section's `Rule:` paragraph repeats the root rule's leading text verbatim and is the pairing key; the section title is
only an address. The agent-tooling audit owns this file: it reports a section whose `Rule:` paragraph matches no live
rule, and a rule whose `evidence:` token names this record when no section matches that rule. The file holds no guidance
of its own.

## local-network-permission

Rule: **macOS local-network privacy can deny a daemonised tool its cluster access while the terminal that launched it
works.**

Recorded 2026-10-26 from a local deployment diagnosis on macOS 27.0.1 (26A434), apple silicon, colima 0.10.3 with k3s
v1.37.1+k3s1, Gradle 9.8.2, Helm plugin 3.2.0 (helm client 4.3.1), OpenCode v2.1.4.

**Symptom.** `./gradlew helmUninstall` failed against the cluster's VM address, naming the transport error
`dial tcp 192.168.64.2:54816: connect: no route to host`.

The cluster itself was healthy: `colima ssh -- sudo k3s kubectl get nodes` reported the node `Ready`, and `helm list -A`
through the loopback forwarder listed every release as `deployed`.

**Measured matrix** — same host, same minute, same destination; `python3` is a non-entitled client, `/usr/bin/curl` a
platform binary:

| client          | `192.168.100.1:80` (LAN) | `192.168.64.2:54816` (VM) | `140.82.121.4:443` |
| --------------- | ------------------------ | ------------------------- | ------------------ |
| `python3`       | `errno 65`               | `errno 65`                | connected          |
| `/usr/bin/curl` | reachable                | `HTTP 401` from the API   | reachable          |

The same pair run from a Warp shell reported `colima Ready` from `kubectl` and `errno 61` from `python3`; run from the
agent session — a child of `/opt/homebrew/Cellar/opencode-v2/2.1.4/bin/opencode serve --service`, reparented to
`launchd` — the same two commands failed with `no route to host` and `errno 65`. A Gradle daemon (`Gradle 9.8.2`,
`PPID 1`) that built the Helm task held the same verdict while a `helm` invocation in the same shell succeeded.

**What the matrix established.** Two axes, not one host fault: the verdict follows the process tree — the same commands
behave normally from a Warp shell, where `kubectl` reports the node `Ready` and `python3` reaches the address
(`errno 61`, a refusal) — and, within one session, the client: `python3` gets `EHOSTUNREACH` where `/usr/bin/curl`
reaches the same address. `EHOSTUNREACH` on directly attached subnets while the internet and VPN destinations work is
what the combination produces. The rival explanations the symptom invites were weighed and none accounted for the client
split — the route was present (`route -n get 192.168.64.2` reported `bridge100`), ICMP to the VM was already lost before
any change, and a stale full-tunnel `utun6` interface was present but reachable destinations through it worked. A
process-specific network filter would produce the same split, so the matrix narrows the cause to the process's own
network policy without distinguishing consent from a filter.

**What remained unverified.** The responsible-process attribution itself (`sudo launchctl procinfo <pid>`, which prints
`responsible path`) needs the owner's password and was not run, so the mechanism rests on the behaviour above plus Apple
`TN3179` and the public reports of the same denial for `launchd`-spawned tools.

**Resolution.** The owner does not use the IDE's agent; the failing path was the Gradle daemon, in the agent session and
in IDEA's Gradle panel alike. A kube context pointing at colima's loopback forwarder (`https://127.0.0.1:<port>`, the CA
copied from the `colima` entry) selected through `helm.local.kubeContext` made both succeed — measured with a real
`helm upgrade` through the daemon, and reported by the owner for the uninstall from IDEA's panel. The ingress hostnames
were unaffected: `traefik`'s LoadBalancer stayed at `192.168.64.2` and the `/etc/hosts` mapping remained valid.

`captured: capture-colima-local-network`
