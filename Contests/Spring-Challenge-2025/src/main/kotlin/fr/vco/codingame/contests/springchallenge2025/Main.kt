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


data class State(var hash: Int = 0, var childId: Int = 0)


fun resolve2(initialBoard: List<Int>, depth: Int): Int {
    FINAL_HASH = List(depth + 1) { mutableMapOf() }
    val stack = MutableList(depth + 1) { State() }

    var turn = 0
    stack[0] = State(initialBoard.hash(), 0)

    while (turn >= 0) {
        val curr = stack[turn]
        computeNextBoards(curr.hash)
        if (turn == depth || NEXT_BOARDS[curr.hash]!!.isEmpty()) {
            FINAL_HASH[turn][curr.hash] = curr.hash
//            for (i in turn-1 downTo 0){
//                FINAL_HASH[i][stack[i].hash] = FINAL_HASH[i][stack[i].hash]!! + curr.hash
//            }

        }


        if ((curr.childId == 0 && FINAL_HASH[turn][curr.hash] != null) ||
            curr.childId >= NEXT_BOARDS[curr.hash]!!.size
        ) {
            // This state is already computed
            turn--
            if (turn >= 0) {
                FINAL_HASH[turn][stack[turn].hash] =
                    (FINAL_HASH[turn][stack[turn].hash]!! + FINAL_HASH[turn + 1][curr.hash]!!) and MODULO_30_MASK
            }
        } else {
            if (curr.childId == 0) {
                FINAL_HASH[turn][curr.hash] = 0
            }
            turn++
            stack[turn].hash = NEXT_BOARDS[curr.hash]!![curr.childId]
            stack[turn].childId = 0

            curr.childId++
        }
    }
    return FINAL_HASH[0][stack[0].hash]!!
}

fun getFinalHash(hash: Int, turn: Int): Int {

    if (FINAL_HASH[turn][hash] != null) {
        return FINAL_HASH[turn][hash]!!
    }

    if (turn == 0 || getNextBoards(hash).isEmpty()) {
        FINAL_HASH[turn][hash] = hash
        return hash
    }

    FINAL_HASH[turn][hash] = 0
    getNextBoards(hash).forEach { nextHash ->
        val finalHash = getFinalHash(nextHash, turn - 1)
        FINAL_HASH[turn][hash] = (FINAL_HASH[turn][hash]!! + finalHash) and MODULO_30_MASK
    }

    return FINAL_HASH[turn][hash]!!
}

val NEXT_BOARDS = mutableMapOf<Int, List<Int>>()
fun getNextBoards(hash: Int): List<Int> {
    if (NEXT_BOARDS[hash] != null) {
        return NEXT_BOARDS[hash]!!
    }
    val board = hash.getBoard()
    val next = mutableSetOf<Int>()
    for (i in 0 until BOARD_SIZE) {
        if (board[i] == 0) {
            var capture = false
            for (combination in NEIGHBOURS_COMBINATIONS[i]) {
                var sum = 0

                for (n in combination) {
                    if ( board[n] == 0) {
                        sum = 7
                        break
                    }
                    sum +=  board[n]
                }

                if(sum <= 6) {
                    capture = true
                    next.add(
                        board.toMutableList().apply {
                            this[i] = sum
                            combination.forEach { n -> this[n] = 0 }
                        }.hash()
                    )
                }
            }
            if(!capture) {
                next.add(board.toMutableList().apply { this[i] = 1 }.hash())
            }
//        if (board[i] == 0) {
//            val combinations = NEIGHBOURS_COMBINATIONS[i]
//                .map { combination -> combination.sumOf { n -> board[n] } }
//                .mapIndexedNotNull { j, total -> if (total <= 6 && NEIGHBOURS_COMBINATIONS[i][j].count { n -> board[n] != 0 } >= 2) j to total else null }
//
//            if (combinations.isNotEmpty()) {
//                combinations.forEach { (j, total) ->
//                    next.add(
//                        board.toMutableList().apply {
//                            this[i] = total
//                            NEIGHBOURS_COMBINATIONS[i][j].forEach { n -> this[n] = 0 }
//                        }.hash()
//                    )
//                }
//            } else {
//                next.add(board.toMutableList().apply { this[i] = 1 }.hash())
//            }
        }
    }
    NEXT_BOARDS[hash] = next.toList()
    return NEXT_BOARDS[hash]!!
}

fun computeNextBoards(hash: Int) {
    if (NEXT_BOARDS[hash] != null) return
    val board = hash.getBoard()
    val next = mutableSetOf<Int>()
    for (i in 0 until BOARD_SIZE) {
        if (board[i] == 0) {
            var capture= false
            for (combination in NEIGHBOURS_COMBINATIONS[i]) {
                var sum = 0

                for (n in combination) {
                    if ( board[n] == 0) {
                        sum = 7
                        break
                    }
                    sum +=  board[n]
                }

                if(sum <= 6) {
                    capture = true
                    next.add(
                        board.toMutableList().apply {
                            this[i] = sum
                            combination.forEach { n -> this[n] = 0 }
                        }.hash()
                    )
                }
            }
            if(!capture) {
                next.add(board.toMutableList().apply { this[i] = 1 }.hash())
            }


//            val combinations = NEIGHBOURS_COMBINATIONS[i]
//                .map { combination -> combination.sumOf { n -> board[n] } }
//                .mapIndexedNotNull { j, total -> if (total <= 6 && NEIGHBOURS_COMBINATIONS[i][j].count { n -> board[n] != 0 } >= 2) j to total else null }
//
//            if (combinations.isNotEmpty()) {
//                combinations.forEach { (j, total) ->
//                    next.add(
//                        board.toMutableList().apply {
//                            this[i] = total
//                            NEIGHBOURS_COMBINATIONS[i][j].forEach { n -> this[n] = 0 }
//                        }.hash()
//                    )
//                }
//            } else {
//                next.add(board.toMutableList().apply { this[i] = 1 }.hash())
//            }
        }
    }
    NEXT_BOARDS[hash] = next.toList()
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

