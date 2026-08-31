# port-lint Proposed Changes

**Generated:** 2026-08-31
**Source:** tmp/ed25519-dalek/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Context.kt` | `// port-lint: source ed25519-dalek/src/context.rs` | `// port-lint: source context.rs` | `context.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/context.rs' vs expected 'context.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/ed25519dalek/ContextTest.kt` | `// port-lint: tests ed25519-dalek/src/context.rs` | `// port-lint: tests context.rs` | `context.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:ed25519-dalek/src/context.rs' vs expected 'context.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Signature.kt` | `// port-lint: source ed25519-dalek/src/signature.rs` | `// port-lint: source signature.rs` | `signature.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/signature.rs' vs expected 'signature.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Signing.kt` | `// port-lint: source ed25519-dalek/src/signing.rs` | `// port-lint: source signing.rs` | `signing.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/signing.rs' vs expected 'signing.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Verifying.kt` | `// port-lint: source ed25519-dalek/src/verifying.rs` | `// port-lint: source verifying.rs` | `verifying.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/verifying.rs' vs expected 'verifying.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Hazmat.kt` | `// port-lint: source ed25519-dalek/src/hazmat.rs` | `// port-lint: source hazmat.rs` | `hazmat.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/hazmat.rs' vs expected 'hazmat.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/ed25519dalek/HazmatTest.kt` | `// port-lint: tests ed25519-dalek/src/hazmat.rs` | `// port-lint: tests hazmat.rs` | `hazmat.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:ed25519-dalek/src/hazmat.rs' vs expected 'hazmat.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Batch.kt` | `// port-lint: source ed25519-dalek/src/batch.rs` | `// port-lint: source batch.rs` | `batch.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/batch.rs' vs expected 'batch.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Errors.kt` | `// port-lint: source ed25519-dalek/src/errors.rs` | `// port-lint: source errors.rs` | `errors.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/errors.rs' vs expected 'errors.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/ed25519dalek/ErrorsTest.kt` | `// port-lint: tests ed25519-dalek/src/errors.rs` | `// port-lint: tests errors.rs` | `errors.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:ed25519-dalek/src/errors.rs' vs expected 'errors.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek/verifying/StreamVerifier.kt` | `// port-lint: source ed25519-dalek/src/verifying/stream.rs` | `// port-lint: source verifying/stream.rs` | `verifying/stream.rs` | `port-lint provenance header matched only after fallback normalization: 'ed25519-dalek/src/verifying/stream.rs' vs expected 'verifying/stream.rs'` |
