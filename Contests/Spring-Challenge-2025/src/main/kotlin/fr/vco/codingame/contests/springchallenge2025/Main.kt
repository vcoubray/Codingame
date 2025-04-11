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
    val die = List(3){readln().split(" ")}.flatten()
    var board = 0
    repeat(BOARD_SIZE) {
        board = board.add(it, die[it].toInt())
    }

    println(resolve(board, depth))
}

//var FINAL_HASH: List<HashMap<Int, Int>> = emptyList()
lateinit var FINAL_HASH2: HashMap<Long, Int> //= HashMap(4_000_000, 0.95f)
fun resolve(initialBoard: Int, depth: Int): Int {
    FINAL_HASH2 = HashMap(4_000_000, 0.95f)
//    FINAL_HASH = List(depth + 1) { HashMap(100_000, 0.95f) }
    return getFinalHash(initialBoard, depth)
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

    if (turn == 0 || board.isFinal()) {
        return board.toHash()
    }
    val key = board*41L+turn
//    var hash = FINAL_HASH[turn][board]
    var hash = FINAL_HASH2[key]
    if (hash != null) {
        return hash
    }

    hash = 0
    getNextBoards(board).forEach { nextBoard ->
        val finalHash = getFinalHash(nextBoard, turn - 1)
        hash = (hash!! + finalHash) and MODULO_30_MASK
    }

//    FINAL_HASH[turn][board] = hash!!
    FINAL_HASH2[key] = hash!!
    return hash!!
}

fun getNextBoards(board: Board): List<Int> {

    val next =  mutableListOf<Int>()
    for (i in 0 until BOARD_SIZE) {
        if (board and BOARD_MASKS[i] == 0) {
            var capture = false
            for (combination in NEIGHBOURS_COMBINATIONS[i]) {
                var sum = 0
                var nextBoard = board
                for (n in combination) {
                    val remove = board and BOARD_MASKS[n]
                    if (remove == 0) {
                        sum = 7
                        break
                    }
                    sum += board.get(n)
                    nextBoard -= remove
                }

                if(sum <= 6) {
                    capture = true
                    next.add(nextBoard.add(i,sum))
                }
            }
            if(!capture) {
                next.add(board.add(i, 1))
            }
        }
    }

    return next.toList()
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


typealias Board = Int

fun Board.add(i : Int, value: Int): Board{
    return this + (value shl i*3)
}

fun Board.get (i:Int): Board {
    return (this shr i*3) and 7
}

fun Board.isFinal(): Boolean {
    repeat(BOARD_SIZE){
        if( this.get(it) == 0 ) return false
    }
    return true
}

fun Board.toHash() : Int {
    var hash = 0
    repeat(BOARD_SIZE){ hash = 10 * hash + this.get(it) }
    return hash
}


val BOARD_MASKS = List(9){ 7 shl it*3}