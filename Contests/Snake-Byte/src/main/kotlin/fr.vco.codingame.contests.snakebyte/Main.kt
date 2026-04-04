package fr.vco.codingame.contests.snakebyte


const val BOARD_OFFSET = 3

fun readInt() = readln().toInt()
fun debug(any: Any?) = System.err.println(any)


fun main() {

    val myId = readInt()
    val width = readInt()
    val height = readInt()
    val grid = List(height) { readln() }

    val board = Board(width, height, grid)
    val game = Game(board)


    debug(board.surfaceIndices)
    // game loop
    while (true) {
        game.updateGame()

        println(game.play())

    }
}

data class SnakeBot(val id: Int, val body: List<Int>) {
    val head = body.first()
    val size = body.size
}

class Board(width: Int, height: Int, grid: List<String>) {

    companion object {
        val WALL = '#'
    }

    val width = width + BOARD_OFFSET * 2
    val height = height + BOARD_OFFSET * 2

    val size = this.width * this.height

    val neighbours = List(size) {
        val pos = toPosition(it)
        DIRECTIONS.map { dir -> (pos + dir).takeIf { it in this }?.toIndex() ?: -1 }
    }

    val freeCells = List(size) {
        val p = toPosition(it)
        grid.getOrNull(p.y)?.getOrElse(p.x) { ' ' } != WALL
    }

    val distances = List(size) { computeDistances(it) }

    val surfaces = List(size) {
        val bottom = neighbours[it][DOWN]
        freeCells[it] && bottom != -1 && !freeCells[bottom]
    }

    val surfaceIndices = surfaces.mapIndexedNotNull { i, isSurface -> i.takeIf { isSurface } }

    fun computeDistances(start: Int): List<Int> {
        val toVisit = ArrayDeque<Int>().apply { addFirst(start) }
        val workingDistances = MutableList(size) { -1 }
        workingDistances[start] = 0

        while (toVisit.isNotEmpty()) {
            val curr = toVisit.removeFirst()

            neighbours[curr]
                .filter { it != -1 && freeCells[it] && workingDistances[it] == -1 }
                .forEach { n ->
                    workingDistances[n] = workingDistances[curr] + 1
                    toVisit.addLast(n)
                }

        }
        return workingDistances
    }


    fun getDistances(start: Int, end: Int): Int = distances[start][end]


    fun toPosition(id: Int) = Position((id % width) - BOARD_OFFSET, (id / width) - BOARD_OFFSET)

    fun Position.toIndex() = toIndex(x, y)
    fun toIndex(x: Int, y: Int) = (y + BOARD_OFFSET) * width + (x + BOARD_OFFSET)
    operator fun contains(pos: Position) =
        (pos.x + BOARD_OFFSET) in 0 until this.width && (pos.y + BOARD_OFFSET) in 0 until this.height
}


class Game(val board: Board) {

    val mySnakeIds: List<Int>
    val oppSnakeIds: List<Int>

    lateinit var freeCells: MutableList<Boolean>
    lateinit var powerSources: List<Int>
    lateinit var snakeBots: List<SnakeBot>

    init {
        val snakebotsPerPlayer = readInt()
        mySnakeIds = List(snakebotsPerPlayer) { readInt() }
        oppSnakeIds = List(snakebotsPerPlayer) { readInt() }
    }

    fun updateGame() {
        val powerSourceCount = readInt()
        powerSources = List(powerSourceCount) {
            val (x, y) = readln().split(" ").map { it.toInt() }
            board.toIndex(x, y)
        }

        val snakebotCount = readInt()
        snakeBots = List(snakebotCount) {
            val (id, body) = readln().split(" ")
            SnakeBot(
                id.toInt(),
                body.split(":").map { coord ->
                    coord.split(",").map { it.toInt() }.let { (x, y) -> board.toIndex(x, y) }
                }
            )
        }
        freeCells = board.freeCells.toMutableList()
        snakeBots.flatMap { bot -> bot.body.dropLast(1) }.forEach { freeCells[it] = false }

    }

    fun play(): String {

        val actions = mutableListOf<String>()
        snakeBots.filter { it.id in mySnakeIds }
            .forEach { bot ->
                val reachablePowerSources = powerSources.filter { isReachable(bot, it) }
                debug("${bot.id} -> $reachablePowerSources")
                val head = bot.body.first()
                val target = reachablePowerSources.minByOrNull { p -> board.getDistances(head, p) } ?: bot.body.last()
                val action = getNextDirection(head, target).takeIf { it != -1 }
                    ?: board.neighbours[head].indexOfFirst { n -> n != -1 && freeCells[n] }

                debug("${bot.id} -> ${bot.body.first()} $target -> ${board.getDistances(head, target)} - $action")
                actions.add("${bot.id} ${action.directionToAction()}")
            }

        return if (actions.isNotEmpty()) {
            actions.joinToString(";")
        } else {
            "WAIT"
        }
    }


    fun isReachable(snake: SnakeBot, target: Int): Boolean {
        val starts = snake.body.mapIndexedNotNull { i, it -> if (board.surfaces[it]) snake.size - i else null }

        val toVisit = ArrayDeque<Pair<Int, Int>>().apply { addAll(starts.map { snake.head to it }) }
        val visited = MutableList(board.size) { false }
        visited[snake.head] = true

        while (toVisit.isNotEmpty()) {
            val (head, dist) = toVisit.removeFirst()
            if (board.distances[head][target] in 0..dist + 1) {
                return true
            }
            val neighbours =
                board.surfaceIndices.filter { !visited[it] && board.distances[head][it] in 0..dist }

            neighbours.forEach { n ->
                toVisit.addLast(n to snake.size)
                visited[n] = true
            }
        }
        return false
    }


    fun getNextDirection(start: Int, end: Int): Int {
        val toVisit = ArrayDeque<Int>().apply { addFirst(start) }
        val visited = MutableList(board.size) { -1 }

        visited[start] = start

        while (toVisit.isNotEmpty()) {
            val curr = toVisit.removeFirst()
            if (curr == end) {

                var parent = curr
                var dir = -1
                while (parent != start) {
                    dir = board.neighbours[visited[parent]].indexOf(parent)
                    parent = visited[parent]
                }
                return dir
            }
            board.neighbours[curr]
                .filter { it != -1 && freeCells[it] && visited[it] == -1 }
                .forEach { n ->
                    visited[n] = curr
                    toVisit.addLast(n)
                }
        }
        return -1
    }

}

data class Position(var x: Int, var y: Int) {
    operator fun plus(other: Position) = Position(this.x + other.x, this.y + other.y)
}

const val UP = 0
const val RIGHT = 1
const val DOWN = 2
const val LEFT = 3

fun Int.directionToAction() = when (this) {
    UP -> "UP"
    RIGHT -> "RIGHT"
    DOWN -> "DOWN"
    LEFT -> "LEFT"
    else -> "UP"
}

val DIRECTIONS = listOf(
    Position(0, -1), // UP
    Position(1, 0), // RIGHT
    Position(0, 1), // DOWN
    Position(-1, 0), // LEFT
)


