## Context

`showcaseCache`'s maximum size appears on four surfaces:

| Surface                                                              | Value    |
| -------------------------------------------------------------------- | -------- |
| `ShowcaseCommandProperties` (Java default)                           | `1000`   |
| `application.yml` (`${SHOWCASE_CACHE_MAX_SIZE:…}`)                   | `100000` |
| `helm/chart/.../values.yaml` (`showcaseCache.maxSize`)               | `100000` |
| the write-side spec's "Default cache configuration applies" scenario | `1000`   |

ADR-0002 makes the Java class the authoritative contract, with the yml placeholder and the chart mirroring it — and its
Consequences require updating the Java class, the yml, and the chart **together**. Here the yml is bound in every
environment, so the Java default never runs: the effective bound is `100000` while the documented one is `1000`.

Only the size diverges; the expiries (`PT10M`/`PT5M`) agree on all four surfaces, as do `sagaCache` and
`sagaAssociationsCache` at `1000` throughout.

History locates the divergence: the placeholder and the Java default were both `1000` in the initial commit (`5ff1a0c`),
and `4569bbc` ("enhancements in observability and error handling") raised the yml **and the chart** to `100000` in the
same commit — moving two of the three surfaces deliberately, not editing one by accident. The Java default and the spec
were not updated with them.

The repository has already resolved this exact class of drift once: `align-gateway-cache-defaults` (archived 2026-08-18)
found the gateway's query caches declared as Java `1000` against yml `100000`/`1000000`. It did not simply adopt either
side: it picked the operationally-intended values (`10000`/`100000`), **raised the Java defaults to match them, and
lowered the yml and the chart** — updating all three surfaces together. The rule it established is: pick the intended
value, make the Java class authoritative at it, and bring every surface into line — not "the Java value always wins".

## Goals / Non-Goals

**Goals:**

- One value on every surface, with the Java class authoritative at it, per ADR-0002.
- The component test asserts `showcaseCache`, so this drift fails next time.

**Non-Goals:**

- Changing the deployed bound: every environment already uses `100000`, so behavior is unchanged.
- Re-opening the gateway caches, which are already reconciled.

## Decisions

### The reconciled value is `100000`

There is no need to infer intent from the drift alone — the intent was recorded in the code, under review, in a change
of its own:

1. **The repository's own precedent.** `align-gateway-cache-defaults` faced the identical shape (Java default lagging
   the deployed one) and resolved it by choosing the intended value and updating all three surfaces to it — raising Java
   and lowering the deployments. Applying that rule here means making the Java default `100000`, not lowering the
   deployed bound to the Java field's lagging `1000`.
2. **`4569bbc` moved the surfaces deliberately.** It changed the yml placeholder _and_ the chart value together, and in
   the same commit raised the gateway caches; a commit that moves a cache bound across two surfaces and touches a
   sibling cache's defaults is making a decision, not an incidental edit.
3. **The incoherence that would follow from `1000`.** If the bound were meant to be `1000`, `4569bbc` would have raised
   the deployed value a hundredfold _and_ raised the gateway caches in the same breath — and no reader of that commit
   would call it a change of intent. Keeping `1000` would also force a production reduction of a live cache bound,
   justified only by the Java field's value, which the precedent explicitly does not treat as authoritative on its own.

The spec's `1000` is a separate staleness: the requirement was authored in `73f0fd9` (2026-08-18), months after
`4569bbc` had raised the deployed value, and it was written at the Java field's lagging value rather than the deployed
one. The delta updates the requirement to `100000` rather than treating it as a signal for `1000`.

### The delta updates the requirement rather than the implementation

Both remaining surfaces (Java, spec) move to the deployed value; the yml and the chart are already correct. The
alternative — treating the Java class and the spec as jointly authoritative and lowering the yml and chart — is the
option `align-gateway-cache-defaults` rejected, and it would change deployed behavior for no recorded benefit.

## Risks / Trade-offs

- **[The deployed `100000` was never meant to be the default]** → it was moved deliberately with its sibling surfaces
  and has run in every environment since `4569bbc`; if the owner disagrees, the alternative is the inverse change and
  the tasks name the value in one place so it is trivial to invert.
- **[Silent re-drift]** → the added component-test assertion binds `showcaseCache` with the others, so a future edit to
  one surface alone fails the build.
