package org.mm0.kt


import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertFails



expect fun filePathOf(folderPath: String): Sequence<String>


val testsFolder = "/home/lakedaemon/IdeaProjects/mephistolus/src/mm0/"
val mm0MmuFolder = "/home/lakedaemon/IdeaProjects/upstreamMM0/mm0/tests/mm0_mmu"

class Tests {
    @Test
    @JsName("testForMM0Parser")
    fun `MM0 Parser test`() {
        for (mm0 in mm0SequenceOf(string(testsFolder, "string.mm0"), simpleCanonizer)) println(mm0.toString())
    }


    /*@DisplayName("Should create shapes with different numbers of sides")
    @ValueSource(ints = [3, 4, 5, 8, 14])
    fun shouldCreateShapesWithDifferentNumbersOfSides(expectedNumberOfSides: Int) {
    }*/


    private fun testMM0Parsing(folder: String, name: String, list: MutableList<String>?) = mm0SequenceOf(string(folder, name), simpleCanonizer).forEach { list?.add(it.toString()) }

    /*
        private fun testMMUParsing(name: String, folder: String) = try {
            mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer).toList()
        } catch (t: Throwable) {
            println("warning : $t")
            throw t
        }

        private fun testProofCheck(name: String, folder: String) = try {
            ContextBuilderImpl().asyncCheck(mm0SequenceOf(string("$name.mm0", folder), simpleCanonizer), mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer), ::simpleCheck)
        } catch (t: Throwable) {
            println("warning : $t")
            throw t
        }
    */
    fun wrap(shouldFail: Boolean, action: () -> Unit): Any = if (shouldFail) assertFails { action() } else action()


    @Test
    fun evaluateTests() {
        val results = mutableMapOf<String, Exception?>()
        for (folderPath in filePathOf(mm0MmuFolder)) {
            if (folderPath.endsWith( "run-mm0-hs.sh")) continue
            val type = when {
                folderPath.endsWith("fail") -> "fail"
                folderPath.endsWith("pass") -> "pass"
                folderPath.endsWith("run") -> "run"
                else -> error("not supported $folderPath")
            }
            for (filePath in filePathOf(folderPath)) {
                val fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length)
                when {
                    filePath.endsWith(".mm0") -> {
                        val list = mutableListOf<String>()
                        val exception = try {
                            testMM0Parsing(folderPath, fileName, list)
                            null
                        } catch (e: Exception) {
                            e
                        }
                        when (type) {
                            "fail" -> results[filePath] = if (exception != null) null else Exception("expected exception but parsed and got $list")
                            "pass" -> results[filePath] = if (exception == null) null else Exception("expected exception but parsed and got $exception\n after $list")
                            "run" -> results[filePath] = exception
                        }
                    }
                }
            }
        }
        // display results
        val isFailure = results.any{(it.key.contains("fail") ||it.key.contains("pass")) && it.value!= null }
        val message = """
            Fail ${results.count{ it.key.contains("fail") && it.value!= null }}/${results.count{ it.key.contains("fail")}}
            ${results.entries.filter { it.key.contains("fail") && it.value != null }.joinToString("\n") { "${it.key} : ${it.value}" }}
            Pass ${results.count{ it.key.contains("pass") && it.value!= null }}/${results.count{ it.key.contains("pass")}}
            ${results.entries.filter { it.key.contains("pass") && it.value != null }.joinToString("\n") { "${it.key} : ${it.value}" }}
            Run ${results.count{ it.key.contains("run") && it.value== null }}/${results.count{ it.key.contains("run")}}
             ${results.entries.filter { it.key.contains("run") && it.value != null }.joinToString("\n") { "${it.key} : ${it.value}" }}
            """
        if (isFailure) error(message) else println(message)
    }
}

