package fr.vco.codingame.contests.summerchallenge2024

fun Int.pow(n: Int) : Int = Math.pow(this.toDouble(),n.toDouble()).toInt()
fun log(message: Any?) = System.err.println(message ?: "null")
val ACTIONS = listOf("LEFT", "RIGHT", "UP", "DOWN")
const val LEFT = 0
const val RIGHT = 1
const val UP = 2
const val DOWN = 3
const val GAME_OVER = "GAME_OVER"

const val SIMU_DEEP = 4
val COMBINATION_COUNT = 4.pow(SIMU_DEEP)


fun main() {
    val playerIdx = readln().toInt()
    val nbGames = readln().toInt()

    val games = listOf(
        Obstacle(playerIdx),
        Archery(playerIdx),
        Roller(playerIdx),
        Diving(playerIdx)
    )
    
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
//            it.scores = scoreByGame[i]
//            log(it.scores)
            it.update(readln())
            it.computeScore()
        }

        log(games[1].scores.max())

        var bestScore = 0.0
        var bestActions = 0

        for (i in 0 until COMBINATION_COUNT) {
            val score = games.sumOf { it.scores[i] }
            if (bestScore < score) {
                bestScore = score
                bestActions = i
            }
        }

        val actions = bestActions.toString(2).padStart(8, '0').chunked(2).map { ACTIONS[it.toInt(2)] }
        log("$actions -> $bestScore")

        println(actions.first())
    }
}


