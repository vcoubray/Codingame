package fr.vco.codingame.puzzles.voxcodei.episode1

import java.util.*

const val NODE = 0
const val WALL = 1
const val EMPTY = 2
const val BOMB = 2
const val BOMB_RANGE = 3

interface Action
class Wait() : Action {
    override fun toString() = "WAIT"
}

class PutBomb(val x: Int, val y: Int) : Action {
    override fun toString() = "$x $y"
}


class Node(
    val x: Int,
    val y: Int,
    var type: Int = EMPTY,
    val rangedNodes: MutableList<Node> = mutableListOf()
)


class Board(
    val height: Int,
    val width: Int
) {

    val nodes: List<Node> = List(height * width) { i: Int ->
        Node(i%width,i/width )
    }

    fun init(input: Scanner) {
        repeat(height) { y ->
            input.nextLine().mapIndexed { x, it ->
                when (it) {
                    '#' -> WALL
                    '@' -> NODE
                    else -> EMPTY
                }.run { getNode(x, y).type = this }
            }
        }

        initChildren()
    }

    fun initChildren() {
        nodes.forEach {
            it.rangedNodes.addAll(getChildren(it))
        }
    }

    fun isValid(node: Node) = isValid(node.x,node.y)
    fun isValid(x: Int, y: Int) = x in 0 until width && y in 0 until height
    fun getNode(x: Int, y: Int) = nodes[y * width + x]

    fun getChildren(parent: Node) =
        if (!isValid(parent) || parent.type == WALL)
            emptyList()
        else
            getLine(parent, 1, 0, 1) +
                    getLine(parent, -1, 0, 1) +
                    getLine(parent, 0, 1, 1) +
                    getLine(parent, 0, -1, 1)

    fun getLine(origin:Node, vx: Int, vy: Int, range: Int): List<Node> {

        if (range > BOMB_RANGE || !isValid(origin) || !isValid(origin.x + vx, origin.y + vy))
            return emptyList()

        val node = getNode(origin.x + vx, origin.y + vy)
        return if (node.type == WALL) emptyList()
        else getLine(node, vx, vy, range + 1) + node
    }

    fun boardToString(): String {
        val sb = StringBuffer()
        nodes.forEach{
            if(it.x == 0) sb.append("\n")
            sb.append("[${it.x} ${it.y} ${it.type}] ")
        }
        return sb.toString()
    }
}


fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt() // width of the firewall grid
    val height = input.nextInt() // height of the firewall grid

    if (input.hasNextLine()) {
        input.nextLine()
    }

    val board = Board(height, width)
    board.init(input)
    System.err.println(board.boardToString())

    // game loop
    while (true) {
        val rounds = input.nextInt() // number of rounds left before the end of the game
        val bombs = input.nextInt() // number of bombs left

        val action = board.nodes
            .filter{it.type == EMPTY}
            .maxBy{it.rangedNodes.count { c -> c.type == NODE }}
            ?.let{"${it.x} ${it.y}"}
            ?:"WAIT"

        println(action)
    }
}