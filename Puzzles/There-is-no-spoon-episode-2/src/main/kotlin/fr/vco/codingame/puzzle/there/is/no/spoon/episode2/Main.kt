package fr.vco.codingame.puzzle.there.`is`.no.spoon.episode2

import java.util.*
import kotlin.math.max
import kotlin.math.min


fun main() {
    val input = Scanner(System.`in`)

    Board.init(input)
    val state = Board.initialState()

    val start = System.currentTimeMillis()
    state.getMandatoryLink()

    val finalState = findFinalState(state)
    if (finalState == null) System.err.println("No Solution found")
    finalState?.links?.forEach(::println)


    System.err.println("terminated in ${System.currentTimeMillis() - start}ms")

}

fun findFinalState(state: State): State? {
    if (state.isFinish()) return state

    val actions = state.getActions()
    actions.forEach { action ->
        val nextState = state.copy().apply {
            play(action)
            getMandatoryLink()
        }
        if (nextState.isValid(action.nodeId1)) {
            val finalState = findFinalState(nextState)
            if (finalState != null) return finalState
        }
    }

    return null
}

data class Node(
    val index: Int,
    val x: Int,
    val y: Int,
    var maxLinks: Int,
    val neighbours: MutableList<Int> = mutableListOf(),
)

data class Link(
    val nodeId1: Int,
    val nodeId2: Int,
    val count: Int = 1
) {
    override fun toString() = "${Board[nodeId1].x} ${Board[nodeId1].y} ${Board[nodeId2].x} ${Board[nodeId2].y} $count"

    fun isHorizontal() = Board[nodeId1].y == Board[nodeId2].y
    fun isParallel(link: Link) = isHorizontal() == link.isHorizontal()

    fun isCrossing(link: Link): Boolean {

        if (isParallel(link)) return false

        return when {
            isParallel(link) -> false
            isHorizontal() -> inRange(link) { it.x } && link.inRange(this) { it.y }
            else -> inRange(link) { it.y } && link.inRange(this) { it.x }
        }
    }

    fun inRange(link: Link, coord: (Node) -> Int): Boolean {
        val range = (min(coord(Board[nodeId1]), coord(Board[nodeId2])) + 1 until max(
            coord(Board[nodeId1]),
            coord(Board[nodeId2])
        ))
        return coord(Board[link.nodeId1]) in range || coord(Board[link.nodeId2]) in range
    }

}

object Board {
    lateinit var nodes: List<Node>

    operator fun get(index: Int) = nodes[index]
    fun init(input: Scanner) {
        val width = input.nextInt() // the number of cells on the X axis
        val start = System.currentTimeMillis()
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
        System.err.println("initialized in ${System.currentTimeMillis() - start}ms")
    }

    fun initialState() = State(
        nodes.map { StateNode(it.index, it.maxLinks, it.neighbours.associateWith { 0 }.toMutableMap()) },
        mutableListOf()
    )

}

class StateNode(
    val index: Int,
    var power: Int,
    val neighbours: MutableMap<Int, Int>
) {
    fun copy() = StateNode(index, power, neighbours.toMutableMap())
    fun isComplete() = power == 0
    fun removeCrossingWith(link: Link) {
        neighbours.keys
            .filter { link.isCrossing(Link(it, index)) }
            .forEach(neighbours::remove)
    }

    fun addLink(neighborId: Int, link: Int = 1) {
        power -= link
        neighbours[neighborId] = neighbours[neighborId]!! + link
    }
}


class State(
    val nodes: List<StateNode>,
    val links: MutableList<Link>
) {

    fun getMandatoryLink(): List<Link> {
        val mandatoryLinks = mutableListOf<Link>()
        val currentLinks = mutableListOf<Link>()
        do {
            currentLinks.clear()
            nodes.forEach { stateNode ->
                val node = Board[stateNode.index]
                val n = stateNode.power
                val validNeighbours = stateNode.neighbours
                    .filter { (_, v) -> v < 2 }
                    .filterNot { (id, _) -> nodes[id].isComplete() }
                val s = validNeighbours.keys.sumOf { min(2, nodes[it].power) }
                validNeighbours.forEach { (neighbor, _) ->
                    val link = n - s + min(2, nodes[neighbor].power)
                    if (link > 0) {
                        stateNode.addLink(neighbor, link)
                        nodes[neighbor].addLink(stateNode.index, link)
                        val action = Link(node.index, neighbor, link)
                        currentLinks.add(action)
                    }
                }
            }
            mandatoryLinks.addAll(currentLinks)
            currentLinks.forEach { link -> nodes.forEach { it.removeCrossingWith(link) } }
        } while (currentLinks.isNotEmpty())
        links.addAll(mandatoryLinks)
        return mandatoryLinks
    }

    fun getActions(): List<Link> {
        return nodes.filter { it.power > 0 }.flatMap { stateNode ->
            stateNode.neighbours
                .filter { (_, linkCount) -> linkCount < 2 }
                .filter { (id, _) -> id > stateNode.index }
                .filterNot { (id, _) -> nodes[id].isComplete() }
                .map { (id, _) -> Link(stateNode.index, id) }
        }
    }

    fun copy(): State {
        return State(
            nodes.map { it.copy() },
            MutableList(links.size) { links[it] },
        )
    }

    fun play(link: Link) {
        nodes[link.nodeId1].addLink(link.nodeId2, link.count)
        nodes[link.nodeId2].addLink(link.nodeId1, link.count)
        links.add(link)
        nodes.forEach { it.removeCrossingWith(link) }
    }

    fun isFinish() = nodes.none { !it.isComplete() }

    fun isValid(nodeId: Int) = !isSubOptimal() && !isBreakdown(nodeId)

    private fun isSubOptimal() = nodes.filterNot { it.isComplete() }
        .any { it.power > it.neighbours.keys.sumOf { id -> nodes[id].power } }

    fun isBreakdown(nodeId: Int): Boolean {
        val toVisit = LinkedList<Int>()
        val visited = mutableSetOf<Int>()
        toVisit.add(nodeId)

        while (!toVisit.isEmpty()) {
            val currentId = toVisit.poll()
            visited.add(currentId)

            val current = nodes[currentId]
            if (!current.isComplete()) return false
            current.neighbours
                .filter { (_, links) -> links > 0 }
                .keys.filterNot(visited::contains)
                .forEach(toVisit::add)
        }

        return visited.size != nodes.size
    }
}