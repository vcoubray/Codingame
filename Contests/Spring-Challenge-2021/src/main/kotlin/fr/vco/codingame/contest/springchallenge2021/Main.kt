package fr.vco.codingame.contest.springchallenge2021

import java.util.*

fun log (message: Any) = System.err.println(message.toString())

data class Tree(
    val cellIndex: Int,
    val size: Int,
    val isMine: Boolean,
    val isDormant: Boolean
)


fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val numberOfCells = input.nextInt() // 37
    for (i in 0 until numberOfCells) {
        val index = input.nextInt() // 0 is the center cell, the next cells spiral outwards
        val richness = input.nextInt() // 0 if the cell is unusable, 1-3 for usable cells
        val neigh0 = input.nextInt() // the index of the neighbouring cell for each direction
        val neigh1 = input.nextInt()
        val neigh2 = input.nextInt()
        val neigh3 = input.nextInt()
        val neigh4 = input.nextInt()
        val neigh5 = input.nextInt()
    }

    // game loop
    while (true) {
        val day = input.nextInt() // the game lasts 24 days: 0-23
        val nutrients = input.nextInt() // the base score you gain from the next COMPLETE action
        val sun = input.nextInt() // your sun points
        val score = input.nextInt() // your current score
        val oppSun = input.nextInt() // opponent's sun points
        val oppScore = input.nextInt() // opponent's score
        val oppIsWaiting = input.nextInt() != 0 // whether your opponent is asleep until the next day
        val numberOfTrees = input.nextInt() // the current amount of trees
        val trees = List(numberOfTrees) {Tree(
            input.nextInt(),
            input.nextInt(),
            input.nextInt() !=0,
            input.nextInt() !=0
        )}

        val numberOfPossibleMoves = input.nextInt()
        if (input.hasNextLine()) {
            input.nextLine()
        }
        for (i in 0 until numberOfPossibleMoves) {
            val possibleMove = input.nextLine()
        }

        log(trees)
        // GROW cellIdx | SEED sourceIdx targetIdx | COMPLETE cellIdx | WAIT <message>
        println(trees.firstOrNull { it.isMine }?.cellIndex?.let{"COMPLETE $it"}?:"WAIT")
    }
}