package fr.vco.codingame.contest.springchallenge2021

import fr.vco.codingame.contest.springchallenge2021.mcts.Game
import fr.vco.codingame.contest.springchallenge2021.mcts.State
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class MctsNode(
    val parent: MctsNode?,
    // val stateIndex: Int,
    val state: State
) {
    val children: MutableList<MctsNode> = mutableListOf()
    var win: Int = 0
    var visit: Int = 0

    fun getRandomChild(): MctsNode {
        return children[Random.nextInt(children.size)]
    }
}

object Mcts {

    var currentStateIndex = 0
    var maxExecutionTime = 0L
    fun findNextMove(game: Game, timeout: Int = 30): String {
        val start = System.currentTimeMillis()
        val end = System.currentTimeMillis() + timeout

//        currentStateIndex = 0
//        NODES_POOL[currentStateIndex].fromState(state)
//        currentStateIndex++
        val rootState = State().initFromGame(game)

        log("Start MCTS : $currentStateIndex ")

        //val rootNode = MctsNode(null, 0)
        val rootNode = MctsNode(null, rootState)
        while (System.currentTimeMillis() < end  /* && currentStateIndex < NODES_POOL_SIZE - 1_000*/) {

            val promisingNode = selectPromisingNode(rootNode)
            //if (!NODES_POOL[promisingNode.stateIndex].isFinish()) {
            if (promisingNode.state.getStatus() == IN_PROGRESS) {
                expand(promisingNode)
            }

            val nodeToExplore =
                if (promisingNode.children.isNotEmpty()) promisingNode.getRandomChild() else promisingNode
            val isVictoryMine = simulateRandomGame(nodeToExplore)

            backPropagation(nodeToExplore, isVictoryMine)

        }

        if (timeout < 100) maxExecutionTime = max(maxExecutionTime, System.currentTimeMillis() - start)
        log("Execution Time : ${System.currentTimeMillis() - start}")
        //log("Total Node created : $currentStateIndex")
        log("Total simulation : ${rootNode.visit}")
        log("Total victory : ${rootNode.win}")
        log("max execution Time : $maxExecutionTime")

        return rootNode.children.maxByOrNull { if (it.visit == 0) 0.0 else it.win.toDouble() / it.visit }?.let {
            //log("** action : ${it.state.action}, score : ${if (it.visit == 0) 0.0 else it.win.toDouble() / it.visit }")
            it.state.action?.toString()
        } ?: "WAIT"
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
        node.state.actions.forEach {
            //log("add $it")
            node.children.add(
                MctsNode(node, node.state.getNextState(it))
            )
        }
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
            if (winner == current.state.player) {
                current.win++
            }
            current = current.parent
        }

    }

    fun selectPromisingNode(node: MctsNode): MctsNode {
        var bestNode = node
        while (bestNode.children.isNotEmpty()) {
            val parentVisit = Math.log(bestNode.visit.toDouble())
            bestNode = bestNode.children.maxByOrNull { uct(it,parentVisit) }!!
        }
        return bestNode
    }

    fun uct(node: MctsNode, totalVisit : Double): Double{
        if (node.visit == 0) return 0.0
        return (node.win.toDouble()/node.visit + 1.41 * sqrt(totalVisit/node.visit))
        //return (node.win.toDouble()/node.visit )
    }


    fun simulateRandomGame(node: MctsNode): Int {
//        var currentIndex = node.stateIndex
//        while (!NODES_POOL[currentIndex].isFinish()) {
//            if (NODES_POOL[currentIndex].children.isEmpty()) {
//                currentStateIndex = NODES_POOL[currentIndex].initChildren(currentStateIndex)
//            }
//            val randomPlayIndex = Random.nextInt(NODES_POOL[currentIndex].children.size)
//            currentIndex = NODES_POOL[currentIndex].children[randomPlayIndex]
//        }
//
//        return NODES_POOL[currentIndex].isVictoryMine()
        var current = node.state
        while (current.getStatus() == IN_PROGRESS) {
//            log("***************")
//            log("Day : ${current.day}")
//            log("Player : ${current.player}, sun : ${current.players[current.player]!!.sun}, score: ${current.players[current.player]!!.score}")
//            current.trees.filter{it.owner == current.player}.forEach{log("** $it")}
//            current.actions.forEach{log("**** $it")}
            if (current.actions.isNotEmpty()) {

                val randomAction = current.actions[Random.nextInt(current.actions.size)]
//                log("Chosen Action : ${randomAction}")
                current = current.getNextState(randomAction)
            }

        }
        return current.getStatus()
    }


}