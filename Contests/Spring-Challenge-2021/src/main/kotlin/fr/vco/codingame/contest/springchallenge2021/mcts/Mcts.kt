package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*
//import fr.vco.codingame.contest.springchallenge2021.PoolState
//import fr.vco.codingame.contest.springchallenge2021.mcts.PoolState
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class MctsNode(
    val parent: MctsNode?,
    val state: State,
    var action: Action? = null

) {

    var children: List<MctsNode> = emptyList()
    val actions by lazy { state.getAvailableActions() }
    var win: Int = 0
    var visit: Int = 0

    fun getRandomChild(): MctsNode {
        return children[Random.nextInt(children.size)]
    }
}

object Mcts {
    var createdNodes = 0
    var maxExecutionTime = 0L
    var totalSimulation = 0
    val simulationState = State()

    fun findNextMove(game: Game, timeout: Int = 90): Action {
//        PoolState.reset()
//
//        val rootState = PoolState.getNextState().initFromGame(game)
        val rootState = State().initFromGame(game)
        return findNextMove(rootState, timeout)
    }

    fun findNextMove(state: State, timeout: Int = 90): Action {
        val start = System.currentTimeMillis()
        val end = System.currentTimeMillis() + timeout


//        PoolState.reset()
//        val rootState = PoolState.getNextState().initFromGame(game)
        //val rootState = State().initFromGame(game)

//        log("Start MCTS : ${PoolState.index} ")
        //val rootNode = MctsNode(null, 0)
        val rootNode = MctsNode(null, state,WaitAction(ME))
        createdNodes = 1
        totalSimulation = 0
        while (System.currentTimeMillis() < end /*&& PoolState.index < PoolState.MAX_SIZE - 500*/) {

            val startLoop = System.nanoTime()
            val promisingNode = selectPromisingNode(rootNode)
            val endPromise = System.nanoTime()
            log("select in ${endPromise - startLoop}")
            //if (!NODES_POOL[promisingNode.stateIndex].isFinish()) {

            if (promisingNode.state.getStatus() == IN_PROGRESS) {
                expand(promisingNode)
            }
            val endExpand = System.nanoTime()
            log("expand in ${endExpand - endPromise}")

            val nodeToExplore =
                if (promisingNode.children.isNotEmpty()) promisingNode.getRandomChild() else promisingNode
            val winner = simulateRandomGame(nodeToExplore)
            val endExploration = System.nanoTime()
            log("Explore in ${endExploration - endExpand}")

            backPropagation(nodeToExplore, winner)
            val endBackPropa = System.nanoTime()
            log("backPropagation in ${endBackPropa - endExploration}")
            log("--------------------------")


        }

        if (timeout < 100) maxExecutionTime = max(maxExecutionTime, System.currentTimeMillis() - start)
        log("Execution Time : ${System.currentTimeMillis() - start}")
        log("Total simulation : ${rootNode.visit}")
        log("Total simulation 2  : ${totalSimulation}")
        log("Total Node created : $createdNodes")
//        log("Total State created : ${PoolState.index}")
        log("Total victory : ${rootNode.win}")


        var current: MctsNode? = rootNode
//        while (current?.state?.getStatus() == IN_PROGRESS) {
//            log("Day : ${current.state.day}, Player : ${current.action?.player},  ${current.action}")
//            current = current.children.maxByOrNull { it.win.toDouble() / it.visit }
//        }
        val bestNode = rootNode.children.maxByOrNull { it.visit }
        return bestNode?.action
         ?: WaitAction(ME)

    }


    fun selectPromisingNode(node: MctsNode): MctsNode {
        var bestNode = node
        while (bestNode.children.isNotEmpty()) {
            val parentVisit = ln(bestNode.visit.toDouble())
            bestNode = bestNode.children.maxByOrNull { uct(it, parentVisit) }!!
            //bestNode = bestNode.children.maxByOrNull { if (it.visit ==  0 ) Double.MAX_VALUE else it.win.toDouble() / bestNode.visit.toDouble() }!!
        }
        return bestNode
    }

    fun uct(node: MctsNode, totalVisit: Double): Double {
        if (node.visit == 0) return Double.MAX_VALUE
        return node.win.toDouble() / node.visit.toDouble() + sqrt(totalVisit / node.visit.toDouble())
    }


    fun expand(node: MctsNode) {
//        currentStateIndex = NODES_POOL[node.stateIndex].initChildren(currentStateIndex)
//        NODES_POOL[node.stateIndex].children.forEach { childStateIndex ->
//            val nodeChild = MctsNode(
//                parent = node,
//                stateIndex = childStateIndex
//            )
//            node.children.add(nodeChild)
//        }
        //log("expand : ${node.state.action}")
        node.children = node.actions.map {
            MctsNode(node, node.state.getNextState(it), it)
        }
        createdNodes += node.children.size

//        currentStateIndex = NODES_POOL[node.stateIndex].initChildren(currentStateIndex)
//        NODES_POOL[node.stateIndex].children.forEach { childStateIndex ->
//            val nodeChild = MctsNode(
//                parent = node,
//                stateIndex = childStateIndex
//            )
//            node.children.add(nodeChild)
//        }
    }

    fun backPropagation(node: MctsNode, winner: Int) {
        var current: MctsNode? = node
        while (current != null) {
            current.visit++
            //if (isMe == NODES_POOL[current.stateIndex].myTurn) {
            if (winner == current.action!!.player) {
                current.win++
            }
            current = current.parent
        }

    }


    fun simulateRandomGame(node: MctsNode): Int {
        simulationState.copyFromState(node.state)
        return simulationState.simulateRandomGame()

//        return node.state.child().simulateRandomGame()
    }


}