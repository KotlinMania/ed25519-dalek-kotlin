# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 10/10 (100.0%)
- **Function parity:** 44/103 matched (target 95) — 42.7%
- **Class/type parity:** 9/22 matched (target 21) — 40.9%
- **Combined symbol parity:** 53/125 matched (target 116) — 42.4%
- **Average inline-code cosine:** 0.25 (function body across 10 matched files)
- **Average documentation cosine:** 0.61 (doc text across 10 matched files)
- **Cheat-zeroed Files:** 2
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
- **Similarity:** 0.63
- **Dependents:** 3
- **Priority Score:** 3010503.8
- **Functions:** 3/4 matched (target 5)
- **Missing functions:** `context_correctness`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 2. signature

- **Target:** `ed25519dalek.Signature`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1050807.9
- **Functions:** 2/6 matched (target 17)
- **Missing functions:** `clone`, `fmt`, `try_from`, `from`
- **Types:** 1/2 matched
- **Missing types:** `Error`

### 3. constants

- **Target:** `ed25519dalek.Constants [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched (target 1)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 4. signing

- **Target:** `ed25519dalek.Signing`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 264007.8
- **Functions:** 13/34 matched (target 18)
- **Missing functions:** `with_context`, `generate`, `as_ref`, `fmt`, `try_sign`, `try_sign_digest`, `from`, `try_from`, `ct_eq`, `eq`, `drop`, `to_pkcs8_der`, `signature_algorithm_identifier`, `serialize`, `deserialize`, `expecting`, `visit_bytes`, `visit_seq`, `raw_sign`, `raw_sign_byupdate`, `raw_sign_prehashed`
- **Types:** 1/6 matched (target 1)
- **Missing types:** `SecretKey`, `VerifyingKey`, `Error`, `SigningKeyVisitor`, `Value`

### 5. verifying

- **Target:** `ed25519dalek.Verifying`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 203707.3
- **Functions:** 15/32 matched (target 21)
- **Missing functions:** `fmt`, `as_ref`, `hash`, `eq`, `from`, `with_context`, `raw_verify`, `raw_verify_prehashed`, `verify_digest`, `try_from`, `to_public_key_der`, `signature_algorithm_identifier`, `serialize`, `deserialize`, `expecting`, `visit_bytes`, `visit_seq`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `Error`, `VerifyingKeyVisitor`, `Value`

### 6. hazmat

- **Target:** `ed25519dalek.Hazmat`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 122006.5
- **Functions:** 7/16 matched (target 17)
- **Missing functions:** `fmt`, `ct_eq`, `eq`, `drop`, `try_from`, `random`, `sign_verify_nonspec`, `sign_verify_prehashed_nonspec`, `sign_byupdate`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Error`, `CtxDigest`, `MsgDigest`
- **Tests:** 0/4 matched

### 7. batch

- **Target:** `ed25519dalek.Batch`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 60708.9
- **Functions:** 1/6 matched (target 1)
- **Missing functions:** `next_u32`, `next_u64`, `fill_bytes`, `try_fill_bytes`, `gen_u128`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `ZeroRng`

### 8. errors

- **Target:** `ed25519dalek.Errors`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 10408.5
- **Functions:** 1/2 matched (target 12)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_

### 9. verifying.stream

- **Target:** `verifying.StreamVerifier`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 10404.8
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `new`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 10. lib

- **Target:** `ed25519dalek.Mod [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 1)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

