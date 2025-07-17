package fr.vco.codingame.puzzles.voxcodei.episode2


class GameResolver(val simulator: GameSimulator, val startTurn: Int) {

    val visited = mutableMapOf<Long, Int>()

    val actions = simulator.rounds.take(startTurn)
        .flatMapIndexed { turn, round ->
            round.actions.map { (pos, nodes) ->
                Action(turn + 3, pos, nodes)
            }
        }
        .filter { it.turn in 3..startTurn }
        .sortedWith(compareByDescending<Action> { it.nodesInRange.countOneBits() }.thenByDescending { it.turn })


    val ranges = actions.groupBy { it.nodesInRange }.keys.sortedByDescending{it.countOneBits()}


    var combinations = 0L
    fun resolve(): List<Int> {
        System.err.println("Total actions: ${actions.size}")
        System.err.println("Total ranges : ${ranges.size}")
        System.err.println("total valid Ranges : ${ranges}")
        System.err.println("Total Combinations: ${combinationCount(ranges.size.toLong(), simulator.bombs)}")
//        val actions = findValidActions(emptyList(), 0, simulator.bombs)
//            .map { actions[it] }
//            .sortedBy { it.turn }
//            .toMutableList()

        val validRanges = findRangeCombinations()
        System.err.println("total valid Ranges : ${validRanges.size}")
        validRanges.forEach{
            System.err.println(it)
        }

        val actions = findValidActions()
            .sortedBy { it.turn }
            .toMutableList()

        System.err.println(actions.joinToString())
        return (startTurn downTo 0).map {
            if (actions.lastOrNull()?.turn == it) actions.removeLast().pos
            else -1
        }

    }


    val rangeStack = IntArray(simulator.bombs)


    fun findRangeCombinations() : List<List<Long>>{
        val result = mutableListOf<List<Long>>()

        val currentRangeCombination = ArrayDeque<Long>()
        var workingTargetedNodes = 0L
        var stackId = 0
        var combinations = 0L
        rangeStack[stackId] = 0
        while (stackId >= 0) {
            val currId = rangeStack[stackId]

            combinations++
            if (combinations % 100000 == 0L) {
                System.err.println("combinations: $combinations, actions = ${currentRangeCombination.joinToString()}")
            }


            if(currId >= ranges.size) {
                currentRangeCombination.removeLast()
                workingTargetedNodes = currentRangeCombination.fold(0L) { acc, a -> acc or a }
                stackId--
                if(stackId >= 0) {
                    rangeStack[stackId]++
                }
                continue
            }

//            if(workingTargetedNodes == simulator.nodeMask) {
//                result.add(currentRangeCombination)
//                rangeStack[stackId]++
//                currentRangeCombination.removeLast()
//                workingTargetedNodes = currentRangeCombination.fold(0L) { acc, a -> acc or a }
//                break
//            }


            // All Node are targeted
            if( workingTargetedNodes or ranges[currId] == simulator.nodeMask) {
                result.add(currentRangeCombination + ranges[currId] )
                rangeStack[stackId]++
                break
            }

            // If add no new targeted node
            if (workingTargetedNodes and ranges[currId] != 0L) {
                rangeStack[stackId]++
                continue
            }

            // if no bombs remaining
            if(stackId >= simulator.bombs-1) {
                rangeStack[stackId]++
                continue
            }

            currentRangeCombination.addLast(ranges[currId])
            workingTargetedNodes = workingTargetedNodes or ranges[currId]
            stackId++
            rangeStack[stackId] = currId + 1

        }

        return result
    }


    val workingActions = ArrayDeque<Action>(simulator.bombs)
    val availableTurn = BooleanArray(simulator.turns) { true }
    var workingTargetedNodes = 0L
    val stack = IntArray(simulator.bombs)

