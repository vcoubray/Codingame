package fr.vco.codingame.contests.springchallenge2025

const val SUM_HASH_MASK = 1073741823 // (2^31)-1
const val CACHE_CAPACITY = 4_194_304

fun main() {
    val depth = readln().toInt()
    val grid = List(3) { readln().split(" ").map{it.toInt()} }

    println(resolve(grid.toBoard(), depth+1))
}

class StackState(
    var board: Int = 0,
    var key: Long = 0,
    var sum: Int = 0,
    var childId: Int = -2,
    val childs: IntArray = IntArray(21) { 0 },
)

fun resolve(initialBoard: Int, depth: Int): Int {
    val cachedSums: HashMap<Long, Int>  = HashMap(CACHE_CAPACITY, 1f)
    val stack = List(depth + 1) { StackState() }
    var turn = 1
    stack[1].board = initialBoard

    while (turn > 0) {
        val curr = stack[turn]

        // This board has not been computed yet.
        if (curr.childId == -2) {

            // The board is final (or depth is reached)
            if (turn >= depth || curr.board.isFinal()) {
                turn--
                stack[turn].sum = (stack[turn].sum + curr.board.toSum()) and SUM_HASH_MASK
                continue
            }

            curr.key = 41L * curr.board + turn
            val sum = cachedSums[curr.key]
            if (sum != null) {
                turn--
                stack[turn].sum = (stack[turn].sum + sum) and SUM_HASH_MASK
                continue
            }
            curr.sum = 0
            computeNextBoards(curr)
        }

        // All children board have been computed
        if (curr.childId < 0) {
            cachedSums[curr.key] = curr.sum
            turn--
            stack[turn].sum = (stack[turn].sum + curr.sum) and SUM_HASH_MASK
            continue
        }

        // There is more children board to compute
        turn++
        stack[turn].board = curr.childs[curr.childId]
        stack[turn].childId = -2
        curr.childId--
    }
    return stack[1].sum
}
