package fr.vco.codingame.contests.summerchallenge2024


fun log(message: Any?) = System.err.println(message ?: "null")
val ACTIONS = listOf("LEFT", "RIGHT", "UP", "DOWN")
fun main() {
    val playerIdx = readln().toInt()
    val nbGames = readln().toInt()

    val games = listOf(
        ObstacleRun(playerIdx),
        Archery(playerIdx),
        Roller(playerIdx),
        ArtisticDiving(playerIdx)
    )
    // game loop
    while (true) {
        for (i in 0 until 3) {
            val scoreInfo = readln()
        }
        val actions = games.map{it.getActionsScore(readln().split(" "))}

        val scores = ACTIONS.map{action -> action to actions.sumOf{it[action]!!}}
        log(scores)
        val action = scores.maxBy { it.second }.first
        println(action)
    }
}



abstract class Game(val playerIndex: Int) {
    abstract fun getActionsScore(registry: List<String>): Map<String, Int>
}


class ObstacleRun(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(registry: List<String>): Map<String, Int> {
        val trackfield = registry[0]
        val playerPos = registry.subList(1, 4).map { it.toInt() }
        val playerStuns = registry.subList(4, 7).map { it.toInt() }

        val nextObstacle = trackfield.drop(playerPos[playerIndex]+1).indexOfFirst { it == '#' }

        if (playerStuns[playerIndex] > 0) return ACTIONS.associateWith { 0 }

        return mapOf(
            "RIGHT" to if (nextObstacle >= 3 || nextObstacle == -1) 3 else 0,
            "UP" to when(nextObstacle) {
                3, -1 -> 2
                2, 1 -> 3
                else -> 2
            },
            "LEFT" to when(nextObstacle) {
                2 -> 3
                1 -> 0
                3 -> 2
                else -> 2
            },
            "DOWN" to when(nextObstacle) {
                3, -1 -> 2
                2,1 -> 0
                else -> 2
            }
        )
    }
}

class Archery(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(registry: List<String>): Map<String, Int> {
        return ACTIONS.associateWith { (0..3).random() }
    }
}

class Roller(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(registry: List<String>): Map<String, Int> {
        return ACTIONS.associateWith { (0..3).random() }
    }
}

class ArtisticDiving(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(registry: List<String>): Map<String, Int> {
        return ACTIONS.associateWith { (0..3).random() }
    }
}
