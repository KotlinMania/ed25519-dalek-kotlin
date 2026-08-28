# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 10/10 (100.0%)
- **Function parity:** 55/103 matched (target 110) — 53.4%
- **Class/type parity:** 10/22 matched (target 22) — 45.5%
- **Combined symbol parity:** 65/125 matched (target 132) — 52.0%
- **Average inline-code cosine:** 0.29 (function body across 10 matched files)
- **Average documentation cosine:** 0.62 (doc text across 10 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 9 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. context

- **Target:** `ed25519dalek.Context`
- **Similarity:** 0.83
- **Dependents:** 3
- **Priority Score:** 3000501.8
- **Functions:** 4/4 matched (target 8)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 2. signature

- **Target:** `ed25519dalek.Signature`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1050807.9
- **Functions:** 2/6 matched (target 17)
- **Missing functions:** `clone`, `fmt`, `try_from`, `from`
- **Types:** 1/2 matched
- **Missing types:** `Error`

### 3. signing

- **Target:** `ed25519dalek.Signing`
- **Similarity:** 0.26
- **Dependents:** 0
- **Priority Score:** 214007.4
- **Functions:** 17/34 matched (target 23)
- **Missing functions:** `as_ref`, `fmt`, `try_sign`, `try_sign_digest`, `from`, `try_from`, `ct_eq`, `eq`, `drop`, `serialize`, `deserialize`, `expecting`, `visit_bytes`, `visit_seq`, `raw_sign`, `raw_sign_byupdate`, `raw_sign_prehashed`
- **Types:** 2/6 matched (target 3)
- **Missing types:** `VerifyingKey`, `Error`, `SigningKeyVisitor`, `Value`

### 4. verifying

- **Target:** `ed25519dalek.Verifying`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 173706.9
- **Functions:** 18/32 matched (target 25)
- **Missing functions:** `fmt`, `as_ref`, `hash`, `eq`, `from`, `raw_verify`, `raw_verify_prehashed`, `verify_digest`, `try_from`, `serialize`, `deserialize`, `expecting`, `visit_bytes`, `visit_seq`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `Error`, `VerifyingKeyVisitor`, `Value`

### 5. hazmat

- **Target:** `ed25519dalek.Hazmat`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 92004.8
- **Functions:** 10/16 matched (target 22)
- **Missing functions:** `fmt`, `ct_eq`, `eq`, `drop`, `try_from`, `random`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Error`, `CtxDigest`, `MsgDigest`
- **Tests:** 3/4 matched

### 6. batch

- **Target:** `ed25519dalek.Batch`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 60708.9
- **Functions:** 1/6 matched (target 1)
- **Missing functions:** `next_u32`, `next_u64`, `fill_bytes`, `try_fill_bytes`, `gen_u128`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `ZeroRng`

### 7. errors

- **Target:** `ed25519dalek.Errors`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 10408.5
- **Functions:** 1/2 matched (target 12)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_

### 8. verifying.stream

- **Target:** `verifying.StreamVerifier`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 10404.8
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `new`
- **Types:** 1/1 matched
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

### Matched

| Source | Target | Path |
|--------|--------|------|
| `constants` | `ed25519dalek.Constants` | `constants` |
| `lib` | `ed25519dalek.Mod` | `lib` |

