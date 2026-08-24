# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/10 (30.0%)
- **Function parity:** 4/122 matched (target 14) — 3.3%
- **Class/type parity:** 3/22 matched (target 11) — 13.6%
- **Combined symbol parity:** 7/144 matched (target 25) — 4.9%
- **Average inline-code cosine:** 0.26 (function body across 3 matched files)
- **Average documentation cosine:** 0.87 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 2 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. context

- **Target:** `ed25519dalek.Context`
- **Similarity:** 0.63
- **Dependents:** 3
- **Priority Score:** 3010503.8
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `context_correctness`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 2. constants

- **Target:** `ed25519dalek.Constants [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched (target 1)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 3. errors

- **Target:** `ed25519dalek.Errors`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 10408.5
- **Functions:** 1/2 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 9)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

