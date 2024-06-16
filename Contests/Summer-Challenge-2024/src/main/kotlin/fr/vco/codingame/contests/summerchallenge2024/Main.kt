package fr.vco.codingame.contests.summerchallenge2024


fun log(message: Any?) = System.err.println(message ?: "null")
val ACTIONS = listOf("LEFT", "RIGHT", "UP", "DOWN")
const val GAME_OVER = "GAME_OVER"


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
        games.forEach { it.update(readln()) }

        val actions = games.map { it.getActionsScore() }
        val scores = ACTIONS.map { action -> action to actions.sumOf { it[action]!! } }
        log(scores)
        val action = scores.maxBy { it.second }.first
        println(action)
    }
}


abstract class Game(val me: Int) {
    val opponents = (0..2).filterNot{it == me}

    lateinit var gpu: String
    val registry = MutableList(7) { 0 }

    abstract fun getActionsScore(): Map<String, Int>

    fun update(input: String) {
        val inputs = input.split(" ")
        gpu = inputs.first()
        inputs.drop(1).forEachIndexed { i, it -> registry[i] = it.toInt() }
    }
}


class ObstacleRun(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(): Map<String, Int> {
        val track = gpu
        val playerPos = registry.subList(0,3)
        val playerStuns = registry.subList(3, 6)

        val nextObstacle = track.drop(playerPos[me] + 1).indexOfFirst { it == '#' }

        if (playerStuns[me] > 0 || track == GAME_OVER) return ACTIONS.associateWith { 0 }

        return mapOf(
            "RIGHT" to if (nextObstacle >= 3 || nextObstacle == -1) 3 else 0,
            "UP" to when (nextObstacle) {
                0, 2 -> 3
                1 -> 0
                else -> 2
            },
            "LEFT" to when (nextObstacle) {
                1 -> 3
                2 -> 2
                0 -> 0
                else -> 1
            },
            "DOWN" to when (nextObstacle) {
                2 -> 3
                0, 1 -> 0
                else -> 2
            }
        )
//        return ACTIONS.associateWith { 0 }
    }
}


data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun times(scalar: Int) = Position(x * scalar, y * scalar)
    fun distanceToOrigin() = x * x + y * y
}

val DIRECTIONS = mapOf(
    "LEFT" to Position(-1, 0),
    "RIGHT" to Position(1, 0),
    "UP" to Position(0, -1),
    "DOWN" to Position(0, 1)
)

class Archery(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(): Map<String, Int> {
        val winds = gpu
        log("Winds : $winds")
        if (winds == GAME_OVER) return ACTIONS.associateWith { 0 }

        val currentWind = winds.first().digitToInt()

        val positions = registry.take(6)
            .chunked(2)
            .map { (x, y) -> Position(x, y) }

        return DIRECTIONS.map { (action, dir) -> action to dir * currentWind + positions[me] }
            .sortedByDescending { (_, pos) -> pos.distanceToOrigin() }
            .mapIndexed { i, (action) -> action to i }
            .toMap()
    }
}

class Roller(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(): Map<String, Int> {
        val riskOrder = gpu
        if (riskOrder == GAME_OVER) return ACTIONS.associateWith { 0 }

        val positions = registry.take(3)
        val risks = registry.subList(3, 6)
        val turn = registry.last().toInt()

//        return ACTIONS.associateWith{ action ->
//             if (riskOrder.indexOf(action.first()) == 1 ) 3 else 0
//        }
        return ACTIONS.associateWith { 0 }
    }
}

class ArtisticDiving(playerIndex: Int) : Game(playerIndex) {
    override fun getActionsScore(): Map<String, Int> {
        val objective = gpu
        
        val remainingTurn = objective.length
                
        val points = registry.take(3)
        val combos = registry.subList(3, 6)
        
        val maxScore = opponents.maxOf { maxPoints(points[it], combos[it], remainingTurn) }

        if (objective == GAME_OVER || maxScore < points[me]) {
            return ACTIONS.associateWith { 0 }
        }
        val actions = ACTIONS.associateWith { if (objective.first() == it.first()) 3 else 0 }
        return actions
//        return ACTIONS.associateWith { 0 }
    }
    
    fun maxPoints(points: Int, combos: Int, remainingTurn:Int) : Int {
        return points + (combos + combos + remainingTurn - 1) * remainingTurn / 2  
    }
    
}
