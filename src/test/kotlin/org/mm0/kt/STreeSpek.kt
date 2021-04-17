package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals


object STreeSpek : Spek({
                            describe("red black trees") {
                                val tree0: STree<Int>? = null
                                val tree1 = tree0.put("one", 1)
                                val tree2 = tree1.put("two", 2)
                                val tree3 = tree2.put("three", 3)
                                val tree4 = tree3.put("four", 4)

                                it("find stuff") {
                                    assertEquals(null, tree0.find("two"))
                                    assertEquals(null, tree1.find("two"))
                                    assertEquals(2, tree2.find("two"))
                                    assertEquals(2, tree3.find("two"))
                                    assertEquals(2, tree4.find("two"))
                                }

                                it("walk tree in order") {
                                    assertEquals(listOf(1), tree0.values().toList())
                                    assertEquals(listOf(1), tree1.values().toList())
                                    assertEquals(listOf(1, 2), tree2.values().toList())
                                    assertEquals(listOf(1, 2, 3), tree3.values().toList())
                                    assertEquals(listOf(1, 2, 3, 4), tree4.values().toList())
                                }

                            }
/*        it("persist data") {


    }*/

                            /*describe("checking patched files") {
                                it("set.mm", timeout = 30000L) {
                                    val patchedMm0String = File("/home/lakedaemon/IdeaProjects/mephistolus/src/mm0/patched_set.mm.mm0").readText()
                                    val patchedMmuString = File("/home/lakedaemon/IdeaProjects/mephistolus/src/mm0/patched_set.mm.mmu").readText()
                                    val tim = timeMs()
                                    assertEquals(true, MMUVerifier2(patchedMm0String, patchedMmuString).check().apply { debug("${timeMs() - tim}") })
                                }
                            }*/
                        })