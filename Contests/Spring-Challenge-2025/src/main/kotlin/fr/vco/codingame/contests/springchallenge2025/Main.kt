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
    val die = List(3) { readln().split(" ") }.flatten()
    var board = 0
    repeat(BOARD_SIZE) {
        board = board.add(it, die[it].toInt())
    }

//    println(resolve(board, depth))
    println(resolveNonRecursive(board, depth))
}

lateinit var FINAL_HASH: HashMap<Long, Int> //= HashMap(4_000_000, 0.95f)
fun resolve(initialBoard: Int, depth: Int): Int {
    FINAL_HASH = HashMap(4_000_000, 0.95f)
    return getFinalHash(initialBoard, depth)
}


class StakState(
    var board: Int = 0,
    var hash: Int = 0,
    var childId: Int = 0,
    val next: IntArray = IntArray(21) { 0 },
    var childCount: Int = 0
)


fun resolveNonRecursive(initialBoard: Int, depth: Int): Int {
    FINAL_HASH = HashMap(4_000_000, 0.95f)
    val stack = List(depth + 1) { StakState() }
    var turn = 0
    stack[0].board = initialBoard

    while (turn >= 0) {
        val curr = stack[turn]

        // Etat final || turn > depth
        // - Ajouter le hash a l'état précédent
        // - turn --
        if (curr.board.isFinal() || turn >= depth) {
            if(turn > 0) stack[turn - 1].hash = (stack[turn - 1].hash + curr.board.toHash()) and MODULO_30_MASK
            turn--
            continue
        }

        // Board déjà calculé
        // - Ajouter le hash à l'état précédent
        // - turn --
        val key = curr.board * 41L + turn
        val hash = FINAL_HASH[key]
        if (hash != null) {
            if(turn > 0) stack[turn - 1].hash = (stack[turn - 1].hash + hash) and MODULO_30_MASK
            turn--
            continue
        }

        // Premier passage -> ChildId == 0
        // - Initialiser le hash a 0
        // - Initialiser l'état courant avec les bons childs
        // - initiliser le childCount
        if (curr.childId == 0) {
            curr.hash = 0
            computeNextBoards(curr)
        }

        // Il n'y a plus de child a vérifier : childId >= childCount
        // - Stocker le hash dans le cache
        // - turn --
        if (curr.childId >= curr.childCount) {
            FINAL_HASH[key] = curr.hash
            if(turn > 0) stack[turn - 1].hash = (stack[turn - 1].hash + curr.hash) and MODULO_30_MASK
            turn--
            continue
        }

        // Encore des enfant a vérifier
        //  - Initialiser l'état suivant avec le child courant
        //  - Initialiser le childId de l'état suivant a 0
        //  - childId ++
        //  - turn++
        stack[turn + 1].board = curr.next[curr.childId]
        stack[turn + 1].childId = 0
        curr.childId++
        turn ++

    }
    return stack[0].hash
}

fun getFinalHash(board: Int, turn: Int): Int {

    if (turn == 0 || board.isFinal()) {
        return board.toHash()
    }
    val key = board * 41L + turn
//    var hash = FINAL_HASH[turn][board]
    var hash = FINAL_HASH[key]
    if (hash != null) {
        return hash
    }

    hash = 0
    getNextBoards(board).forEach { nextBoard ->
        val finalHash = getFinalHash(nextBoard, turn - 1)
        hash = (hash!! + finalHash) and MODULO_30_MASK
    }

//    FINAL_HASH[turn][board] = hash!!
    FINAL_HASH[key] = hash!!
    return hash!!
}

fun getNextBoards(board: Board): List<Int> {

    val next = mutableListOf<Int>()
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

                if (sum <= 6) {
                    capture = true
                    next.add(nextBoard.add(i, sum))
                }
            }
            if (!capture) {
                next.add(board.add(i, 1))
            }
        }
    }

    return next.toList()
}

fun computeNextBoards(state: StakState) {
    var nextId = 0
    for (i in 0 until BOARD_SIZE) {
        if (state.board and BOARD_MASKS[i] == 0) {
            var capture = false
            for (combination in NEIGHBOURS_COMBINATIONS[i]) {
                var sum = 0
                var nextBoard = state.board
                for (n in combination) {
                    val remove = state.board and BOARD_MASKS[n]
                    if (remove == 0) {
                        sum = 7
                        break
                    }
                    sum += state.board.get(n)
                    nextBoard -= remove
                }

                if (sum <= 6) {
                    capture = true
                    state.next[nextId] = (nextBoard.add(i, sum))
                    nextId++
                }
            }
            if (!capture) {
                state.next[nextId] =(state.board.add(i, 1))
                nextId++
            }
        }
    }
    state.childCount = nextId
}


typealias Board = Int

fun Board.add(i: Int, value: Int): Board {
    return this + (value shl i * 3)
}

fun Board.get(i: Int): Board {
    return (this shr i * 3) and 7
}

fun Board.isFinal(): Boolean {
    repeat(BOARD_SIZE) {
        if (this.get(it) == 0) return false
    }
    return true
}

fun Board.toHash(): Int {
    var hash = 0
    repeat(BOARD_SIZE) { hash = 10 * hash + this.get(it) }
    return hash
}


val BOARD_MASKS = List(9) { 7 shl it * 3 }