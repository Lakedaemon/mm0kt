package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.GroupBody
import org.spekframework.spek2.style.specification.describe
import java.io.File
import kotlin.test.assertFails

object TestsSpek : Spek({
                            fun string(filename: String, folder: String) = File("$folder/$filename").readText(Charsets.US_ASCII)


                            fun testMM0Parsing(name: String, folder: String) = try {
                                println(mm0SequenceOf(string("$name.mm0", folder), simpleCanonizer).toList())
                            } catch (t: Throwable) {
                                println("warning : $t")
                                throw t
                            }

                            fun testMMUParsing(name: String, folder: String) = try {
                                mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer).toList()
                            } catch (t: Throwable) {
                                println("warning : $t")
                                throw t
                            }

                            fun testProofCheck(name: String, folder: String) = try {
                                ContextBuilderImpl().asyncCheck(mm0SequenceOf(string("$name.mm0", folder), simpleCanonizer), mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer), ::simpleCheck)
                            } catch (t: Throwable) {
                                println("warning : $t")
                                throw t
                            }

                            fun wrap(shouldFail: Boolean, action: () -> Unit): Any = if (shouldFail) assertFails { action() } else action()

                            fun MutableMap<String, Int>.unique(reason: String): String {
                                val int = 1 + getOrDefault(reason, 0)
                                this[reason] = int
                                return if (int == 1) reason else "$reason$int"
                            }

                            val parsingSet = setOf("parsingMM0", "parsingBoth", "parsingMMU")


                            fun description(name:String, shouldFail:Boolean) = "$name should ${if (shouldFail) "fail" else "pass"}"
                            fun GroupBody.evaluateTests() {
                                for (folder in listOf(failFolder, passFolder)) {
                                    val shouldFail = folder == failFolder
                                    File(folder).listFiles().orEmpty().forEach { f ->
                                        if (f.isDirectory) describe(description(f.name, shouldFail) ) {
                                            val reasons = mutableMapOf<String, Int>()
                                            f.listFiles().orEmpty().sorted().forEach {
                                                val fileName = it.name
                                                val isMM0 = fileName.endsWith(".mm0")
                                                val name = fileName.substring(0, fileName.length - 4)
                                                when {
                                                    f.name in parsingSet -> it(reasons.unique(name + " mm${if (isMM0) "0" else "u"}")) { wrap(shouldFail) { if (isMM0) testMM0Parsing(name, f.absolutePath) else testMMUParsing(name, f.absolutePath) } }
                                                    !isMM0 -> it(reasons.unique(name)) { wrap(shouldFail) { testProofCheck(name, f.absolutePath) } }
                                                    else -> Unit

                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            //evaluateTestsForParsing()
                            evaluateTests()
                        })