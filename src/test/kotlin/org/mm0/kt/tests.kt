package org.mm0.kt

const val failFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/fail"
const val passFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/pass"

fun String.toCamelCase() = split(' ').filterNot { it.isBlank() }.map { it.sanitizeForFileName() }.joinToString("") { if (it.length == 1) it.toUpperCase() else it[0].toUpperCase() + it.substring(1).toLowerCase() }
fun String.sanitizeForFileName() = map { if (it.isLetterOrDigit()) it else "${it.toInt()}_" }.joinToString("")

fun String.fail(vararg list: M, fileName: String = "") = Test.Fail(this, list.toList(), fileName)
fun String.pass(vararg list: M, fileName: String = "") = Test.Pass(this, list.toList(), fileName)

sealed class Test(val reason: String, val list: List<M>, fileName: String) {
    val name = uniqueFileName(if (fileName != "") fileName else reason.toCamelCase())
    class Pass(reason: String, list: List<M>, fileName: String) :Test(reason, list, fileName)
    class Fail(reason: String, list: List<M>, fileName: String) :Test(reason, list, fileName)
}

val filenames = mutableMapOf<String, Int>()
fun uniqueFileName(name: String) :String {
    val int = 1 + filenames.getOrDefault(name, 0)
    filenames[name] = int
    return if (int == 1) name else "${name}_$int"
}

fun Test.writeFiles() {
    val folder = if (this is Test.Fail) failFolder else passFolder
    val mm0 = MM0FileWriter("$folder/$name.mm0")
    val mmu = MMUFileWriter("$folder/$name.mmu")
    for (m in list) {
        when (m) {
            is M.Computer.Sort -> {
                mm0.add(m)
                mmu.mmu(m)
            }
        }
    }
    mm0.close()
    mmu.close()
}


fun sort(name: String, isPure: Boolean = false, isStrict: Boolean = false, isProvable: Boolean = false, isFree: Boolean = false) = M.Computer.Sort(name, isPure, isStrict, isProvable, isFree)
fun String.toType(): Type = split(' ').let { Type(it.first(), it.drop(1)) }
fun String.toBinder(): Binder = split(':').let { list -> if (list[0] == ".") Binder(true, list[1], list[2].toType()) else Binder(false, list[0], list[1].toType()) }
fun term(id: String, type: String, vararg binders: String) = M.Computer.Term(id, type = type.toType(), binders = binders.map { it.toBinder() })
