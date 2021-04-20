package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.GroupBody
import org.spekframework.spek2.meta.*
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.style.specification.xdescribe
import java.io.File
import kotlin.test.assertFails

object TestsSpek : Spek({
                            fun string(filename: String, folder: String) = File("$folder/$filename").readText(Charsets.US_ASCII)
                            fun proofCheck(name: String, folder: String) = try {
                                ContextBuilderImpl().asyncCheck(mm0SequenceOf(string("$name.mm0", folder), simpleCanonizer), mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer), ::simpleCheck)
                            } catch (t: Throwable) {
                                println("warning : $t")
                                throw t
                            }

                            fun check(folder:String, name: String, shouldFail: Boolean): Any = if (shouldFail) assertFails { proofCheck(name, folder) } else proofCheck(name, folder)
                            fun MutableMap<String, Int>.unique(reason: String): String {
                                val int = 1 + getOrDefault(reason, 0)
                                this[reason] = int
                                return if (int == 1) reason else "$reason$int"
                            }


                            fun GroupBody.evaluateTests() {
                                for (folder in listOf(failFolder, passFolder)) {
                                    val shouldFail = folder == failFolder
                                    File(folder).listFiles().orEmpty().forEach { f ->
                                        if (f.isDirectory) describe("${f.name} should ${if (shouldFail) "fail" else "pass"}") {

                                            val reasons = mutableMapOf<String, Int>()
                                            f.listFiles().orEmpty().sorted().forEach {
                                                val fileName = it.name
                                                if (fileName.endsWith(".mmu")) {
                                                    val name = fileName.substring(0, fileName.length - 4)
                                                    it(reasons.unique(name)) { check(f.absolutePath, name, shouldFail) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            evaluateTests()
                        })