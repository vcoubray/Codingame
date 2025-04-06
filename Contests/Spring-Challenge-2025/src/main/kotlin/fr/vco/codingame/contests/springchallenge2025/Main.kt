package fr.vco.codingame.contests.springchallenge2025

const val MODULO_30 = 1073741824
const val MODULO_30_MASK = MODULO_30 - 1

const val BOARD_SIZE = 9

val NEIGHBOURS_COMBINATIONS = arrayOf(
    arrayOf(intArrayOf(1, 3)),
    arrayOf(intArrayOf(2, 4), intArrayOf(0, 4), intArrayOf(0, 2), intArrayOf(0, 2, 4)),
    arrayOf(intArrayOf(1, 5)),
    arrayOf(intArrayOf(4, 6), intArrayOf(0, 6), intArrayOf(0, 4), intArrayOf(0, 4, 6)),
    arrayOf(
        intArrayOf(5, 7),
        intArrayOf(3, 7),
        intArrayOf(3, 5),
        intArrayOf(3, 5, 7),
        intArrayOf(1, 7),
        intArrayOf(1, 5),
        intArrayOf(1, 5, 7),
        intArrayOf(1, 3),
        intArrayOf(1, 3, 7),
        intArrayOf(1, 3, 5),
        intArrayOf(1, 3, 5, 7)
    ),
    arrayOf(intArrayOf(4, 8), intArrayOf(2, 8), intArrayOf(2, 4), intArrayOf(2, 4, 8)),
    arrayOf(intArrayOf(3, 7)),
    arrayOf(intArrayOf(4, 8), intArrayOf(6, 8), intArrayOf(6, 4), intArrayOf(6, 4, 8)),
    arrayOf(intArrayOf(5, 7)),
)


fun main() {
    val depth = readln().toInt()
    val dies = List(3) { readln().split(" ").map { it.toInt() } }.flatten()

    println(resolve(dies, depth))
}

var FINAL_HASH: List<MutableMap<Int, Int>> = emptyList()
fun resolve(initialBoard: List<Int>, depth: Int): Int {
    FINAL_HASH = List(depth + 1) { mutableMapOf() }

    return getFinalHash(initialBoard.hash(), depth)
}

fun getFinalHash(hash: Int, turn: Int): Int {

    val board = hash.getBoard()
    if (turn == 0 || board.none { it == 0 }) {
        return hash
    }

    if (FINAL_HASH[turn][hash] != null) {
        return FINAL_HASH[turn][hash]!!
    }

    FINAL_HASH[turn][hash] = 0
    getNextDie(hash).forEach { nextHash ->
        val finalHash = getFinalHash(nextHash, turn - 1)
        FINAL_HASH[turn][hash] = (FINAL_HASH[turn][hash]!! + finalHash) and MODULO_30_MASK
    }

    return FINAL_HASH[turn][hash]!!
}

val NEXT_BOARDS = mutableMapOf<Int, Set<Int>>()
fun getNextDie(hash: Int): Set<Int> {
    val board = hash.getBoard()
    if (NEXT_BOARDS[hash] != null) {
        return NEXT_BOARDS[hash]!!
    }
    val next = mutableSetOf<Int>()
    for (i in 0 until BOARD_SIZE) {
        if (board[i] == 0) {
            val combinations = NEIGHBOURS_COMBINATIONS[i]
                .map { combination -> combination.sumOf { n -> board[n] } }
                .mapIndexedNotNull { j, total -> if (total <= 6 && NEIGHBOURS_COMBINATIONS[i][j].count { n -> board[n] != 0 } >= 2) j to total else null }

            if (combinations.isNotEmpty()) {
                combinations.forEach { (j, total) ->
                    next.add(
                        board.toMutableList().apply {
                            this[i] = total
                            NEIGHBOURS_COMBINATIONS[i][j].forEach { n -> this[n] = 0 }
                        }.hash()
                    )
                }
            } else {
                next.add(board.toMutableList().apply { this[i] = 1 }.hash())
            }
        }
    }
    NEXT_BOARDS[hash] = next
    return next
}

fun Int.getBoard(): List<Int> {
    val board = this.toString().padStart(BOARD_SIZE, '0')
    return List(BOARD_SIZE) {
        board[it].digitToInt()
    }
}

fun List<Int>.hash(): Int {
    var hash = 0
    this.forEach { hash = 10 * hash + it }
    return hash
}

