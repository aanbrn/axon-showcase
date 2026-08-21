# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file
is a scratchpad, not a backlog of planned work.

## 2026-08-21

- Investigate the Spring Data calendar-versioning gap in the `majorOf()` heuristic: `2025.0.x -> 2025.1.x` reads as a
  same-major bump, but 2025.1 is a new (SB4) train. Consider extending the version-comparison logic or documenting the
  blind spot (surfaced while exploring the spring-data-bom report row).