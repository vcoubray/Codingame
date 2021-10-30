package fr.vco.codingame.puzzle.there.`is`.no.spoon.episode2

import java.util.*
import kotlin.math.max
import kotlin.math.min


fun main() {
    val input = Scanner(System.`in`)

    Board.init(input)
    val state = Board.initialState()
    val links = state.getMandatoryLink()

    links.forEach(::println)
    println("No more obvious links")

}

data class Node(
    val index: Int,
    val x: Int,
    val y: Int,
    var maxLinks: Int,
    val neighbours: MutableList<Int> = mutableListOf(),
)

class Link(
    val node: Node,
    val neighbor: Node,
    val count: Int
) {
    override fun toString() = "${node.x} ${node.y} ${neighbor.x} ${neighbor.y} $count"

    fun isHorizontal() = node.y == neighbor.y
    fun isParallel(link: Link) = isHorizontal() == link.isHorizontal()

    fun isCrossing(link: Link): Boolean {

        if (isParallel(link)) return false

        return when {
            isParallel(link) -> false
            isHorizontal() ->  inRange(link){it.x} && link.inRange(this){it.y}
            else -> inRange(link){it.y} && link.inRange(this){it.x}
        }
    }

    fun inRange(link: Link, coord: ( Node )-> Int) : Boolean {
        val range = (min(coord(node),coord(neighbor)) +1 until max(coord(node),coord(neighbor)))
        return coord(link.node) in range || coord(link.neighbor) in range
    }

}

object Board {
    lateinit var nodes: List<Node>

    operator fun get(index: Int) = nodes[index]
    fun init(input: Scanner) {
        val width = input.nextInt() // the number of cells on the X axis
        val height = input.nextInt() // the number of cells on the Y axis
        if (input.hasNextLine()) {
            input.nextLine()
        }
        var nodeIndex = 0
        val board = List(height) { y ->
            input.nextLine().mapIndexed { x, cell ->
                cell.takeIf { it != '.' }?.let { Node(nodeIndex++, x, y, cell.digitToInt()) }
            }
        }
        nodes = board.flatten().filterNotNull()
        nodes.forEach(System.err::println)

        System.err.println(board.joinToString("\n") { it.joinToString("") { n -> n?.maxLinks?.toString() ?: "." } })

        board.forEachIndexed { y, line ->
            line.forEachIndexed { x, node ->
                node?.let {
                    for (i in x + 1 until width) {
                        val neighbor = board[y][i]
                        if (neighbor != null) {
                            it.neighbours.add(neighbor.index)
                            neighbor.neighbours.add(it.index)
                            break
                        }
                    }
                    for (i in y + 1 until height) {
                        val neighbor = board[i][x]
                        if (neighbor != null) {
                            it.neighbours.add(neighbor.index)
                            neighbor.neighbours.add(it.index)
                            break
                        }
                    }
                }
            }
        }

    }

    fun initialState(): State {
        return State(nodes.map { StateNode(it.index, it.neighbours.associateWith { 0 }.toMutableMap()) }, emptyList())
    }

}

data class StateNode(val index: Int, val neighbours: MutableMap<Int, Int>) {
    fun removeCrossingWith(link: Link) {
        neighbours.keys
            .filter { link.isCrossing(Link(Board[it], Board[index], 0)) }
            .forEach(neighbours::remove)

    }
}


data class State(
    val nodes: List<StateNode>,
    val links: List<Link>

) {

    fun getMandatoryLink(): List<Link> {
        val mandatoryLinks = mutableListOf<Link>()
        val actions = mutableListOf<Link>()
        do {
            actions.clear()
            nodes.forEach { stateNode ->
                val node = Board[stateNode.index]
                val n = node.maxLinks
                val validNeighbours = stateNode.neighbours.filter { (_, v) -> v < 2 }
                val s = validNeighbours.keys.sumOf { min(2, Board[it].maxLinks) }
                validNeighbours.forEach { (neighbor, _) ->
                    val link = n - s + min(2, Board[neighbor].maxLinks)
                    if (link > 0) {
                        node.maxLinks -= link
                        Board[neighbor].maxLinks -= link
                        stateNode.neighbours[neighbor] = stateNode.neighbours[neighbor]!! + link
                        nodes[neighbor].neighbours[stateNode.index] =
                            nodes[neighbor].neighbours[stateNode.index]!! + link
                        val action = Link(node, Board[neighbor], link)
                        actions.add(action)
                    }
                }
            }
            mandatoryLinks.addAll(actions)
            actions.forEach{action -> nodes.forEach{it.removeCrossingWith(action)}}
        } while (actions.isNotEmpty())
        return mandatoryLinks
    }

}