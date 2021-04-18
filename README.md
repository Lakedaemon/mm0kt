<meta charset="utf-8" lang="en"><style class="fallback">body{visibility:hidden;}</style>

**pure kotlin tools for mm0/mmu and set.mm**


Mark deep version [Lien](https://github.com/Lakedaemon/mm0kt/blob/main/README.md.html "Mark deep version")

# Introduction

This repository implements in (pure) Kotlin:
- [x] a mm0 parser
- [x] a mmu parser
- [x] a (mm0/mmu) one-pass proofchecker
- [ ] a (pipelinable) patcher for set.mm.mm0 and set.mm.mmu
- [ ] a patch mm0-ifying set.mm


# Goals 

 - [ ] accurate and correct 
   - [x] mm0 parser
   - [x] mmu parser
   - [ ] proof-checker
 - [ ] mm0-ification of set.mm (a subset of maths) : 
   - [ ] turn most mm axioms into theorems
   - [ ] introduce definitions, operators, notations whenever sensible
 - [ ] building a sane fundation for further work on set.mm
 - [x] pure Kotlin code base: native, mobile and browser targets
 - [ ] good performance (set.mm checks in 16 s in single threaded mode)
 - [ ] low Memory usage : (proof Checking should be a streaming process)
 - [ ] good error reports
 - [ ] understand mm0/mmu in depth to build better tools (on a binary spec, with minimum work, I'm looking at you mmb) later, when it makes sense

# Non-goals

- build stuff on the binary mmb spec (we will do that much later)
- absolute performance (we wouldn't use parsing and the text based mmu spec otherwise)
- build the shortest proofs possible, in the mm0ifying process
 

# TODO : 

- fix bugs : 
   - notations aren't handled correctly in the Dynamic Parser
- export as a library (publish in mavenCentral ?)
- proofCheck each theorem in a dedicated coroutine
- port the patcher to the new architecture
- improve the patch


<!-- Markdeep: --><style class="fallback">body{visibility:hidden;white-space:pre;font-family:monospace}</style><script src="markdeep.min.js"></script><script src="https://casual-effects.com/markdeep/latest/markdeep.min.js"></script><script>window.alreadyProcessedMarkdeep||(document.body.style.visibility="visible")</script>