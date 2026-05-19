# Rust Dependency Import Audit

This repo is still in parity mode. Kotlin dependencies must follow the upstream
Rust crate imports by transliterating crate names to kotlinmania artifacts, not
by substituting unrelated libraries.

## Main and Feature Imports

| Rust crate | Kotlin coordinate to use | Status |
|---|---|---|
| `curve25519-dalek` | `io.github.kotlinmania:curve25519-dalek-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `ed25519` | `io.github.kotlinmania:ed25519-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `merlin` | `io.github.kotlinmania:merlin-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `rand_core` | `io.github.kotlinmania:rand-core-kotlin:<version>` | Blocked: no sibling repo or published artifact found. Do not substitute `rand-kotlin`. |
| `serde` | `io.github.kotlinmania:serde-kotlin:0.1.1` | Blocked: sibling repo exists, but Maven Central has no published artifact. |
| `sha2` | `io.github.kotlinmania:sha2-kotlin:0.1.0` | Blocked: sibling repo exists, but Maven Central has no published artifact. |
| `signature` | `io.github.kotlinmania:signature-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `subtle` | `io.github.kotlinmania:subtle-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `zeroize` | `io.github.kotlinmania:zeroize-kotlin:0.1.0` | Blocked: sibling repo exists, but Maven Central has no published artifact. |

## Dev/Test Imports

| Rust crate | Kotlin coordinate to use | Status |
|---|---|---|
| `bincode` | `io.github.kotlinmania:bincode-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `blake2` | `io.github.kotlinmania:blake2-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `criterion` | `io.github.kotlinmania:criterion-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `hex` | `io.github.kotlinmania:hex-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `hex-literal` | `io.github.kotlinmania:hex-literal-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `rand` | `io.github.kotlinmania:rand-kotlin:0.1.0` | Sibling repo exists; add only when porting Rust dev/test code that imports `rand`. |
| `serde_json` | `io.github.kotlinmania:serde-json-kotlin:0.1.0` | Sibling repo exists; add only when porting Rust dev/test code that imports `serde_json`. |
| `sha3` | `io.github.kotlinmania:sha3-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |
| `toml` | `io.github.kotlinmania:toml-kotlin:0.1.0` | Sibling repo exists; add only when porting Rust dev/test code that imports `toml`. |
| `x25519-dalek` | `io.github.kotlinmania:x25519-dalek-kotlin:<version>` | Blocked: no sibling repo or published artifact found. |

## Verification Notes

`ast_distance --deep tmp/ed25519-dalek/src rust src/commonMain/kotlin/io/github/kotlinmania/ed25519dalek kotlin`
reported 10 upstream source files and 80 Rust imports, with no Kotlin source
files present yet.

Attempting to add the available sibling coordinates to `build.gradle.kts`
failed `./gradlew codeqlCompileJvm --no-daemon --console=plain --no-configuration-cache`
because Gradle could not resolve:

- `io.github.kotlinmania:serde-kotlin-jvm:0.1.1`
- `io.github.kotlinmania:sha2-kotlin-jvm:0.1.0`
- `io.github.kotlinmania:zeroize-kotlin-jvm:0.1.0`

The matching root, JVM, and Android artifacts for those three coordinates also
returned 404 from Maven Central. Do not add a mismatched artifact to keep the
build green; publish or create the correctly named dependency ports first.
