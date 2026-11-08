# Unit: address-kafka-client-advisories

Workspace:
`unit=address-kafka-client-advisories; path=/Users/afanasyev/Development/axon-showcase-address-kafka-client-advisories; branch=address-kafka-client-advisories; writer=ses_e42e83c1dffegAuNLdVN2PMr8o`

State: active

The unit addresses the two high-severity `org.apache.kafka:kafka-clients@3.9.2` advisories that failed the 2026-11-07
`Dependency Security` run: it verifies whether a `kafka-clients` 4.4.0 constraint is viable with the pinned Spring Kafka
3.3 / Axon Kafka 4.12 stack, and ships the bump when it is, falling back to a reasoned, expiring `.snyk` suppression
otherwise. Its plan and tasks live in `openspec/changes/address-kafka-client-advisories/`.
