## REMOVED Requirements

### Requirement: Cache fallback on transient query failures

**Reason**: Superseded by "Read-through caching of showcase queries". The fallback served cached results only _after_ a
query failed, so under normal load it absorbed nothing; the new behavior consults the cache on the healthy read path and
retires the failure-triggered fallback.

**Migration**: The caches are consulted before the query rather than only in an error handler. A query that fails on a
cache miss is no longer served from the cache and propagates to the existing error translation — a `400`/`404` query
error, a `503` availability failure — as a read with no cached entry already did.

## ADDED Requirements

### Requirement: Read-through caching of showcase queries

The gateway SHALL maintain in-memory caches of fetch-showcase-list and fetch-showcase-by-id results and SHALL serve a
read from the cache when a matching entry is present; otherwise it SHALL fetch from the query service and cache the
result. Entries SHALL expire after each cache's configured write time-to-live. The two caches SHALL be independent — a
list entry SHALL hold the full showcases rather than resolving through the by-ID cache — and concurrent reads for the
same key SHALL collapse into a single query-service call.

#### Scenario: A list read is served from the cache

- **WHEN** a `GET /showcases` request matches a cached list query
- **THEN** the system responds with `200 OK` and the cached showcases without calling the query service

#### Scenario: A list read on a miss fetches and caches

- **WHEN** a `GET /showcases` request matches no cached list query
- **THEN** the system fetches the list from the query service, responds with `200 OK`, and caches the list under that
  query

#### Scenario: A by-ID read is served from the cache

- **WHEN** a `GET /showcases/{showcaseId}` request matches a cached showcase
- **THEN** the system responds with `200 OK` and the cached showcase without calling the query service

#### Scenario: A by-ID read on a miss fetches and caches

- **WHEN** a `GET /showcases/{showcaseId}` request matches no cached showcase
- **THEN** the system fetches the showcase from the query service, responds with `200 OK`, and caches it under its ID

#### Scenario: An entry expires after its write time-to-live

- **WHEN** a cached entry's write time-to-live has elapsed
- **THEN** the next matching read fetches from the query service rather than serving the expired entry

#### Scenario: The list cache is independent of the by-ID cache

- **WHEN** a list read is served from the cache
- **THEN** its showcases come from the cached list entry itself, without a by-ID lookup

#### Scenario: Concurrent reads for the same key collapse into one call

- **WHEN** several reads for the same query or showcase arrive before the first has completed
- **THEN** the query service is called once and every read is served from that result

#### Scenario: A failed fetch on a miss is not cached and propagates

- **WHEN** a read misses the cache and its query fails
- **THEN** the system responds with the error mapped by the query or availability error translation (a `404` or `400`
  for a query error, a `503` for an availability failure) rather than serving a cached result
- **AND** a subsequent identical read queries the query service again rather than serving a cached failure

### Requirement: Showcase events invalidate the by-ID cache

On any showcase domain event, the gateway SHALL evict that showcase's by-ID cache entry, so a subsequent by-ID read
re-queries the query service rather than serving a cached pre-change state.

#### Scenario: An event for a cached showcase evicts its by-ID entry

- **WHEN** the gateway receives any showcase event for a showcase it has cached by ID
- **THEN** the cache no longer holds that showcase, so a read that starts after the eviction queries the query service
  rather than serving the cached showcase
