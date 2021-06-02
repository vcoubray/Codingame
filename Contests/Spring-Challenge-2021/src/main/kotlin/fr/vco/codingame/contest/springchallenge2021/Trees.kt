package fr.vco.codingame.contest.springchallenge2021

typealias Trees = Long

operator fun Trees.get(i: Int) = (this shr i) and 1
fun Trees.print() = this.toString(2)
fun Trees.size() = this.countOneBits()
fun Trees.addTree(i: Int) = this or (1L shl i)
fun Trees.removeTree(i: Int) = this and (1L shl i).inv()
fun Trees.getIndexes(): List<Int> = (0 until BOARD_SIZE).filter{this[it] == 1L}


fun main(){

    val trees : Trees = 500L

    println(trees.print())
    println(trees[0])

    println(trees.addTree(0))
    println(trees.removeTree(2).print())
    println(trees.print())


    val long = "10101010001000000000001000010100000".toLong(2)
    println(long.print())
    println(long.removeTree(32).print())



}