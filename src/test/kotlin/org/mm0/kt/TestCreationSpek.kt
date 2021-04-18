package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import kotlin.test.assertFails

object CreateTestSpek : Spek({

                                 fun string(filename: String, folder: String) = File("$folder/$filename").readText()
                                 fun proofCheck(name: String, folder: String) = ContextBuilderImpl().asyncCheck(mm0SequenceOf(string("$name.mm0", folder), simpleCanonizer), mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer), ::simpleCheck)
                                 fun failCheck(name: String) = assertFails { proofCheck(name, failFolder) }




                                 describe("write test files") {
                                     for (test in failTests + passTests) it("write files for " + test.reason) { test.writeFiles() }
                                 }

                                 describe("fail tests") {
                                     val reasons = mutableMapOf<String, Int>()
                                     fun uniqueReason(reason: String): String {
                                         val int = 1 + reasons.getOrDefault(reason, 0)
                                         reasons[reason] = int
                                         return if (int == 1) reason else "$reason $int"
                                     }
                                     for (failTest in failTests) it(uniqueReason(failTest.reason)) { failCheck(failTest.name) }
                                 }

                                 describe("pass tests") {
                                     val reasons = mutableMapOf<String, Int>()
                                     fun uniqueReason(reason: String): String {
                                         val int = 1 + reasons.getOrDefault(reason, 0)
                                         reasons[reason] = int
                                         return if (int == 1) reason else "$reason $int"
                                     }
                                     for (test in passTests) it(uniqueReason(test.reason)) { failCheck(test.name) }
                                 }

                             })