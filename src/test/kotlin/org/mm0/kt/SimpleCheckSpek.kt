package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object SimpleCheckSpek : Spek({
                                  describe("proof checking stuff") {
                                      val folder = "/home/lakedaemon/IdeaProjects/mephistolusEngine/mm0/"
                                      fun string(filename: String) = File("$folder$filename").readText()
                                      fun proofCheck(name: String) = ContextBuilderImpl().asyncCheck(mm0SequenceOf(string("$name.mm0"), simpleCanonizer), mmuSequenceOf(string("$name.mmu"), simpleCanonizer), ::simpleCheck)
                                      it("string") { proofCheck("string") }
                                      it("hello") { proofCheck("hello") }
                                      it("peano") { proofCheck("peano") }
                                      it("set.mm") { proofCheck("set.mm") }
                                  }
                              })