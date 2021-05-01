package org.mm0.kt


import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertFails

expect fun filePathOf(folderPath: String): Sequence<String>


private const val testsFolder = "/home/lakedaemon/IdeaProjects/mephistolus/src/mm0/"
private const val mm0MmuFolder = "/home/lakedaemon/IdeaProjects/upstreamMM0/mm0/tests/mm0_mmu"

class Tests {
    private fun testMM0Parsing(folder: String, name: String, list: MutableList<String>?) = mm0SequenceOf(string(folder, name), simpleCanonizer).forEach { list?.add(it.toString()) }
    private fun testMMUParsing(folder: String, name: String, list: MutableList<String>?) = mmuSequenceOf(string(folder, name), simpleCanonizer).forEach { list?.add(it.toString()) }
    private fun testProofCheck(folder: String, name: String, list: MutableList<String>?) = ContextBuilderImpl().asyncCheck(mm0SequenceOf(string(folder, "$name.mm0"), simpleCanonizer), mmuSequenceOf(string(folder, "$name.mmu"), simpleCanonizer), ::simpleCheck).forEach { list?.add(it.toString()) }


    fun runTestShouldFail(sharedName:String):Boolean = when(sharedName) {
     "bad-int1" -> true
        "math-string" -> false
     else -> error("unsupported $sharedName")
    }

    @Test
    @JsName("filePassAndRunTests")
    /** when we have parameterized common tests simplify this */
    fun `fail, pass and run tests`() {
        val results = mutableMapOf<String, Exception?>()

        val files = mutableMapOf<String, Int>()

        // first classify all files
        for (folderPath in filePathOf(mm0MmuFolder)) {
            if (folderPath.endsWith("run-mm0-hs.sh")) continue
            for (filePath in filePathOf(folderPath)) {
                val sharedPath = filePath.substring(0, filePath.length - 4)
                files[sharedPath] = files.getOrElse(sharedPath) { 0 } or (if (filePath.endsWith(".mm0")) 1 else 2)
            }
        }

        fun test(sharedPath: String, test: (String, String, MutableList<String>) -> Unit) {
            val list = mutableListOf<String>()
            val folderPath = sharedPath.substring(0, sharedPath.lastIndexOf('/'))
            val sharedName = sharedPath.substring(sharedPath.lastIndexOf('/') + 1, sharedPath.length)
            val type = when {
                sharedPath.contains("/fail/") -> "fail"
                sharedPath.contains("/pass/") -> "pass"
                sharedPath.contains("/run/") -> "run"
                else -> error("not supported $sharedPath")
            }
            val exception = try {
                test(folderPath, sharedName, list)
                null
            } catch (e: Exception) {
                e
            }
           // val sharedWithoutExtension = sharedName.substring(0, sharedName.lastIndexOf('.'))
            when (type) {
                "fail" -> results[sharedPath] = if (exception != null) null else Exception("for $sharedName expected exception but parsed and got $list")
                "pass" -> results[sharedPath] = if (exception == null) null else Exception("for $sharedName expected null but parsed and got $exception\n after $list", exception)
                "run" -> results[sharedPath] = if ((exception != null) == runTestShouldFail(sharedPath.name())) null else Exception("for $sharedName we should have ${if (runTestShouldFail(sharedPath.name())) "failed" else "passed"}", exception)
            }
        }

        for ((path, mask) in files) when (mask) {
                1 -> test("$path.mm0") { f, name, list -> testMM0Parsing(f, name, list) }
                2 -> test("$path.mmu") { f, name, list -> testMMUParsing(f, name, list) }
                3 -> {
                    test("$path.mm0") { f, name, list -> testMM0Parsing(f, name, list) }
                    test("$path.mmu") { f, name, list -> testMMUParsing(f, name, list) }
                    test(path) { f, name, list ->testProofCheck(f, name, list)}
                }
            }

        // display results
        val isFailure = results.any { (it.key.contains("fail") || it.key.contains("pass")) && it.value != null }
        val message = """
            Fail ${results.count { it.key.contains("fail") && it.value != null }}/${results.count { it.key.contains("fail") }}
            ${results.entries.filter { it.key.contains("fail") && it.value != null }.joinToString("\n") { "${it.key} : ${it.value} ${it.value?.printStackTrace()}" }}
            Pass ${results.count { it.key.contains("pass") && it.value != null }}/${results.count { it.key.contains("pass") }}
            ${results.entries.filter { it.key.contains("pass") && it.value != null }.joinToString("\n") { "${it.key} : ${it.value} ${it.value?.printStackTrace()}" }}
            Run ${results.count { it.key.contains("run") && it.value != null }}/${results.count { it.key.contains("run") }}
             ${results.entries.filter { it.key.contains("run") && it.value != null }.joinToString("\n") { "${it.key} : ${it.value} ${it.value?.printStackTrace()}" }}
            """
        if (isFailure) error(message) else println(message)
    }
    fun String.name() :String{
        val i1 = lastIndexOf('/')
        val i2 = lastIndexOf('.')
return substring(i1+1, i2)
    }
}