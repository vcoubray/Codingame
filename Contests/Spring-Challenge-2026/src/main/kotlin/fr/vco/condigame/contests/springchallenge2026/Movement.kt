package fr.vco.condigame.contests.springchallenge2026

data class MovementResult(val actions: List<String>, val stepTiles: Map<Troll, Int>)

object Movement {
    fun resolve(plan: List<Pair<Troll, Intent>>, board: Board): MovementResult {
        val actions = mutableListOf<String>()
        val stepTiles = mutableMapOf<Troll, Int>()
        val blockedTiles = mutableSetOf<Int>()

        for ((troll, intent) in plan) {
            if (intent is Intent.Act) {
                stepTiles[troll] = troll.tileId
                blockedTiles += troll.tileId
                actions += intent.command
            }
        }

        for ((troll, intent) in plan) {
            if (intent !is Intent.Move) continue
            val shortest = intent.targetTileIds
                .map { board.findPath(troll.tileId, it, blockedTiles) }
                .filter { it.isNotEmpty() }
                .minByOrNull { it.size }
            val step = if (shortest == null) {
                troll.tileId
            } else {
                shortest[minOf(troll.movementSpeed, shortest.size - 1)]
            }
            blockedTiles += step
            stepTiles[troll] = step
            if (step != troll.tileId) {
                val (x, y) = board.getCoord(step)
                actions += "MOVE ${troll.id} $x $y"
            }
        }

        return MovementResult(actions, stepTiles)
    }
}