    fun tryAddAction(action: Action): Int {
        if (workingActions.size == simulator.bombs) return IMPOSSIBLE
        if (!availableTurn[action.turn]) return IMPOSSIBLE
        if (workingTargetedNodes == workingTargetedNodes or action.nodesInRange) return IMPOSSIBLE

        if (simulator.rounds[action.turn].grid[action.pos] != -1) {
            return IMPOSSIBLE
        }

        simulator.ranges[action.pos].forEach { n ->
            if (workingActions.any { it.pos == n }) return IMPOSSIBLE
        }


        if (workingTargetedNodes or action.nodesInRange != simulator.nodeMask && workingActions.size == simulator.bombs - 1)
            return IMPOSSIBLE


        workingActions.add(action)
        workingTargetedNodes = workingTargetedNodes or action.nodesInRange
        if (workingTargetedNodes == simulator.nodeMask) return WIN
        availableTurn[action.turn] = false
        return IN_PROGRESS
    }

    fun removeLastAction() {
        val action = workingActions.removeLast()
        availableTurn[action.turn] = true
        workingTargetedNodes = workingActions.fold(0L) { acc, a -> acc or a.nodesInRange }
    }

    fun findValidActions(): List<Action> {

        var actionCount = 0
        stack[actionCount] = 0
        while (actionCount >= 0) {

            combinations++
            if (combinations % 100000 == 0L) {
                System.err.println("combinations: $combinations, actions = ${workingActions.joinToString()}")
            }

            val currActionId = stack[actionCount]

            if (currActionId >= actions.size) {
                removeLastAction()
                actionCount--
                if (actionCount >= 0) {
                    stack[actionCount]++
                }
                continue
            }

            val action = actions[currActionId]
            when (tryAddAction(action)) {
                WIN -> {
                    System.err.println("combinations: $combinations, actions = ${workingActions.joinToString()}")
                    break
                }

                IN_PROGRESS -> {
                    actionCount++
                    stack[actionCount] = currActionId + 1
                }

                IMPOSSIBLE -> {
                    stack[actionCount]++
                }
            }
        }
        return workingActions
    }


    fun findValidActions(actionIds: List<Int>, nextActionId: Int, bombs: Int): List<Int> {
        val isValid = isValidActions(actionIds)
        combinations++
        if (combinations % 1000 == 0L) {
            System.err.println("combinations: $combinations")
        }
        when {
            isValid == WIN -> return actionIds
            bombs == 0 -> return emptyList()
            isValid == IMPOSSIBLE -> return emptyList()
            else -> for (i in nextActionId until actions.size) {
                val actions = findValidActions(actionIds + i, i + 1, bombs - 1)
                if (actions.isNotEmpty()) return actions
            }
        }
        return emptyList()
    }

    val nodesMask = simulator.nodeMask

    val initialGrid = IntArray(simulator.width * simulator.height) { startTurn }
    var grid = IntArray(initialGrid.size)
    fun isValidActions(actionIds: List<Int>): Int {

        System.arraycopy(initialGrid, 0, grid, 0, initialGrid.size)
        var targetedNodes = 0L
        var lastTargetedNode = targetedNodes
        var lastTurn = -1
        actionIds.map { actions[it] }
            .sortedByDescending { (turn, _) -> turn }
            .forEach { action ->
                targetedNodes = targetedNodes or action.nodesInRange
                if (targetedNodes == nodesMask) return WIN
                if (targetedNodes == lastTargetedNode) return IMPOSSIBLE
                lastTargetedNode = targetedNodes

                if (simulator.rounds[action.turn].grid[action.pos] != -1)
                    return IMPOSSIBLE

                if (action.turn == lastTurn) return IMPOSSIBLE
                lastTurn = action.turn

                if (action.turn > grid[action.pos]) return IMPOSSIBLE
                simulator.ranges[action.pos].forEach {
                    grid[it] = action.turn - 3
                }
            }
        return IN_PROGRESS
    }
}

const val WIN = 1
const val IMPOSSIBLE = -1
const val IN_PROGRESS = 0


data class Action(val turn: Int, val pos: Int, val nodesInRange: Long)


fun combinationCount(number: Long, repetitions: Int): Long {
    return if (repetitions == 1) number
    else number * combinationCount(number - 1, repetitions - 1)
}