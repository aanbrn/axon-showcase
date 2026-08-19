## 1. Remove dead fallback branches

- [x] 1.1 Simplify the `fetchList` list-cache fallback (remove the dead `whenComplete` error branch)
- [x] 1.2 Simplify the `fetchList` byId-cache fallback
- [x] 1.3 Simplify the `fetchById` byId-cache fallback
- [x] 1.4 Revert the earlier ineffective fallback error-branch tests (they only exercised the miss path)
