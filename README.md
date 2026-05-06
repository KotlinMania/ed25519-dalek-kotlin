# ed25519-dalek-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fed25519--dalek--kotlin-blue.svg)](https://github.com/KotlinMania/ed25519-dalek-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/ed25519-dalek-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/ed25519-dalek-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/ed25519-dalek-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/ed25519-dalek-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`dalek-cryptography/curve25519-dalek`](https://github.com/dalek-cryptography/curve25519-dalek/tree/main/ed25519-dalek).

**Original Project:** This port is based on [`dalek-cryptography/curve25519-dalek`](https://github.com/dalek-cryptography/curve25519-dalek/tree/main/ed25519-dalek). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `dalek-cryptography/curve25519-dalek`

> The text below is reproduced and lightly edited from [`https://github.com/dalek-cryptography/curve25519-dalek/tree/main/ed25519-dalek`](https://github.com/dalek-cryptography/curve25519-dalek/tree/main/ed25519-dalek). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

<p align="center">
<img
 alt="dalek-cryptography logo: a dalek with edwards curves as sparkles coming out of its radar-schnozzley blaster thingies"
 width="200px"
 src="https://cdn.jsdelivr.net/gh/dalek-cryptography/curve25519-dalek/docs/assets/dalek-logo-clear.png"/>
</p>

## Dalek elliptic curve cryptography

This repo contains pure-Rust crates for elliptic curve cryptography:

|                 Crate                    |   Description  | Crates.io | Docs | CI                                                                                                                                                                                                                          |
-------------------------------------------|----------------|-----------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| [`curve25519‑dalek`](https://github.com/dalek-cryptography/curve25519-dalek/blob/HEAD/curve25519-dalek) | A library for arithmetic over the Curve25519 and Ristretto elliptic curves and their associated scalars. | [![](https://img.shields.io/crates/v/curve25519-dalek.svg)](https://crates.io/crates/curve25519-dalek) | [![](https://img.shields.io/docsrs/curve25519-dalek)](https://docs.rs/curve25519-dalek) | [![CI](https://github.com/dalek-cryptography/curve25519-dalek/actions/workflows/curve25519-dalek.yml/badge.svg?branch=main)](https://github.com/dalek-cryptography/curve25519-dalek/actions/workflows/curve25519-dalek.yml) |
| [`ed25519‑dalek`](https://github.com/dalek-cryptography/curve25519-dalek/blob/HEAD/ed25519-dalek)       | An implementation of the EdDSA digital signature scheme over Curve25519. | [![](https://img.shields.io/crates/v/ed25519-dalek.svg)](https://crates.io/crates/ed25519-dalek) | [![](https://docs.rs/ed25519-dalek/badge.svg)](https://docs.rs/ed25519-dalek) | [![CI](https://github.com/dalek-cryptography/curve25519-dalek/actions/workflows/ed25519-dalek.yml/badge.svg?branch=main)](https://github.com/dalek-cryptography/curve25519-dalek/actions/workflows/ed25519-dalek.yml)       |
| [`x25519‑dalek`](https://github.com/dalek-cryptography/curve25519-dalek/blob/HEAD/x25519-dalek)         | An implementation of elliptic curve Diffie-Hellman key exchange over Curve25519. | [![](https://img.shields.io/crates/v/x25519-dalek.svg)](https://crates.io/crates/x25519-dalek) | [![](https://docs.rs/x25519-dalek/badge.svg)](https://docs.rs/x25519-dalek) | [![CI](https://github.com/dalek-cryptography/curve25519-dalek/actions/workflows/x25519-dalek.yml/badge.svg?branch=main)](https://github.com/dalek-cryptography/curve25519-dalek/actions/workflows/x25519-dalek.yml)         |

There is also the [`curve25519-dalek-derive`](https://github.com/dalek-cryptography/curve25519-dalek/blob/HEAD/curve25519-dalek-derive) crate, which is just a helper crate with some macros that make curve25519-dalek easier to write.

# Contributing

Please see [`CONTRIBUTING.md`](https://github.com/dalek-cryptography/curve25519-dalek/blob/HEAD/CONTRIBUTING.md).

# Code of Conduct

We follow the [Rust Code of Conduct](http://www.rust-lang.org/conduct.html),
with the following additional clauses:

* We respect the rights to privacy and anonymity for contributors and people in
  the community.  If someone wishes to contribute under a pseudonym different to
  their primary identity, that wish is to be respected by all contributors.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:ed25519-dalek-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same BSD-3-Clause license as the upstream [`dalek-cryptography/curve25519-dalek`](https://github.com/dalek-cryptography/curve25519-dalek/tree/main/ed25519-dalek). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the curve25519-dalek authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`dalek-cryptography/curve25519-dalek`](https://github.com/dalek-cryptography/curve25519-dalek/tree/main/ed25519-dalek) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
