package fr.vco.codingame.puzzle.there.`is`.no.spoon.episode2

import java.util.*
import kotlin.math.max
import kotlin.math.min


fun main() {
    val input = Scanner(System.`in`)

    Board.init(input)
    val state = Board.initialState()
    state.addMandatoryLink()

    val finalState = findFinalState(state)
    if (finalState == null) System.err.println("No Solution found")
    finalState?.links?.forEach(::println)
}

fun findFinalState(state: State): State? {
    if (state.isFinish()) return state

    val links = state.getPossibleLinks()
    links.forEach { link ->
        val nextState = state.copy().apply { play(link) }
        if (nextState.isValid(link)) {
            val finalState = findFinalState(nextState)
            if (finalState != null) return finalState
        }
    }
    return null
}

data class Node(
    val id: Int,
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

    private fun isHorizontal() = Board[nodeId1].y == Board[nodeId2].y
    private fun isParallel(link: Link) = isHorizontal() == link.isHorizontal()

    fun isCrossing(link: Link): Boolean {
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
        val width = input.nextInt()
        val height = input.nextInt()
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
                            addNeighbors(it, neighbor)
                            break
                        }
                    }
                    for (i in y + 1 until height) {
                        val neighbor = board[i][x]
                        if (neighbor != null) {
                            addNeighbors(it, neighbor)
                            break
                        }
                    }
                }
            }
        }
    }

    fun addNeighbors(node1: Node, node2: Node) {
        node1.neighbours.add(node2.id)
        node2.neighbours.add(node1.id)
    }

    fun initialState() = State(
        nodes.map { StateNode(it.id, it.maxLinks, it.neighbours.associateWith { 0 }.toMutableMap()) },
        mutableListOf()
    )
}

class StateNode(
    val id: Int,
    var links: Int,
    val neighbours: MutableMap<Int, Int>
) {
    fun copy() = StateNode(id, links, neighbours.toMutableMap())
    fun isComplete() = links == 0

    fun removeCrossingWith(link: Link) {
        neighbours.keys
            .filter { link.isCrossing(Link(it, id)) }
            .forEach(neighbours::remove)
    }

    fun addLink(neighborId: Int, link: Int = 1) {
        links -= link
        neighbours[neighborId] = neighbours[neighborId]!! + link
    }
}

class State(
    val nodes: List<StateNode>,
    val links: MutableList<Link>
) {
    fun addMandatoryLink() {
        val mandatoryLink = mutableListOf<Link>()
        do {
            mandatoryLink.clear()
            nodes.forEach { node ->
                val n = node.links
                val validNeighbours = node.neighbours
                    .filter { (_, v) -> v < 2 }
                    .filterNot { (id, _) -> nodes[id].isComplete() }
                val s = validNeighbours.keys.sumOf { min(2, nodes[it].links) }
                validNeighbours.forEach { (neighbor, _) ->
                    val link = n - s + min(2, nodes[neighbor].links)
                    if (link > 0) {
                        node.addLink(neighbor, link)
                        nodes[neighbor].addLink(node.id, link)
                        mandatoryLink.add(Link(node.id, neighbor, link))
                    }
                }
            }
            links.addAll(mandatoryLink)
            mandatoryLink.forEach { link -> nodes.forEach { it.removeCrossingWith(link) } }
        } while (mandatoryLink.isNotEmpty())
    }

    fun getPossibleLinks(): List<Link> {
        return nodes.filterNot { it.isComplete() }
            .flatMap { stateNode ->
                stateNode.neighbours
                    .filter { (_, linkCount) -> linkCount < 2 }
                    .filter { (id, _) -> id > stateNode.id }
                    .filterNot { (id, _) -> nodes[id].isComplete() }
                    .map { (id, _) -> Link(stateNode.id, id) }
            }
    }

    fun copy() = State(
        nodes.map { it.copy() },
        MutableList(links.size) { links[it] },
    )

    fun play(link: Link) {
        nodes[link.nodeId1].addLink(link.nodeId2, link.count)
        nodes[link.nodeId2].addLink(link.nodeId1, link.count)
        links.add(link)
        nodes.forEach { it.removeCrossingWith(link) }
        addMandatoryLink()
    }

    fun isFinish() = nodes.all { it.isComplete() }
    fun isValid(link: Link) = !isSubOptimal() && !isBreakdown(link.nodeId1)
    private fun isIsolated(node: StateNode) = node.links > node.neighbours.keys.sumOf { id -> nodes[id].links }
    private fun isSubOptimal() = nodes.filterNot { it.isComplete() }.any(::isIsolated)

    private fun isBreakdown(nodeId: Int): Boolean {
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