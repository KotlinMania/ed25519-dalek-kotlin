# port-lint Proposed Changes

**Generated:** 2026-08-24
**Source:** tmp/ed25519-dalek
**Target:** src

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Context.kt` | `// port-lint: source context.rs` | `// port-lint: source context.rs` | `context.rs` | `port-lint provenance header matched only after fallback normalization: 'context.rs' vs expected 'context.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/ed25519dalek/ContextTest.kt` | `// port-lint: tests context.rs` | `// port-lint: tests context.rs` | `context.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:context.rs' vs expected 'context.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Signature.kt` | `// port-lint: source signature.rs` | `// port-lint: source signature.rs` | `signature.rs` | `port-lint provenance header matched only after fallback normalization: 'signature.rs' vs expected 'signature.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Constants.kt` | `// port-lint: source constants.rs` | `// port-lint: source constants.rs` | `constants.rs` | `port-lint provenance header matched only after fallback normalization: 'constants.rs' vs expected 'constants.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/ed25519dalek/ConstantsTest.kt` | `// port-lint: tests constants.rs` | `// port-lint: tests constants.rs` | `constants.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:constants.rs' vs expected 'constants.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Signing.kt` | `// port-lint: source signing.rs` | `// port-lint: source signing.rs` | `signing.rs` | `port-lint provenance header matched only after fallback normalization: 'signing.rs' vs expected 'signing.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Verifying.kt` | `// port-lint: source verifying.rs` | `// port-lint: source verifying.rs` | `verifying.rs` | `port-lint provenance header matched only after fallback normalization: 'verifying.rs' vs expected 'verifying.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Hazmat.kt` | `// port-lint: source hazmat.rs` | `// port-lint: source hazmat.rs` | `hazmat.rs` | `port-lint provenance header matched only after fallback normalization: 'hazmat.rs' vs expected 'hazmat.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Batch.kt` | `// port-lint: source batch.rs` | `// port-lint: source batch.rs` | `batch.rs` | `port-lint provenance header matched only after fallback normalization: 'batch.rs' vs expected 'batch.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Errors.kt` | `// port-lint: source errors.rs` | `// port-lint: source errors.rs` | `errors.rs` | `port-lint provenance header matched only after fallback normalization: 'errors.rs' vs expected 'errors.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/ed25519dalek/ErrorsTest.kt` | `// port-lint: tests errors.rs` | `// port-lint: tests errors.rs` | `errors.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:errors.rs' vs expected 'errors.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/verifying/StreamVerifier.kt` | `// port-lint: source verifying/stream.rs` | `// port-lint: source verifying/stream.rs` | `verifying/stream.rs` | `port-lint provenance header matched only after fallback normalization: 'verifying/stream.rs' vs expected 'verifying/stream.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/ed25519dalek/Mod.kt` | `// port-lint: source lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'lib.rs' vs expected 'lib.rs'` |
