- [test-control] scripts/verify-openspec-delta-guard.py — the archived spec was read before the archive return code was
  checked, so a failed archive raised instead of reporting; its stub wrote the file unconditionally.
