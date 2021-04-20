package org.mm0.kt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.GroupBody
import org.spekframework.spek2.meta.*
import org.spekframework.spek2.style.specification.Suite
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.style.specification.xdescribe
import java.io.File
import kotlin.test.assertFails

object WriteTestsSpek : Spek({
                            describe("write test files") {

                                it("tests that should fail") {
                                    File(failFolder).deleteRecursively()
                                    File(failFolder).mkdir()
                                    fail()
                                }
                                it("tests that should pass") {
                                    File(passFolder).deleteRecursively()
                                    File(passFolder).mkdir()
                                    pass()
                                }
                            }

                        })