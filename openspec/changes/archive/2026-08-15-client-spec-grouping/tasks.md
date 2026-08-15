## 1. Add contract-source cross-references to client specs

- [x] 1.1 Add a "Contract source" note to `openspec/specs/showcase/clients/command-client/spec.md` pointing at
  `openspec/specs/showcase/write-side/command-service/spec.md` and naming the shared symbols: the four commands
  (`ScheduleShowcaseCommand`, `StartShowcaseCommand`, `FinishShowcaseCommand`, `RemoveShowcaseCommand`) and the error
  codes `ILLEGAL_STATE`, `NOT_FOUND`, `TITLE_IN_USE`, `INVALID_COMMAND`.
- [x] 1.2 Add a "Contract source" note to `openspec/specs/showcase/clients/query-client/spec.md` pointing at
  `openspec/specs/showcase/read-side/query-service/spec.md` and naming the shared symbols: the two endpoints
  (`/streaming-query`, `/query`), the query types (`FetchShowcaseListQuery`, `FetchShowcaseByIdQuery`), and the error
  codes `INVALID_QUERY`, `NOT_FOUND`.

## 2. Verify

- [x] 2.1 Run `openspec validate --specs` and confirm no validation errors.
- [x] 2.2 Confirm the two client specs still render each requirement unchanged apart from the added cross-reference note.