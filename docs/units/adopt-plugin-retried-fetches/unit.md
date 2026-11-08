# Unit: adopt-plugin-retried-fetches

Workspace:
`unit=adopt-plugin-retried-fetches; path=/Users/afanasyev/Development/axon-showcase; branch=adopt-plugin-retried-fetches; writer=ses_e44c24768ffekZbpH3Gi5YeGvN`

State: closed

The unit takes the `gradle-helm-plugin` 3.3.1 release and retires both repository-fetch workarounds: the plugin's tasks
own the retry and the declared-repository scope (through `extraArgs`), while Helm's two-minute per-download default
stays. Its plan and tasks live in `openspec/changes/adopt-plugin-retried-fetches/`.
