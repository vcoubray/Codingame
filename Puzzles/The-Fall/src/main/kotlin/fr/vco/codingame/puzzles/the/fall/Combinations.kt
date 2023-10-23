package fr.vco.codingame.puzzles.the.fall

fun <T : Any> List<List<T>>.getCombination(i: Long): List<T> {

    return buildList {
        var index = i
        this@getCombination.filter { it.isNotEmpty() }.forEach {
            val current = index % it.size
            index /= it.size
            this.add(it[current.toInt()])
        }
    }
}

fun <T : Any> List<List<T>>.getCombinationCount(): Long {
    return this.filter { it.isNotEmpty() }.fold(1L) { acc, a -> a.size * acc }
}

fun <T : Any> List<List<T>>.getCombinations(): List<List<T>> {
    return List(getCombinationCount().toInt()) { getCombination(it.toLong()) }
}