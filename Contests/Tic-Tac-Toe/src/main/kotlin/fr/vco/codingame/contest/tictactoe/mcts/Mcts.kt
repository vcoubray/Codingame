package fr.vco.codingame.contest.tictactoe.mcts


import fr.vco.codingame.contest.tictactoe.DRAW
import fr.vco.codingame.contest.tictactoe.IN_PROGRESS
import fr.vco.codingame.contest.tictactoe.game.Action
import fr.vco.codingame.contest.tictactoe.game.Board
import fr.vco.codingame.contest.tictactoe.game.Grid
import fr.vco.codingame.contest.tictactoe.log
import kotlin.math.ln
import kotlin.math.sqrt

object Mcts {

    val LOG_NEP = MutableList(10000) { ln(it.toFloat()) }

    var createdNodes = 0
    var executionTime = 0L
    var totalSimulation = 0

    private val simulationState = Board()
    private val rootState = Board()
    lateinit var rootNode: Node
    lateinit var bestNode: Node

    fun findNextMove(board: Board, timeout: Int = 90): Action? {
        val start = System.currentTimeMillis()
        val end = System.currentTimeMillis() + timeout
        rootState.load(board)

        rootNode = Node(null, null)
        createdNodes = 1
        totalSimulation = 0
        while (System.currentTimeMillis() < end) {

            simulationState.load(rootState)

            // 1 - Selection
            val promisingNode = selectPromisingNode(rootNode)

            // 2 - Expansion
            if (simulationState.status == IN_PROGRESS) {
                expand(promisingNode)
            }

            val nodeToExplore = if (promisingNode.children.isNotEmpty()) {
                val child = promisingNode.getRandomChild()
                simulationState.play(child.action!!)
                child
            } else promisingNode

            // 3 - Simulation
            val winner = simulateRandomGame()

            // 4 - BackPropagation
            backPropagation(nodeToExplore, winner)
        }


        executionTime = System.currentTimeMillis() - start
        log("MCTS Execution Time : ${executionTime}")
        log("Total simulation : ${rootNode.visit}")
        log("Total Node created : $createdNodes")
        log("Total victory : ${rootNode.win}")

        bestNode = rootNode.children.maxByOrNull { it.visit } ?: rootNode
        return bestNode.action
    }

    fun summary() = "${bestNode.win / bestNode.visit} ${rootNode.visit} ${executionTime}ms"

    private fun selectPromisingNode(node: Node): Node {
        var bestNode = node
        while (bestNode.children.isNotEmpty()) {
            val parentVisit = LOG_NEP.getOrElse(bestNode.visit) { ln(it.toFloat()) }
            bestNode = bestNode.children.maxByOrNull { uct(it, parentVisit) }!!
            //bestNode = bestNode.children.maxByOrNull { if (it.visit ==  0 ) Double.MAX_VALUE else it.win.toDouble() / bestNode.visit.toDouble() }!!
            simulationState.play(bestNode.action!!)
        }
        return bestNode
    }

    private fun uct(node: Node, totalVisit: Float): Float {
        if (node.visit == 0) return Float.MAX_VALUE
        return node.win / node.visit + sqrt(totalVisit / node.visit)
    }

    private fun expand(node: Node) {
        node.children = simulationState.getActions().map { Node(node, it) }
        createdNodes += node.children.size
    }

    private fun backPropagation(node: Node, winner: Int) {
        var current: Node? = node

        while (current != null) {
            current.visit++
            current.win += when (winner) {
                current.action?.player -> 1f
                DRAW -> 0.25f
                else -> 0f
            }
            current = current.parent
        }

    }

    fun simulateRandomGame(): Int {
        return simulationState.simulateRandomGame()
    }

}