package fr.vco.codingame.contest.springchallenge2021

typealias Trees = Long

operator fun Trees.get(i: Int) = (this shr i) and 1
fun Trees.print() = this.toString(2)
fun Trees.size() = this.countOneBits()
fun Trees.addTree(i: Int) = this or (1L shl i)
fun Trees.removeTree(i: Int) = this and (1L shl i).inv()
fun Trees.getIndexes(): List<Int> = (0 until BOARD_SIZE).filter{this[it] == 1L}

