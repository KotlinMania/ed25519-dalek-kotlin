=== Deep Analysis: tmp/ed25519-dalek/src (rust) -> src/commonMain/kotlin (kotlin) ===

Scanning source codebase (rust)...
Codebase: tmp/ed25519-dalek/src (rust)
  Files: 10
  Total imports: 80
  Most depended: context (3 dependents)

Scanning target codebase (kotlin)...
Codebase: src/commonMain/kotlin (kotlin)
  Files: 6
  Total imports: 4

Comparing codebases...
Computing AST similarities...

=== Codebase Comparison Report ===

Source: tmp/ed25519-dalek/src (10 files)
Target: src/commonMain/kotlin (6 files)
Scoring invariant: FnSim is required function body/parameter similarity. Class/type and symbol parity are reported beside it; whole-file shape is diagnostic only.

Matched:   3 files
Unmatched: 7 source, 2 target

=== Matched Files (by porting priority) ===

Source                        Target                        FnSim     Dependents FunctionParityTypeParity  Priority
 --------------------------------------------------------------------------------------------------------------------------
context                       ed25519dalek.Context          0.63      3          3/4           1/1         3010503.8 
constants                     ed25519dalek.Constants [ZERO] 0.00      1          0/0           0/0         1000010.0 
errors                        ed25519dalek.Errors           0.15      0          1/2           2/2         10408.5   

=== Function and Symbol Details ===

context -> ed25519dalek.Context
  similarity: 0.63, priority: 3010503.8, dependents: 3
  functions: 3/4 matched (target total: 3, required body score: 0.63)
  missing functions: context_correctness
  types: 1/1 matched (target total: 1)
  missing types: none
  tests: 0/1 matched

constants -> ed25519dalek.Constants [ZERO]
  similarity: 0.00, priority: 1000010.0, dependents: 1
  functions: 0/0 matched (target total: 1, required body score: 0.00)
  missing functions: none
  types: 0/0 matched (target total: 1)
  missing types: none
  *** CHEAT DETECTION / SCORING FAILURE ***
  function-by-function score forced to 0: no source functions found; target defines functions; report scoring is function-by-function only

errors -> ed25519dalek.Errors
  similarity: 0.15, priority: 10408.5, dependents: 0
  functions: 1/2 matched (target total: 10, required body score: 0.15)
  missing functions: fmt
  types: 2/2 matched (target total: 9)
  missing types: none


=== Scores Forced To 0 ===

  - constants -> ed25519dalek.Constants: no source functions found; target defines functions; report scoring is function-by-function only

=== Missing from Target (need to port) ===

File                          Deps    Path
------------------------------------------------------------------------------
signature                     1       signature.rs
batch                         0       batch.rs
hazmat                        0       hazmat.rs
lib                           0       lib.rs
signing                       0       signing.rs
verifying                     0       verifying.rs
verifying.stream              0       verifying/stream.rs

=== Porting Quality Summary ===

Matched by exact header:          3 / 3
Matched by provenance fallback:   0 / 3
Matched by name:                  0 / 3
Total TODOs in target: 0
Total lint errors:    0
Stub files:           0

=== Big Picture ===

- Missing files: 7
- Incomplete ports (similarity < 60%): 2
- Stub files: 0
- Files missing functions: 2 (total deficit: 2 functions)
- Type definitions missing: 0
- Files missing tests: 1 (total deficit: 1 unported `#[test]` functions)
- Documentation coverage: 62 / 162 lines (38%)

Primary focus: create missing files (highest deps first)

=== Files with Issues ===

File                          Similarity LineRatio  FunctionParityTests     TODOs Lint  Status
----------------------------------------------------------------------------------------------------
ed25519dalek.Context          0.63       0.00       3/4           0/1       0     0     MISSING_FUNCS
  missing functions: `context_correctness`
ed25519dalek.Constants [ZERO  0.00       0.00       -             -         0     0     LOW_SIM
ed25519dalek.Errors           0.15       0.00       1/2           -         0     0     LOW_SIM
  missing functions: `fmt`

=== Porting Recommendations ===

Incomplete ports (similarity < 60%): 2
Missing files: 7

Incomplete ports to complete:
  constants                      similarity=0.00 function_parity=- dependents=1
  errors                         similarity=0.15 function_parity=1/2 dependents=0
    missing functions: `fmt`

=== Missing Files (by Dependents) ===

Source File                   Expected Target                       Dependents Path
-----------------------------------------------------------------------------------------------------------------------
signature                     Signature                             1          signature.rs
batch                         Batch                                 0          batch.rs
hazmat                        Hazmat                                0          hazmat.rs
signing                       Signing                               0          signing.rs
verifying                     verifying.Verifying                   0          verifying.rs
verifying.stream              verifying.Stream                      0          verifying/stream.rs

=== Reexport / Wiring Modules (consult, don't transliterate) ===

lib                           Lib                                   0          lib.rs

=== Documentation Gaps ===

There is missing documentation that is hurting overall scoring.
Documentation coverage: 62 / 162 lines (38%)
Files with >20% doc gap: 3

File                          Src Docs    Tgt Docs    Gap %     DocSim    DocAmt    DocEq     
----------------------------------------------------------------------------------------------
context                       92          18          80%       0.62      0.20      0.41      
errors                        54          34          37%       1.00      0.63      0.81      
constants                     16          10          37%       1.00      0.62      0.81      

=== Generating Reports ===

✅ Generated: port_status_report.md
✅ Generated: high_priority_ports.md
✅ Generated: NEXT_ACTIONS.md
✅ Generated: port_lint_proposed_changes.md

📁 All reports generated successfully!
