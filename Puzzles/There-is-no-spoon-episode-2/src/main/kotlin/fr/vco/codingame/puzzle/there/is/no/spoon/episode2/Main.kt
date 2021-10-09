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
                        it.neighbours.add(neighbor)
                        neighbor.neighbours.add(it)
                        break
                    }
                }
                for (i in y+1 until height) {
                    val neighbor = board[i][x]
                    if (neighbor != null) {
                        it.neighbours.add(neighbor)
                        neighbor.neighbours.add(it)
                        break
                    }
                }
            }
        }
    }

    var currentBoard = board.flatten().filterNotNull()

    while (currentBoard.any { it.linkCount > 0 }) {
        currentBoard.maxByOrNull { it.linkCount/it.neighbours.size.toFloat() }?.getAction()?.let { action ->
            action.node.linkCount -= action.links
            action.neighbor.linkCount -= action.links
            action.node.neighbours.remove(action.neighbor)
            action.neighbor.neighbours.remove(action.node)
            currentBoard = currentBoard.filter { it.linkCount > 0 }
            println(action)
        }
    }





//    board.flatten().filterNotNull().forEach {
//        System.err.println("${it.x} ${it.y}")
//        it.neighbours.forEach { n ->
//            System.err.println("${n.x} ${n.y}")
//        }
//        System.err.println("----")
//    }

    // Two coordinates and one integer: a node, one of its neighbors, the number of links connecting them.
//    println("0 0 2 0 1")
}

class Node(
    val x: Int,
    val y: Int,
    var linkCount: Int,
    val neighbours: MutableList<Node> = mutableListOf()
) {

    fun getAction() : Action? {
         return neighbours.firstOrNull{it.linkCount > 0 }?.let{ neighbor->
             val links = min(2,min(linkCount, neighbor.linkCount))
             return Action(this, neighbor, links)
        }

    }

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