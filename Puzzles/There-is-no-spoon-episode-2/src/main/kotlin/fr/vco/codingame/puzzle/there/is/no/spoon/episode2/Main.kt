package fr.vco.codingame.puzzle.there.`is`.no.spoon.episode2

import java.util.*
import kotlin.math.min


fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt() // the number of cells on the X axis
    val height = input.nextInt() // the number of cells on the Y axis
    if (input.hasNextLine()) {
        input.nextLine()
    }

    val board = List(height) { y ->
        input.nextLine().mapIndexed { x, cell ->
            cell.takeIf { it != '.' }?.let { Node(x, y, cell.digitToInt()) }
        }
    }

    System.err.println(board.joinToString("\n") { it.joinToString("") { n -> n?.linkCount?.toString() ?: "." } })

    board.forEachIndexed { y, line ->
        line.forEachIndexed { x, node ->
            node?.let {
                for (i in x+1 until width) {
                    val neighbor = board[y][i]
                    if (neighbor != null) {
                        it.neighbours[neighbor] = 0
                        neighbor.neighbours[it] = 0
                        break
                    }
                }
                for (i in y+1 until height) {
                    val neighbor = board[i][x]
                    if (neighbor != null) {
                        it.neighbours[neighbor] = 0
                        neighbor.neighbours[it] = 0
                        break
                    }
                }
            }
        }
    }

    var currentBoard = board.flatten().filterNotNull()

    val totalActions = mutableListOf<Action>()
    val actions = mutableListOf<Action>()
    do  {
         actions.clear()
         currentBoard.forEach { node ->
             val n = node.linkCount
             val validNeighbours = node.neighbours.filter{(k,v) -> v <2}
             val s = validNeighbours.keys.sumOf{ min(2, it.linkCount) }
             validNeighbours.forEach{ (neigbhor, _) ->
                 val link = n - s + min(2, neigbhor.linkCount)
                 if (link > 0) {
                     node.linkCount -= link
                     neigbhor.linkCount -= link
                     node.neighbours[neigbhor] = node.neighbours[neigbhor]!! + link
                     neigbhor.neighbours[node] = neigbhor.neighbours[node]!! + link
                     actions.add(Action(node, neigbhor, link))

                 }
             }
         }
         totalActions.addAll(actions)
    }while(actions.isNotEmpty())


    totalActions.forEach(::println)
    println("No more obvious links")



    // Two coordinates and one integer: a node, one of its neighbors, the number of links connecting them.
//    println("0 0 2 0 1")
}

class Node(
    val x: Int,
    val y: Int,
    var linkCount: Int,
    val neighbours: MutableMap<Node,Int> = mutableMapOf()
) {
//    val links = neighbours.associateWith { 0 }.toMutableMap()
//    fun getAction() : Action? {
//         return neighbours.firstOrNull{it.linkCount > 0 }?.let{ neighbor->
//             val links = min(2,min(linkCount, neighbor.linkCount))
//             return Action(this, neighbor, links)
//        }
//    }

//    override fun equals(other: Any?) = other is Node && other.x == x && other.y == y
//
//    override fun hashCode() = x.hashCode() * 31 + y.hashCode()
}

class Action(
    val node : Node,
    val neighbor : Node,
    val links: Int
) {

    override fun toString() = "${node.x} ${node.y} ${neighbor.x} ${neighbor.y} $links"
}