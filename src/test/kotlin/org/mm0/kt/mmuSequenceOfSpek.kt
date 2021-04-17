package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object mmuSequenceOfSpek : Spek({
                                  describe("proof checking stuff") {
                                      val folder = "/home/lakedaemon/IdeaProjects/mephistolus/src/mm0/"
                                      fun string(filename: String) = File("$folder$filename").readText()
                                      fun seq(name:String) = mmuSequenceOf(string("$name.mmu"), simpleCanonizer)
                                      it("mmuSequenceOf(string)") { for (mmu in seq("string")) println(mmu.toString())}
                                  }
                              })