package fr.vco.codingame.puzzles.voxcodei.episode1

import java.util.*

const val NODE = '@'
const val WALL = '#'
const val EMPTY = '.'
const val BOMB_RANGE = 3
const val BOMB_COUNTDOWN = 3

class Node(
    val x: Int,
    val y: Int,
    var isWall : Boolean = false,
    val rangedNodes: MutableList<Node> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if(this === other ) return true
        if(other is Node) {
            return this.x == other.x && this.y == other.y
        }
        return false
    }
}

class Bomb(
    val node: Node,
    var countDown : Int = BOMB_COUNTDOWN
)
class Board(
    val height: Int,
    val width: Int
) {

    val nodes: List<Node> = List(height * width) { i: Int ->
        Node(i%width,i/width )
    }

    val bombs : MutableList<Bomb> = mutableListOf()
    val targets : MutableList<Node> = mutableListOf()
    var notTargeted : List<Node> = listOf()

    fun init(input: Scanner) {
        repeat(height) { y ->
            input.nextLine().forEachIndexed { x, it ->
                when (it) {
                    WALL ->  getNode(x, y).isWall = true
                    NODE -> targets.add(getNode(x,y))
                }
            }
        }
        initChildren()
    }


    fun getFreePosition() = nodes
        .filterNot{it.isWall}
        .filterNot{targets.contains(it)}
        .filterNot{bombs.any{ b->b.node == it }}

    fun addBomb(node : Node){
        bombs.add(Bomb(node))
    }

    fun updateBoard(){
        bombs.forEach { it.countDown--}
        explodeChain()
        val targeted = bombs.map{it.node.rangedNodes}.flatten()
        notTargeted = targets.filterNot{targeted.contains(it)}
    }

    fun explodeChain(){
        while (bombs.filter{it.countDown == 0}.isNotEmpty()) {
            explodeBomb(bombs.first { it.countDown == 0 })
        }
    }

    fun explodeBomb(bomb : Bomb) {
        val explosion = bomb.node.rangedNodes
        bombs.remove(bomb)
        targets.removeAll(explosion)
        bombs.filter{explosion.contains(it.node)}.forEach{ it.countDown = 0}
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
        if (!isValid(parent) || parent.isWall)
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
        return if (node.isWall) emptyList()
        else getLine(node, vx, vy, range + 1) + node
    }

    fun boardToString(): String {
        val sb = StringBuffer()
        nodes.forEach{
            if(it.x == 0) sb.append("\n")
            sb.append("[${it.x} ${it.y} ${it.isWall}] ")
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

    while (true) {
        val rounds = input.nextInt() // number of rounds left before the end of the game
        val bombs = input.nextInt() // number of bombs left

        board.updateBoard()
        val bomb = board.getFreePosition()
            .maxBy{it.rangedNodes.count (board.notTargeted::contains) }
            ?.apply(board::addBomb)
            ?.let{"${it.x} ${it.y}"}
            ?:"WAIT"

        println(bomb)

    }
}