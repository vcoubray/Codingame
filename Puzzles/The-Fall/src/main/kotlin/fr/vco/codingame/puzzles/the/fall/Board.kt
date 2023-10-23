package fr.vco.codingame.puzzles.the.fall

enum class Direction(val x: Int, val y: Int) {
    TOP(0, 1),
    RIGHT(-1, 0),
    LEFT(1, 0),
    NONE(0, 0),
    CRASH(0, 0)
}

data class Position(val index: Int, val dir: Direction)

class Board(
    val width: Int,
    val exit: Int,
    val cellTypes: MutableList<Int>,
    val fixedMap: List<Boolean>,
    val neighbors: Map<Direction, List<List<Pair<Direction, Int>>>>,
) {

    fun update(action: Action) {
        if (action is Action.Rotate) {
            val type = cellTypes[action.y * width + action.x]
            cellTypes[action.y * width + action.x] = CELL_TYPES[type].rotate[action.rotation]!!
        }
    }

    fun isValidDetailedPathCombination(indyPath: DetailedPath, rockPaths: List<DetailedPath>): Boolean {

        val workingCellTypes = this.cellTypes.toMutableList()
        var rockCount = rockPaths.size
        val rocks = rockPaths.toTypedArray()

        var actionCount = 0
        for (i in 0 until indyPath.size) {
            val cellIndex = indyPath.path[i]
            val originType = workingCellTypes[cellIndex]
            val targetType = indyPath.cellTypes[i]

            if (targetType != originType) {
                actionCount += getRotations(originType, targetType).size
                workingCellTypes[cellIndex] = targetType
            }

            for (j in 0 until rockCount) {
                val rockCellIndex = rocks[j].path[i]

                val originRockType = workingCellTypes[rockCellIndex]
                val targetRockType = rocks[j].cellTypes[i]

                if (cellIndex == rockCellIndex && targetType != targetRockType) return false
                if (cellIndex == rockCellIndex && rocks[j].directions[i + 1] != Direction.NONE) return false
                if (i > 0) {
                    val cellIndexPrev = indyPath.path[i - 1]
                    val rockCellIndexPrev = rocks[j].path[i - 1]
                    if (cellIndex == rockCellIndexPrev && cellIndexPrev == rockCellIndex) return false
                }
                if (originRockType != targetRockType) {
                    actionCount += getRotations(originRockType, targetRockType).size
                    workingCellTypes[rockCellIndex] = targetRockType
                }
            }

            // Remove rocks that crash into a wall
            for (j in rockCount - 1 downTo 0) {
                if (rocks[j].size <= i + 1) {
                    rocks[j] = rocks[rockCount - 1]
                    rockCount--
                }
            }

            // remove rocks that crash into another one
            for (j in rockCount - 1 downTo 0) {
                if (j >= 1 && rocks[j].path[i] != -1 && rockCount >= 2) {
                    for (k in j - 1 downTo 0)
                        if (rocks[j].path[i] == rocks[k].path[i]) {

                            rocks[j] = rocks[rockCount - 1]
                            rocks[k] = rocks[rockCount - 2]
                            rockCount -= 2
                            break
                        }
                }
            }
            if (actionCount > i + 1) return false
        }
        return true
    }

    fun getActions(indyPath: DetailedPath, rockPaths: List<DetailedPath>): List<Action> {
        var rockCount = rockPaths.size
        val rocks = rockPaths.toTypedArray()

        val actions = mutableListOf<Action>()

        for (i in 0 until indyPath.size) {
            val cellIndex = indyPath.path[i]
            val targetType = indyPath.cellTypes[i]
            val originType = cellTypes[cellIndex]

            if (targetType != originType) {
                getRotations(originType, targetType).forEach {
                    actions.add(
                        Action.Rotate(
                            cellIndex % width,
                            cellIndex / width,
                            it
                        )
                    )
                }
            }

            repeat(rockCount) { ir ->
                val rockCellIndex = rocks[ir].path[i]
                if (rockCellIndex >= 0) {
                    val rocKTargetType = rocks[ir].cellTypes[i]
                    val rockOriginType = cellTypes[rockCellIndex]

                    if (rocKTargetType != rockOriginType) {
                        getRotations(rockOriginType, rocKTargetType).forEach {
                            actions.add(
                                Action.Rotate(
                                    rockCellIndex % width,
                                    rockCellIndex / width,
                                    it
                                )
                            )
                        }
                    }
                }

                if (rocks[ir].size <= i + 1) {
                    rocks[ir] = rocks[rockCount - 1]
                    rockCount--
                }
            }
        }
        return actions
    }
}
