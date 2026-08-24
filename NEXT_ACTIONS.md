# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 10/15 (66.7%)
- **Function parity:** 44/149 matched (target 94) — 29.5%
- **Class/type parity:** 9/26 matched (target 20) — 34.6%
- **Combined symbol parity:** 53/175 matched (target 114) — 30.3%
- **Average inline-code cosine:** 0.35 (function body across 10 matched files)
- **Average documentation cosine:** 0.61 (doc text across 10 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 8 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. context

- **Target:** `ed25519dalek.Context [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 3
- **Priority Score:** 3010503.8
- **Functions:** 3/4 matched (target 5)
- **Missing functions:** `context_correctness`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `context.rs` vs expected `context.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:context.rs` vs expected `context.rs`
- **Proposed provenance header:** `// port-lint: source context.rs` (current: `// port-lint: source context.rs`)
- **Proposed provenance header:** `// port-lint: tests context.rs` (current: `// port-lint: tests context.rs`)
- **Lint issues:** 2

### 2. signature

- **Target:** `ed25519dalek.Signature [PROVENANCE-FALLBACK]`
- **Similarity:** 0.21
- **Dependents:** 2
- **Priority Score:** 2050807.9
- **Functions:** 2/6 matched (target 17)
- **Missing functions:** `clone`, `fmt`, `try_from`, `from`
- **Types:** 1/2 matched
- **Missing types:** `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `signature.rs` vs expected `signature.rs`
- **Proposed provenance header:** `// port-lint: source signature.rs` (current: `// port-lint: source signature.rs`)
- **Lint issues:** 1

### 3. constants

- **Target:** `ed25519dalek.Constants [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched (target 1)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `constants.rs` vs expected `constants.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:constants.rs` vs expected `constants.rs`
- **Proposed provenance header:** `// port-lint: source constants.rs` (current: `// port-lint: source constants.rs`)
- **Proposed provenance header:** `// port-lint: tests constants.rs` (current: `// port-lint: tests constants.rs`)
- **Lint issues:** 2

### 4. signing

- **Target:** `ed25519dalek.Signing [PROVENANCE-FALLBACK]`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 264007.8
- **Functions:** 13/34 matched (target 18)
- **Missing functions:** `with_context`, `generate`, `as_ref`, `fmt`, `try_sign`, `try_sign_digest`, `from`, `try_from`, `ct_eq`, `eq`, `drop`, `to_pkcs8_der`, `signature_algorithm_identifier`, `serialize`, `deserialize`, `expecting`, `visit_bytes`, `visit_seq`, `raw_sign`, `raw_sign_byupdate`, `raw_sign_prehashed`
- **Types:** 1/6 matched (target 1)
- **Missing types:** `SecretKey`, `VerifyingKey`, `Error`, `SigningKeyVisitor`, `Value`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `signing.rs` vs expected `signing.rs`
- **Proposed provenance header:** `// port-lint: source signing.rs` (current: `// port-lint: source signing.rs`)
- **Lint issues:** 1

### 5. verifying

- **Target:** `ed25519dalek.Verifying [PROVENANCE-FALLBACK]`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 203707.3
- **Functions:** 15/32 matched (target 21)
- **Missing functions:** `fmt`, `as_ref`, `hash`, `eq`, `from`, `with_context`, `raw_verify`, `raw_verify_prehashed`, `verify_digest`, `try_from`, `to_public_key_der`, `signature_algorithm_identifier`, `serialize`, `deserialize`, `expecting`, `visit_bytes`, `visit_seq`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `Error`, `VerifyingKeyVisitor`, `Value`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `verifying.rs` vs expected `verifying.rs`
- **Proposed provenance header:** `// port-lint: source verifying.rs` (current: `// port-lint: source verifying.rs`)
- **Lint issues:** 1

### 6. hazmat

- **Target:** `ed25519dalek.Hazmat [PROVENANCE-FALLBACK]`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 122006.5
- **Functions:** 7/16 matched (target 17)
- **Missing functions:** `fmt`, `ct_eq`, `eq`, `drop`, `try_from`, `random`, `sign_verify_nonspec`, `sign_verify_prehashed_nonspec`, `sign_byupdate`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Error`, `CtxDigest`, `MsgDigest`
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `hazmat.rs` vs expected `hazmat.rs`
- **Proposed provenance header:** `// port-lint: source hazmat.rs` (current: `// port-lint: source hazmat.rs`)
- **Lint issues:** 1

### 7. batch

- **Target:** `ed25519dalek.Batch [PROVENANCE-FALLBACK]`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 60708.9
- **Functions:** 1/6 matched (target 1)
- **Missing functions:** `next_u32`, `next_u64`, `fill_bytes`, `try_fill_bytes`, `gen_u128`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `ZeroRng`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `batch.rs` vs expected `batch.rs`
- **Proposed provenance header:** `// port-lint: source batch.rs` (current: `// port-lint: source batch.rs`)
- **Lint issues:** 1

### 8. errors

- **Target:** `ed25519dalek.Errors [PROVENANCE-FALLBACK]`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 10408.5
- **Functions:** 1/2 matched (target 12)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `errors.rs` vs expected `errors.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:errors.rs` vs expected `errors.rs`
- **Proposed provenance header:** `// port-lint: source errors.rs` (current: `// port-lint: source errors.rs`)
- **Proposed provenance header:** `// port-lint: tests errors.rs` (current: `// port-lint: tests errors.rs`)
- **Lint issues:** 2

### 9. verifying.stream

- **Target:** `verifying.StreamVerifier [PROVENANCE-FALLBACK]`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 10404.8
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `new`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `verifying/stream.rs` vs expected `verifying/stream.rs`
- **Proposed provenance header:** `// port-lint: source verifying/stream.rs` (current: `// port-lint: source verifying/stream.rs`)
- **Lint issues:** 1

### 10. lib

- **Target:** `ed25519dalek.Mod [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source lib.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

