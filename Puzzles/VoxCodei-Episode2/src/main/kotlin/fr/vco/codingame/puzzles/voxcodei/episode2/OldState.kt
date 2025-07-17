package fr.vco.codingame.puzzles.voxcodei.episode2


class StackState(
    var nodes: Long,
    var targetedNodes: Long,
    val freeCells: IntArray,
    var bombCount: Int = 0,
    var childId: Int = 0
)

class Resolver(val simulator: GameSimulator, val startTurn: Int) {

    val stack = Array(simulator.turns) {
        StackState(
            nodes = simulator.nodeMask,
            targetedNodes = 0L,
            freeCells = IntArray(simulator.width * simulator.height) { startTurn })
    }

    fun resolve(): List<Int> {

        val visited = HashMap<Long, Int>()

        var esquivedState = 0L
        var stateCount = 0

        val actions = ArrayDeque<Int>(startTurn)

        var turn = startTurn
        stack[turn].bombCount = simulator.bombs
        stack[turn].nodes = simulator.nodeMask
        stack[turn].targetedNodes = 0L

        while (turn <= startTurn) {
            val curr = stack[turn]

            // If curr not computed yet
            if (curr.childId == 0) {
                stateCount++

                if (curr.targetedNodes == simulator.nodeMask) {
                    System.err.println("Finish, states : $stateCount, esquived: $esquivedState")
                    return actions
                }

                if (curr.bombCount == 0 || turn <= 3) {
                    actions.removeLast()
                    turn++
                    continue
                }

                if (stateCount % 100 == 0) {
                    System.err.println("states : $stateCount, esquived: $esquivedState")
                }

                val bombs = actions.takeLast(3)
                val bomb1 = bombs.getOrNull(0)?:-1
                val bomb2 = bombs.getOrNull(1)?:-1
                val bomb3 = bombs.getOrNull(2)?:-1
                val key = (((curr.nodes * (curr.freeCells.size+1) + (bomb1+1)) * (curr.freeCells.size+1) + (bomb2+1)) * (curr.freeCells.size+1) + (bomb3+1)) * simulator.bombs + curr.bombCount
                if((visited[key]?:0) >= turn) {
                    esquivedState++
                    actions.removeLast()
                    turn++
                    continue
                }

            }
            val nextActions = simulator.rounds[turn - 3].actions
            if (curr.childId < nextActions.size) {
                val (nextAction, targetedNodes) = nextActions[curr.childId]
                curr.childId++

                stack[turn - 1].targetedNodes = curr.targetedNodes or targetedNodes
                if (curr.targetedNodes == stack[turn - 1].targetedNodes) {
                    continue
                }

                val targetValue = simulator.rounds[turn].grid[nextAction]
                if (targetValue != -1 && curr.nodes[targetValue]) {
                    continue
                }

                if (turn > curr.freeCells[nextAction]) {
                    continue
                }

                actions.addLast(nextAction)
                turn--
                System.arraycopy(curr.freeCells, 0, stack[turn].freeCells, 0, curr.freeCells.size)
                simulator.ranges.getOrNull(nextAction)?.forEach { stack[turn].freeCells[it] = turn - 2 }
                stack[turn].childId = 0
                stack[turn].bombCount = curr.bombCount - 1
                stack[turn - 2].nodes = stack[turn - 1].nodes - (stack[turn - 1].nodes and targetedNodes)
                continue
            } else if (curr.childId == nextActions.size) {
                actions.addLast(-1)
                System.arraycopy(curr.freeCells, 0, stack[turn - 1].freeCells, 0, curr.freeCells.size)
                stack[turn - 1].childId = 0
                stack[turn - 1].bombCount = curr.bombCount
                stack[turn - 1].targetedNodes = curr.targetedNodes
                stack[turn - 3].nodes = stack[turn - 2].nodes
                turn--
                curr.childId++
                continue
            }

            val bombs = actions.takeLast(3)
            val bomb1 = bombs.getOrNull(0)?:-1
            val bomb2 = bombs.getOrNull(1)?:-1
            val bomb3 = bombs.getOrNull(2)?:-1
            val key = (((curr.nodes * (curr.freeCells.size+1) + (bomb1+1)) * (curr.freeCells.size+1) + (bomb2+1)) * (curr.freeCells.size+1) + (bomb3+1)) * simulator.bombs + curr.bombCount
            visited[key] = turn

            actions.removeLast()
            turn++
        }
        return emptyList()
    }

}



