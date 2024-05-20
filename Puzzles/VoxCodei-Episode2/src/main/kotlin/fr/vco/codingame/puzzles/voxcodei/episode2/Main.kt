package fr.vco.codingame.puzzles.voxcodei.episode2

import kotlin.math.absoluteValue


data class Position(val x:Int, val y:Int) {
    operator fun plus(other: Position) = Position(x + other.x , y + other.y)
    operator fun minus(other: Position) = Position(x - other.x , y - other.y)
    fun distance(other: Position) = (x-other.x).absoluteValue + (y-other.y).absoluteValue
}

data class Node(val pos: Position, val direction: Position = Position(0,0))

class Nodes() {
    var nodes = emptyList<Position>()
    var directions = emptyList<Position>()

    fun updateNode(nodes : List<Position>) {
        if ( this.nodes.isEmpty()) {
            this.nodes = nodes
        } else if (directions.isEmpty()){
            this.directions = this.nodes.map{ previousNode ->
                val node = nodes.minBy { previousNode.distance(it)}
                node - previousNode
            }
        }
    }

    fun isReady() = nodes.isNotEmpty() && directions.isNotEmpty()
}

fun main() {
    val (width, height) = readln().split(" ").map{it.toInt()}

    var previousNodes = emptyList<Node>()
    var directions = emptyList<Node>()
    val nodes = Nodes()
    // game loop
    while (true) {
        val (rounds, bombs) = readln().split(" ").map{it.toInt()}
        val grid = List(height) { readln() }

        val currentNodes = buildList {
            repeat(height) { y ->
                repeat(width) { x ->
                    if(grid[y][x] == '@') {
                        add(Position(x,y))
                    }
                }
            }
        }
        nodes.updateNode(currentNodes)
        if(nodes.isReady()){
            for(i in nodes.nodes.indices) {
                System.err.println("${nodes.nodes[i]}, ${nodes.directions[i]}")
            }
        }


        println("WAIT")
    }
}