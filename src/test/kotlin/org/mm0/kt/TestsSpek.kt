package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.style.specification.xdescribe
import java.io.File
import kotlin.test.assertFails

object TestsSpek : Spek({
                                   fun string(filename: String, folder: String) = File("$folder/$filename").readText(Charsets.US_ASCII)
                                   fun proofCheck(name: String, folder: String) = try{ContextBuilderImpl().asyncCheck(mm0SequenceOf(string("$name.mm0", folder), simpleCanonizer), mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer), ::simpleCheck)} catch(t:Throwable) {
                                       println("warning : $t")
                                       throw t
                                   }
                                   fun check(name: String, shouldFail: Boolean): Any = if (shouldFail) assertFails { proofCheck(name, failFolder) } else proofCheck(name, passFolder)
                                   fun MutableMap<String, Int>.unique(reason: String): String {
                                       val int = 1 + getOrDefault(reason, 0)
                                       this[reason] = int
                                       return if (int == 1) reason else "$reason$int"
                                   }

                                   fun Suite.evaluateTests(folder: String) {
                                       val reasons = mutableMapOf<String, Int>()
                                       File(folder).listFiles().orEmpty().sorted().forEach {
                                           val fileName = it.name
                                           if (fileName.endsWith(".mmu")) {
                                               val name = fileName.substring(0, fileName.length - 4)
                                               it(reasons.unique(name)) { check(name, folder == failFolder) }
                                           }
                                       }
                                   }

                                   describe("write test files") {

                                       it("tests that should fail") {
                                           File(failFolder).deleteRecursively()
                                           File(failFolder).mkdir()
                                           fail() }
                                       it("tests that should pass") {
                                           File(passFolder).deleteRecursively()
                                           File(passFolder).mkdir()
                                           pass() }
                                   }
                                   describe("fail tests") { evaluateTests(failFolder) }
                                   describe("pass tests") { evaluateTests(passFolder) }
                               })