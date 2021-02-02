package fr.vco.codingame.puzzles.voxcodei.episode1

import java.lang.Exception
import java.util.*

const val NODE = '@'
const val WALL = '#'
const val BOMB_RANGE = 3
const val BOMB_COUNTDOWN = 3
const val WAIT = "WAIT"

data class Location(val x: Int, val y: Int) {
    operator fun plus(loc: Location): Location = Location(this.x + loc.x, this.y + loc.y)
    override fun toString() = "$x $y"
}

enum class TileType { NODE, WALL, EMPTY }

class Tile(
    x: Int,
    y: Int,
    val type: TileType,
    val range: MutableList<Tile> = mutableListOf()
) {
    val loc = Location(x, y)

    fun isWall() = type == TileType.WALL

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Tile) {
            return loc == other.loc
        }
        return false
    }

    override fun hashCode(): Int {
        return loc.hashCode()
    }
}

data class Bomb(
    val loc: Location,
    val countDown: Int = BOMB_COUNTDOWN
)

class Board(
    val height: Int,
    val width: Int,
    input: Scanner
) {

    val tiles = List(height) { y ->
        input.nextLine().mapIndexed { x, it ->
            val type = when (it) {
                WALL -> TileType.WALL
                NODE -> TileType.NODE
                else -> TileType.EMPTY
            }
            Tile(x, y, type)
        }
    }.flatten()

    init {
        initChildren()
    }

    private fun initChildren() {
        tiles.forEach {
            it.range.addAll(getChildren(it))
        }
    }

    fun isValid(tile: Tile) = isValid(tile.loc.x, tile.loc.y)
    fun isValid(loc: Location) = isValid(loc.x, loc.y)
    fun isValid(x: Int, y: Int) = x in 0 until width && y in 0 until height
    fun getTile(x: Int, y: Int) = tiles[y * width + x]
    fun getTile(loc: Location) = getTile(loc.x, loc.y)

    private fun getChildren(parent: Tile) =
        if (!isValid(parent) || parent.isWall())
            emptyList()
        else
            getLine(parent, Location(1, 0), 1) +
                getLine(parent, Location(-1, 0), 1) +
                getLine(parent, Location(0, 1), 1) +
                getLine(parent, Location(0, -1), 1)

    fun getLine(origin: Tile, dir: Location, range: Int): List<Tile> {
        val target = origin.loc + dir
        if (range > BOMB_RANGE || !isValid(target))
            return emptyList()

        val node = getTile(target)
        return if (node.isWall()) emptyList()
        else getLine(node, dir, range + 1) + node
    }
}

class State(
    val board: Board,
    val nodes: List<Tile>,
    val bombs: List<Bomb>,
    val turn: Int,
    val bombsCount: Int,
    val parent: State? = null,
    val action: String? = null
) {

    val bombCover = bombs.map { board.getTile(it.loc).range }.flatten()
    val targetedNodesCount = nodes.intersect(bombCover).size
    val notTargetedNodes = nodes.filterNot(bombCover::contains)

    fun isEnd() = turn <= 0 || bombsCount <= 0
    fun isWin() = notTargetedNodes.isEmpty()

    fun explodeBombs(): Pair<List<Bomb>, List<Tile>> {

        val explodingBombs = LinkedList<Bomb>(bombs.filter { it.countDown <= 0 })
        val newBombs = bombs.minus(explodingBombs).toMutableList()
        val newNodes = nodes.toMutableList()

        while (explodingBombs.isNotEmpty()) {
            val currentBomb = explodingBombs.pop()
            board.getTile(currentBomb.loc).range.forEach {
                newNodes.remove(it)
                val chainedBombs = newBombs.filter { bomb -> bomb.loc == it.loc }
                explodingBombs.addAll(chainedBombs)
                newBombs.removeAll(chainedBombs)
            }
        }
        return (newBombs.map { it.copy(countDown = it.countDown - 1) } to newNodes)
    }


    fun getNextTurns(): List<State> {
        val (newBombs, newNodes) = explodeBombs()
        return when {
            isEnd() -> emptyList()
            bombsCount <= 0 -> listOf(wait(newBombs, newNodes))
            else -> board.tiles
                .asSequence()
                .filterNot { it.isWall() }
                .filterNot(newNodes::contains)
                .filterNot { newBombs.any { b -> b.loc == it.loc } }
                .filter { it.range.intersect(notTargetedNodes).isNotEmpty() }
                .map { dropBomb(it.loc, newBombs, newNodes) }
                .toList() + (if (newBombs.none { it.countDown >= 0 }) emptyList() else listOf(wait(newBombs, newNodes)))
        }
    }

    private fun dropBomb(loc: Location, newBombs: List<Bomb>, newNodes: List<Tile>) = State(
        board,
        newNodes,
        newBombs + Bomb(loc),
        turn - 1,
        bombsCount - 1,
        this,
        loc.toString()
    )

    private fun wait(newBombs: List<Bomb>, newNodes: List<Tile>): State = State(
        board,
        newNodes,
        newBombs,
        turn - 1,
        bombsCount,
        this,
        WAIT
    )

    fun actions(): List<String> {
        var current: State? = this
        val actions = LinkedList<String>()
        while (current?.action != null) {
            actions.addFirst(current.action)
            current = current.parent
        }
        return actions + List(turn) { WAIT }
    }

    fun identity() = StateIdentity(bombsCount, turn, notTargetedNodes)

}

data class StateIdentity(
    val bombsCount: Int,
    val turn: Int,
    val targets: List<Tile>
)

fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt() // width of the firewall grid
    val height = input.nextInt() // height of the firewall grid

    if (input.hasNextLine()) {
        input.nextLine()
    }

    val board = Board(height, width, input)

    val rounds = input.nextInt() // number of rounds left before the end of the game
    val bombs = input.nextInt() // number of bombs left

    val startState = State(
        board = board,
        nodes = board.tiles.filter { it.type == TileType.NODE },
        bombs = emptyList(),
        bombsCount = bombs,
        turn = rounds
    )
    simulation(startState).actions().forEach(::println)

}

fun simulation(state: State) : State {
    val visited = mutableMapOf<Int, Boolean>()
    val toVisit = LinkedList<State>()
    toVisit.add(state)

    while (toVisit.isNotEmpty()) {
        val current = toVisit.removeLast()

        if (!visited.containsKey(current.identity().hashCode())) {
            toVisit.addAll(current.getNextTurns().sortedBy { it.targetedNodesCount })
        }
        visited[current.identity().hashCode()] = true

        if (current.isWin()) {
            return current
        }
    }
    System.err.println("No winner state found")
    throw Exception("No winner state found")
}