package fr.vco.codingame.puzzles.voxcodei.episode1

import java.util.*

const val NODE = '@'
const val WALL = '#'
const val BOMB_RANGE = 3
const val BOMB_COUNTDOWN = 3

data class Location(val x: Int, val y: Int) {
    operator fun plus(loc: Location): Location = Location(this.x + loc.x, this.y + loc.y)
    override fun toString() = "$x $y"
}

class Node(
    x: Int,
    y: Int,
    var isWall: Boolean = false,
    val rangedNodes: MutableList<Node> = mutableListOf()
) {
    val loc = Location(x, y)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Node) {
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

class Board(val height: Int,
            val width: Int
) {

    val nodes: List<Node> = List(height * width) { i: Int ->
        Node(i % width, i / width)
    }

    fun initChildren() {
        nodes.forEach {
            it.rangedNodes.addAll(getChildren(it))
        }
    }

    fun isValid(node: Node) = isValid(node.loc.x, node.loc.y)
    fun isValid(loc: Location) = isValid(loc.x, loc.y)
    fun isValid(x: Int, y: Int) = x in 0 until width && y in 0 until height
    fun getNode(x: Int, y: Int) = nodes[y * width + x]
    fun getNode(loc: Location) = getNode(loc.x, loc.y)

    private fun getChildren(parent: Node) =
        if (!isValid(parent) || parent.isWall)
            emptyList()
        else
            getLine(parent, Location(1, 0), 1) +
                getLine(parent, Location(-1, 0), 1) +
                getLine(parent, Location(0, 1), 1) +
                getLine(parent, Location(0, -1), 1)

    fun getLine(origin: Node, dir: Location, range: Int): List<Node> {

        val target = origin.loc + dir

        if (range > BOMB_RANGE || !isValid(target))
            return emptyList()

        val node = getNode(target)
        return if (node.isWall) emptyList()
        else getLine(node, dir, range + 1) + node
    }
}

class State(
    val board: Board,
    val targets: MutableList<Node>,
    val bombs: MutableList<Bomb>,
    val turn: Int,
    val bombsCount: Int,
    val parent: State?=null,
    val action: String?=null
) {

    val targetedCount = targets.intersect(bombs.map{board.getNode(it.loc).rangedNodes}.flatten()).size
    val explosions  = bombs.map{board.getNode(it.loc).rangedNodes}.flatten()
    val notTargeted = targets.filterNot(explosions::contains)
    init {
        explodeBombs()
    }

    fun isEnd() = turn <= 0 || bombsCount <= 0
    fun isWin(): Boolean {
        return notTargeted.isEmpty()
    }


    fun explodeBombs() {

        val explodingBombs = LinkedList<Bomb>(bombs.filter { it.countDown <= 0 })
        bombs.removeAll(explodingBombs)
        while (explodingBombs.isNotEmpty()) {
            val currentBomb = explodingBombs.pop()
            board.getNode(currentBomb.loc).rangedNodes.forEach {
                targets.remove(it)
                val chainedBombs = bombs.filter { bomb -> bomb.loc == it.loc }
                explodingBombs.addAll(chainedBombs)
                bombs.removeAll(chainedBombs)
            }
        }

    }

    fun getNextTurns(): List<State> {
        val explosions  = bombs.map{board.getNode(it.loc).rangedNodes}.flatten()
        val notTargeted = targets.filterNot(explosions::contains)
        return when {
            isEnd() -> emptyList()
            bombsCount <= 0 -> listOf(wait())
            else -> board.nodes
                .asSequence()
                .filterNot { it.isWall }
                .filterNot(targets::contains)
                .filterNot { bombs.any { b -> b.loc == it.loc } }
                .filter{ it.rangedNodes.intersect(notTargeted).isNotEmpty()}
                .map { dropBomb(it.loc) }
                .toList() + (if(bombs.none{it.countDown >0}) emptyList() else listOf(wait()))
        }
    }

    fun dropBomb(loc: Location): State {
        return State(
            board,
            targets.toMutableList(),
            bombs.map{it.copy(countDown = it.countDown-1)}.toMutableList().apply { add(Bomb(loc)) },
            turn - 1,
            bombsCount - 1,
            this,
            loc.toString()
        )
    }

    fun wait(): State = State(
        board,
        targets.toMutableList(),
        bombs.map{it.copy(countDown = it.countDown-1)}.toMutableList(),
        turn - 1,
        bombsCount,
        this,
        "WAIT"
    )

    fun actions() : List<String> {
        var current: State? = this
        val actions = LinkedList<String>()
        while (current?.action!=null){
            actions.addFirst(current.action)
            current = current.parent
            System.err.println(actions)
        }
        return actions + List(turn){"WAIT"}
    }

    fun identity() = StateIdentity(bombsCount,turn,notTargeted)

}

data class StateIdentity(
    val bombsCount: Int,
    val turn : Int,
    val targets: List<Node>
)

fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt() // width of the firewall grid
    val height = input.nextInt() // height of the firewall grid

    if (input.hasNextLine()) {
        input.nextLine()
    }

    val targets = mutableListOf<Node>()
    val board = Board(height, width)

    repeat(height) { y ->
        input.nextLine().forEachIndexed { x, it ->
            when (it) {
                WALL -> board.getNode(x, y).isWall = true
                NODE -> targets.add(board.getNode(x, y))
            }
        }
    }
    board.initChildren()

    val rounds = input.nextInt() // number of rounds left before the end of the game
    val bombs = input.nextInt() // number of bombs left

    val state = State(
        board = board,
        targets = targets,
        bombs = mutableListOf(),
        bombsCount = bombs,
        turn = rounds
    )

    val visited = mutableMapOf<Int,Boolean>()
    val toVisit = LinkedList<State>()
    toVisit.add(state)

    while (toVisit.isNotEmpty()) {
        val current = toVisit.removeLast()

        if(! visited.containsKey(current.identity().hashCode())) {
            toVisit.addAll(current.getNextTurns().sortedBy { it.targetedCount })
        }
        visited[current.identity().hashCode()] = true

        if(current.isWin()) {
            current.actions().forEach(::println)
            break
        }
    }
}