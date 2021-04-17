# mm0kt

The aim of this code is to implement in (pure) Kotlin
- a mm0 parser
- a mmu parser
- a (mm0 + mmu) proofchecker
- a patcher for set.mm.mm0 and set.mm.mmu
- a patch mm0-ifying set.mm

The main goal of this public project is the mm0-ification of set.mm (a subset of maths). 

Performance of the parsers/proofcheckers/patchers is not a goal (it is good enough). 

TODO : 
- fix bugs : 
   - notations aren't handled correctly in the Dynamic Parser
- proofCheck each theorem in a dedicated coroutine
- port the patcher to the new architecture
- improve the patch
