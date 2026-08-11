## 1. Validate

- [x] 1.1 Run `openspec validate --change "capture-helm-chart-behavior"` and confirm the change and spec structure pass

## 2. Review

- [x] 2.1 Review the new `showcase/helm-chart` spec against the chart templates, helpers, and default values and confirm
      every requirement and scenario matches current behavior

## 3. Archive

- [x] 3.1 Run `openspec archive "capture-helm-chart-behavior"` to fold the delta into
      `openspec/specs/showcase/helm-chart/spec.md`
- [x] 3.2 Confirm `openspec/specs/showcase/helm-chart/spec.md` exists and passes `openspec validate`
