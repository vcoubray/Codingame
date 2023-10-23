package fr.vco.codingame.puzzles.the.fall

enum class Rotation { RIGHT, LEFT }

sealed interface Action {
    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Rotate(val x: Int, val y: Int, val rotation: Rotation) : Action {
        override fun toString() = "$x $y $rotation"
    }
}

class GamePlayer(private val board: Board) {

    private val pathFinder = PathFinder(board)

    fun play(indy: Position, rocks: List<Position>) {
        var action = findAction(indy, rocks)

        if (action is Action.Rotate && (action.x + action.y * board.width) in (rocks + indy).map { it.index }) {
            action = Action.Wait
        }

        println(action)
        board.update(action)
    }


    private fun findAction(indy: Position, rocks: List<Position>) : Action {
        val indyPaths = pathFinder.findValidGlobalPaths(indy)
        val rocksPaths = rocks.map(pathFinder::findAllGlobalPaths)

        for (indyPath in indyPaths) {
            // Filter Rock that have at least one crossing path with indy, and calculate their detailedPaths
            val rocksDetailedPaths = rocksPaths
                .filter{rockPaths -> rockPaths.any{it.isCrossing(indyPath)}}
                .map { rockPaths -> rockPaths.flatMap { pathFinder.toDetailedPaths(it) } }
            
            val indyDetailedPaths = pathFinder.toDetailedPaths(indyPath)

            for (indyDetailedPath in indyDetailedPaths) {
                val totalRockCombination = rocksDetailedPaths.getCombinationCount()
                var i = 0L

                while (!board.isValidDetailedPathCombination(indyDetailedPath, rocksDetailedPaths.getCombination(i))
                    && i < totalRockCombination
                ) {
                    i++
                }
                if (i < totalRockCombination) {
                    return board.getActions(indyDetailedPath, rocksDetailedPaths.getCombination(i)).firstOrNull()?: Action.Wait
                }
            }
        }
        System.err.println("Warning : No solution Found")
        return Action.Wait
    }

}