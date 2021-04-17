package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object mm0SequenceOfSpek : Spek({
                                  describe("proof checking stuff") {
                                      val folder = "/home/lakedaemon/IdeaProjects/mephistolus/src/mm0/"
                                      fun string(filename: String) = File("$folder$filename").readText()
                                      fun seq(name:String) = mm0SequenceOf(string("$name.mm0"), simpleCanonizer)
                                      it("mmuSequenceOf(string)") { for (mm0 in seq("string")) println(mm0.toString())}
                                  }
                              })