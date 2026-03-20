package fr.vco.codingame.contests.snakebyte

fun readInt() = readln().toInt()
fun debug(any: Any?) = System.err.println(any)

fun main() {

    val myId = readInt()
    val width = readInt()
    val height = readInt()
    val grid = List(height) { readln() }

    val board = Board (width, height, grid)
    val game = Game(board)


    debug(board.distances[0])
    // game loop
    while (true) {
        game.updateGame()

        println(game.play())

    }
}

data class SnakeBot( val id: Int, var body: List<Int> )

class Board(val width: Int, val height: Int, grid:List<String>) {

    companion object {
        val WALL = '#'
    }
    val size = width * height

    val neighbours = List(size) {
        val pos = it.toPosition()
        DIRECTIONS.map{ dir -> (pos + dir).takeIf{it in this}?.toIndex() ?: -1 }
    }
    val freeCells = grid.flatMap { row -> row.map{ cell -> cell != WALL } }

    val distances = List(size){computeDistances(it)}

    fun computeDistances(start: Int) : List<Int> {
        val toVisit = ArrayDeque<Int>().apply { addFirst(start)}
        val workingDistances = MutableList(size){ -1 }
        workingDistances[start] = 0

        while (toVisit.isNotEmpty()) {
            val curr = toVisit.removeFirst()

            neighbours[curr]
                .filter{it != -1 && freeCells[it] && workingDistances[it] == -1}
                .forEach{ n ->
                    workingDistances[n] = workingDistances[curr] + 1
                    toVisit.addLast(n)
                }

        }
        return workingDistances
    }



    fun getDistances(start: Int, end: Int): Int  = distances[start][end]


    fun isFree(index: Int) = index > 0 && freeCells[index]
    fun Int.toPosition() = Position(this % width, this / width)
//    fun toIndex(position: Position) = position.toIndex()
    fun Position.toIndex() = y * width + x
    fun toIndex(x: Int, y: Int) = y * width + x
    operator fun contains(pos :Position) = pos.x in 0 until width && pos.y in 0 until height
}


class Game(val board: Board) {

    val mySnakeIds: List<Int>
    val oppSnakeIds: List<Int>

    lateinit var freeCells: MutableList<Boolean>
    lateinit var powerSources: List<Int>
    lateinit var snakeBots: List<SnakeBot>

    init{
        val snakebotsPerPlayer = readInt()
        mySnakeIds = List(snakebotsPerPlayer) { readInt() }
        oppSnakeIds = List(snakebotsPerPlayer) { readInt() }
    }

    fun updateGame() {
        val powerSourceCount = readInt()
        powerSources = List(powerSourceCount) {
            val (x, y) = readln().split(" ").map { it.toInt() }
            board.toIndex(x , y)
        }

        val snakebotCount = readInt()
        snakeBots =  List (snakebotCount) {
            val (id, body) = readln().split(" ")
            SnakeBot(
                id.toInt(),
                body.split(":").map{ coord ->
                    coord.split(",").map{it.toInt()}.let{(x,y)-> board.toIndex(x , y)}
                }
            )
        }
        freeCells = board.freeCells.toMutableList()
        snakeBots.flatMap{ bot -> bot.body }.forEach{ freeCells[it] = false}

    }

    fun play(): String {

        val actions = mutableListOf<String>()
        snakeBots.filter{it.id in mySnakeIds}
            .forEach{ bot ->
                val head = bot.body.first()
                val target = powerSources.minByOrNull{ p -> board.getDistances(head, p) } ?: bot.body.last()

                val action = getNextDirection(head, target)
                debug("${bot.id} -> ${bot.body.first()} $target -> ${board.getDistances(head, target)}")
                actions.add("${bot.id} ${action.directionToAction()}")
            }

        return if (actions.isNotEmpty()) {
            actions.joinToString(";")
        } else {
            "WAIT"
        }
    }

    fun getNextDirection(start: Int, end: Int) : Int {
        val toVisit = ArrayDeque<Int>().apply { addFirst(start)}
        val visited = MutableList(board.size){-1}

        visited[start] = start

        while(toVisit.isNotEmpty()) {
            val curr = toVisit.removeFirst()
            if( curr == end ) {

                var parent = curr
                var dir = -1
                while (parent != start) {
                    dir = board.neighbours[visited[parent]].indexOf(parent)
                    parent = visited[parent]
                }
                return dir
            }
            board.neighbours[curr]
                .filter{it != -1 && freeCells[it] && visited[it] == -1}
                .forEach{ n ->
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

fun Int.directionToAction() = when (this){
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


