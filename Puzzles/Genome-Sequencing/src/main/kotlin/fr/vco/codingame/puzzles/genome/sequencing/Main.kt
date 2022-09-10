import java.util.*


fun main() {
    val input = Scanner(System.`in`)

    List(input.nextInt()) { input.next() }
        .permutations()
        .minOf { it.length }
        .let(::println)
}

fun List<String>.permutations(): List<String> {
    return if (size <= 1) this
    else this.flatMapIndexed { i, elem ->
        this.toMutableList()
            .apply { this.removeAt(i) }
            .permutations()
            .map { elem.collapse(it) }
    }
}

fun String.collapse(s: String): String {
    for (i in indices) {
        var contains = true
        for (j in s.indices) {
            if (j + i >= length) return this + s.drop(j)
            if (s[j] != get(i + j)) {
                contains = false
                break
            }
        }
        if (contains) return this
    }
    return this + s
}