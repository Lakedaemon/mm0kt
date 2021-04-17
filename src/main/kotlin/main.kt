import org.mm0.kt.*

fun main(args: Array<String>) {
    val file = args[1]
    val path = file.substring(0, file.lastIndexOf('.'))
    proofCheck(path)
}