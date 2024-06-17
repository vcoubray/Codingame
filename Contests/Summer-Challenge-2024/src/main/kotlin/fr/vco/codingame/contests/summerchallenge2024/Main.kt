package fr.vco.codingame.contests.summerchallenge2024

import kotlin.math.ceil
import kotlin.math.roundToInt


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

        val playerScores = List(3) {
            readln().split(" ").drop(1).map { it.toInt() }.chunked(3)
        }
        log(playerScores)

        val scoreByGame = List(nbGames) { i ->
            playerScores.map {
                it[i].let { (gold, silver) -> gold * 3 + silver }
            }
        }

        games.forEachIndexed { i, it ->
            it.scores = scoreByGame[i]
            log(it.scores)
            it.update(readln())
        }
        val actions = games.map {
            it.getActionsScore().map { (a, score) -> a to if (it.scores[playerIdx] == 0) score * 10 else score }.toMap()
        }
            .onEach(::log)

        val scores = ACTIONS.map { action -> action to actions.sumOf { it[action]!! } }
        log(scores)
        val action = scores.maxBy { it.second }.first
        println(action)
    }
}

abstract class Game(val me: Int) {
    val opponents = (0..2).filterNot { it == me }
    lateinit var scores: List<Int>

    abstract fun getActionsScore(): Map<String, Int>
    abstract fun updateGame(gpu: String, registry: List<Int>)

    fun update(input: String) {
        val inputs = input.split(" ")
        updateGame(inputs.first(), inputs.drop(1).map { it.toInt() })
    }
}


class ObstacleRun(playerIndex: Int) : Game(playerIndex) {

    class Player(
        var pos: Int = 0,
        var stuns: Int = 0,
    )

    val players = List(3) { Player() }
    var track: String = ""


    override fun updateGame(gpu: String, registry: List<Int>) {
        track = gpu
        players.forEachIndexed { i, p ->
            p.pos = registry[i]
            p.stuns = registry[i + 3]
        }
    }

    override fun getActionsScore(): Map<String, Int> {

        if (players[me].stuns > 0 || track == GAME_OVER) return ACTIONS.associateWith { 0 }

        val nextObstacle = track.drop(players[me].pos + 1).indexOfFirst { it == '#' }


        val oppMinTurns = opponents.minOf { minTurn(players[it]) }
        val maxTurn = track.length - players[me].pos
        log("$maxTurn $oppMinTurns")
        
        if (maxTurn < oppMinTurns) return ACTIONS.associateWith { 0 }
        
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
    }

    private fun minTurn(player: Player): Int {
        val segments = track.drop(player.pos + 1).split("#.").map { it.length }
        val turns = segments.size - 1 + segments.sumOf { ceil((it) / 3.0).roundToInt() } + player.stuns
        return turns
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

    class Player(
        var pos: Position = Position(0, 0),
        var score: Int = 0,
    )

    var winds: List<Int> = emptyList()
    val players = List(3) { Player() }

    override fun updateGame(gpu: String, registry: List<Int>) {
        winds = if (gpu == GAME_OVER) emptyList() else gpu.map { it.digitToInt() }

        registry.take(6)
            .chunked(2)
            .map { (x, y) -> Position(x, y) }
            .forEachIndexed { i, pos ->
                players[i].pos = pos
            }
    }

    override fun getActionsScore(): Map<String, Int> {
        log("Winds : $winds")
        if (winds.isEmpty()) return ACTIONS.associateWith { 0 }

        val currentWind = winds.first()

        return DIRECTIONS.map { (action, dir) -> action to dir * currentWind + players[me].pos }
            .sortedByDescending { (_, pos) -> pos.distanceToOrigin() }
            .mapIndexed { i, (action) -> action to i }
            .toMap()
    }
}

class Roller(playerIndex: Int) : Game(playerIndex) {

    lateinit var riskOrder: String
    var turn: Int = 0
    var risks = listOf<Int>()
    var positions = listOf<Int>()

    override fun updateGame(gpu: String, registry: List<Int>) {
        riskOrder = gpu
        positions = registry.take(3)
        risks = registry.subList(3, 6)
        turn = registry.last().toInt()
    }

    override fun getActionsScore(): Map<String, Int> {

        if (riskOrder == GAME_OVER) return ACTIONS.associateWith { 0 }


//        return ACTIONS.associateWith{ action ->
//             if (riskOrder.indexOf(action.first()) == 1 ) 3 else 0
//        }
        return ACTIONS.associateWith { 0 }
    }
}

class ArtisticDiving(playerIndex: Int) : Game(playerIndex) {

    var objective = GAME_OVER
    var points = listOf<Int>()
    var combos = listOf<Int>()


    override fun updateGame(gpu: String, registry: List<Int>) {
        objective = gpu
        points = registry.take(3)
        combos = registry.subList(3, 6)
    }

    override fun getActionsScore(): Map<String, Int> {

        val remainingTurn = objective.length

        val maxScore = opponents.maxOf { maxPoints(points[it], combos[it], remainingTurn) }

        if (objective == GAME_OVER || maxScore < points[me]) {
            return ACTIONS.associateWith { 0 }
        }
        val actions = ACTIONS.associateWith { if (objective.first() == it.first()) 3 else 0 }
        return actions
//        return ACTIONS.associateWith { 0 }
    }

    private fun maxPoints(points: Int, combos: Int, remainingTurn: Int): Int {
        return points + (combos + combos + remainingTurn - 1) * remainingTurn / 2
    }

}
