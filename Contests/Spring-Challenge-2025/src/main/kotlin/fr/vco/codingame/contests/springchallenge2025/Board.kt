package fr.vco.codingame.contests.springchallenge2025

/**** BOARD ****/
typealias Board = Int

const val BOARD_SIZE = 9

fun List<List<Int>>.toBoard(): Board {
    return this[0][0] or (this[0][1] shl 3) or (this[0][2] shl 6) or
            (this[1][0] shl 9) or (this[1][1] shl 12) or (this[1][2] shl 15) or
            (this[2][0] shl 18) or (this[2][1] shl 21) or (this[2][2] shl 24)
}

fun Board.add(i: Int, value: Int): Board {
    return this or (value shl i * 3)
}

fun Board.get(i: Int): Board {
    return (this shr i * 3) and 7
}

fun Board.isFinal(): Boolean {
    return (this and 7) != 0 &&
            ((this shr 3) and 7) != 0 &&
            ((this shr 6) and 7) != 0 &&
            ((this shr 9) and 7) != 0 &&
            ((this shr 12) and 7) != 0 &&
            ((this shr 15) and 7) != 0 &&
            ((this shr 18) and 7) != 0 &&
            ((this shr 21) and 7) != 0 &&
            ((this shr 24) and 7) != 0
}

fun Board.toSum(): Int {
    return (this and 7) * 100_000_000 +
            ((this shr 3) and 7) * 10_000_000 +
            ((this shr 6) and 7) * 1_000_000 +
            ((this shr 9) and 7) * 100_000 +
            ((this shr 12) and 7) * 10_000 +
            ((this shr 15) and 7) * 1_000 +
            ((this shr 18) and 7) * 100 +
            ((this shr 21) and 7) * 10 +
            ((this shr 24) and 7)
}

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

fun computeNextBoards(state: StackState) {
    var nextId = 0
    for (i in 0 until BOARD_SIZE) {
        if (state.board and (7 shl i * 3) == 0) {
            var capture = false
            for (combination in NEIGHBOURS_COMBINATIONS[i]) {
                var sum = 0
                var nextBoard = state.board
                for (n in combination) {
                    val remove = state.board and (7 shl n * 3)
                    if (remove == 0) {
                        sum = 7
                        break
                    }
                    sum += state.board.get(n)
                    nextBoard -= remove
                }

                if (sum <= 6) {
                    capture = true
                    state.childs[nextId++] = nextBoard.add(i, sum)
                }
            }
            if (!capture) {
                state.childs[nextId++] = state.board.add(i, 1)
            }
        }
    }
    state.childId = nextId-1
}
