package fr.vco.codingame.contests.springchallenge2025


const val MODULO_30_MASK = 1073741823 // (2^31)-1
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

var FINAL_HASH: List<HashMap<Int, Int>> = emptyList()

fun resolve(initialBoard: List<Int>, depth: Int): Int {
    FINAL_HASH = List(depth + 1) { HashMap(100_000, 0.95f) }

    val board = initialBoard.foldIndexed(0){i, acc, a -> acc.add(i, a)}

    return getFinalHash(board, depth)
}


data class State(var hash: Int = 0, var childId: Int = 0)


//fun resolve2(initialBoard: List<Int>, depth: Int): Int {
//    FINAL_HASH = List(depth + 1) { HashMap() }
//    val stack = MutableList(depth + 1) { State() }
//
//    var turn = 0
//    stack[0] = State(initialBoard.hash(), 0)
//
//    while (turn >= 0) {
//        val curr = stack[turn]
//        computeNextBoards(curr.hash)
//        if (turn == depth || NEXT_BOARDS[curr.hash]!!.isEmpty()) {
//            FINAL_HASH[turn][curr.hash] = curr.hash
//        }
//
//
//        if ((curr.childId == 0 && FINAL_HASH[turn][curr.hash] != null) ||
//            curr.childId >= NEXT_BOARDS[curr.hash]!!.size
//        ) {
//            // This state is already computed
//            turn--
//            if (turn >= 0) {
//                FINAL_HASH[turn][stack[turn].hash] =
//                    (FINAL_HASH[turn][stack[turn].hash]!! + FINAL_HASH[turn + 1][curr.hash]!!) and MODULO_30_MASK
//            }
//        } else {
//            if (curr.childId == 0) {
//                FINAL_HASH[turn][curr.hash] = 0
//            }
//            turn++
//            stack[turn].hash = NEXT_BOARDS[curr.hash]!![curr.childId]
//            stack[turn].childId = 0
//
//            curr.childId++
//        }
//    }
//    return FINAL_HASH[0][stack[0].hash]!!
//}

fun getFinalHash(board: Int, turn: Int): Int {

    if (FINAL_HASH[turn][board] != null) {
        return FINAL_HASH[turn][board]!!
    }

    if (turn == 0 || getNextBoards(board).isEmpty()) {
        FINAL_HASH[turn][board] = board.toHash()
        return FINAL_HASH[turn][board]!!
    }

    FINAL_HASH[turn][board] = 0
    getNextBoards(board).forEach { nextBoard ->
        val finalHash = getFinalHash(nextBoard, turn - 1)
        FINAL_HASH[turn][board] = (FINAL_HASH[turn][board]!! + finalHash) and MODULO_30_MASK
    }

    return FINAL_HASH[turn][board]!!
}

//val NEXT_BOARDS = HashMap<Board, MutableList<Board>>( 300_000, 0.95f)
fun getNextBoards(board: Board): List<Int> {
//    if (NEXT_BOARDS[board] != null) {
//        return NEXT_BOARDS[board]!!
//    }

//    NEXT_BOARDS[board] = mutableListOf()
    val next = mutableListOf<Int>()
    for (i in 0 until BOARD_SIZE) {
        if (board and BOARD_MASKS[i] == 0) {
            var capture = false
            for (combination in NEIGHBOURS_COMBINATIONS[i]) {
                var sum = 0
                var remove = 0
                for (n in combination) {
                    if (board and BOARD_MASKS[n] == 0) {
                        sum = 7
                        break
                    }
                    sum += board.get(n)
                    remove += board and BOARD_MASKS[n]
                }

                if(sum <= 6) {
                    capture = true
//                    NEXT_BOARDS[board]!!.add(board.add(i,sum) - remove)
                    next.add(board.add(i,sum) - remove)
                }
            }
            if(!capture) {
//                NEXT_BOARDS[board]!!.add(board.add(i, 1))
                next.add(board.add(i, 1))

            }
        }
    }
//    return NEXT_BOARDS[board]!!
    return next
}
//
//fun computeNextBoards(hash: Int) {
//    if (NEXT_BOARDS[hash] != null) return
//    val board = hash.getBoard()
//    NEXT_BOARDS[hash] = mutableListOf()
//    for (i in 0 until BOARD_SIZE) {
//        if (board[i] == 0) {
//            var capture = false
//            for (combination in NEIGHBOURS_COMBINATIONS[i]) {
//                var sum = 0
//
//                for (n in combination) {
//                    if ( board[n] == 0) {
//                        sum = 7
//                        break
//                    }
//                    sum +=  board[n]
//                }
//
//                if(sum <= 6) {
//                    capture = true
//                    NEXT_BOARDS[hash]!!.add(
//                        board.toMutableList().apply {
//                            this[i] = sum
//                            combination.forEach { n -> this[n] = 0 }
//                        }.hash()
//                    )
//                }
//            }
//            if(!capture) {
//                NEXT_BOARDS[hash]!!.add(board.toMutableList().apply { this[i] = 1 }.hash())
//            }
//        }
//    }
//}


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

typealias Board = Int

fun Board.add(i : Int, value: Int): Board{
    return this + (value shl i*3)
}

fun Board.get (i:Int): Board {
    return (this shr i*3) and 7
}

fun Board.toHash() : Int {
    var hash = 0
    repeat(9){ hash = 10 * hash + this.get(it) }
    return hash
}


val BOARD_MASKS = List(9){ 7 shl it*3}