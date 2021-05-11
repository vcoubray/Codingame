package fr.vco.codingame.contest.springchallenge2021

import java.util.*
import kotlin.math.max

const val SEED_ACTION = 0
const val GROW_0_ACTION = 1
const val GROW_1_ACTION = 2
const val GROW_2_ACTION = 3
const val COMPLETE_ACTION = 4

const val SEED = 0
const val LITTLE = 1
const val MEDIUM = 2
const val GREAT = 3

fun log(message: Any?) = System.err.println(message.toString())

data class Tree(
    val cellIndex: Int,
    val size: Int,
    val isMine: Boolean,
    val isDormant: Boolean
)

interface Action

class SeedAction(val source: Tree, val target: Cell, private val message: String = "") : Action {
    override fun toString() = "SEED ${source.cellIndex} ${target.index} $message"
}

class GrowAction(val tree: Tree, private val message: String = "") : Action {
    override fun toString() = "GROW ${tree.cellIndex} $message"
}

class CompleteAction(val tree: Tree, private val message: String = "") : Action {
    override fun toString() = "COMPLETE ${tree.cellIndex} $message"
}

class Player(
    val score: Int,
    val sun: Int,
    val trees: List<Tree>
) {
    val costs = listOf(
        trees.count { it.size == 0 } + 0,
        trees.count { it.size == 1 } + 1,
        trees.count { it.size == 2 } + 3,
        trees.count { it.size == 3 } + 7,
        trees.count { it.size == 3 } + 4
    )
}


class State(
    val day: Int,
    val nutrients: Int,
    val sun: Int,
    val score: Int,
    val oppSun: Int,
    val oppScore: Int,
    val oppIsWaiting: Boolean,
    val trees: List<Tree>
) {
    constructor(input: Scanner) : this(
        day = input.nextInt(), // the game lasts 24 days: 0-23
        nutrients = input.nextInt(), // the base score you gain from the next COMPLETE action
        sun = input.nextInt(), // your sun points
        score = input.nextInt(), // your current score
        oppSun = input.nextInt(), // opponent's sun points
        oppScore = input.nextInt(), // opponent's score
        oppIsWaiting = input.nextInt() != 0, // whether your opponent is asleep until the next day
        trees = List(input.nextInt()) {
            Tree(
                input.nextInt(),
                input.nextInt(),
                input.nextInt() != 0,
                input.nextInt() != 0
            )
        }
    )

    val sunDir = day % 6
    val treesIndexes = trees.map { it.cellIndex }
    val me = Player(score, sun, trees.filter { it.isMine })
    val opp = Player(oppScore, oppSun, trees.filterNot { it.isMine })
    val myShadow = Board.calcPotentialShadowCount(me.trees)
    val globalShadow = Board.calcPotentialShadowCount(trees)

    fun bestAction(player: Player = me): String {

        val greatTrees = player.trees.filter{it.size == GREAT}
        val mediumTrees = player.trees.filter{it.size == MEDIUM}
        val availableMediumTrees = mediumTrees.filterNot{it.isDormant}
        val availableGreatTrees = greatTrees.filterNot { it.isDormant }

        val shouldComplete = when {
            availableGreatTrees.isEmpty() -> false
            player.costs[COMPLETE_ACTION] > player.sun -> false
            day == 23 -> true
            greatTrees.size > 4 -> true
            greatTrees.size >= 4 && availableMediumTrees.isNotEmpty() &&
                player.costs[COMPLETE_ACTION] + player.costs[GROW_2_ACTION] <= sun-> true
            else ->false
        }

        if( shouldComplete) {
            val target = availableGreatTrees.maxBy { Board[it.cellIndex].richness }!!
            val cost = player.costs[COMPLETE_ACTION]
            val gain = nutrients + Board[target.cellIndex].richness
            if ( gain + (sun-cost)/3 > sun/3)
                return CompleteAction(target).toString()
        }


        if(availableMediumTrees.isNotEmpty() && player.costs[GROW_2_ACTION] <= player.sun && day < 23 )
            return GrowAction(availableMediumTrees.firstOrNull()!!).toString()

        val littleTrees = player.trees.filter { it.size == LITTLE }.filterNot{it.isDormant}
        if(littleTrees.isNotEmpty() && player.costs[GROW_1_ACTION] <= player.sun && day < 22 )
            return GrowAction(littleTrees.firstOrNull()!!).toString()

        val seeds = player.trees.filter { it.size == SEED }.filterNot{it.isDormant}
        if(seeds.isNotEmpty() && player.costs[GROW_0_ACTION] <= player.sun && day < 21 )
            return GrowAction(seeds.firstOrNull()!!).toString()

        val seedTrees = player.trees.filterNot { it.isDormant || it.size <= LITTLE}
        val shouldSeed = when {
            day >= 20 -> false
            seedTrees.isEmpty() -> false
            player.costs[SEED_ACTION] > player.sun -> false
            player.trees.count () < 7 && player.trees.count { it.size == SEED } < 1-> true
            else -> false
        }
        if(shouldSeed ) {

            val seedActions = seedTrees.map { t ->
                val targets = Board[t.cellIndex].neighByRange[t.size]
                targets.forEach{log("${it.index} : ${myShadow[it.index]}")}
                targets
                    .asSequence()
                    .filter { it.richness > 0 }
                    .filterNot { treesIndexes.contains(it.index) }
                    .map { SeedAction(t, it) }
                    .toList()
            }.flatten()

            return seedActions.maxWith(compareBy({-myShadow[it.target.index]},{it.target.richness},{-globalShadow[it.target.index]}))?.toString()?:"WAIT"
        }
        return "WAIT"
    }

}

fun possibleMoves(input: Scanner): List<String> {
    val numberOfPossibleMoves = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    return List(numberOfPossibleMoves) { input.nextLine() }
}

fun main(args: Array<String>) {
    val input = Scanner(System.`in`)

    val startInit = System.currentTimeMillis()
    Board.init(input)
    log("init board in ${System.currentTimeMillis() - startInit}ms")

    var maxTime = 0L
    // game loop
    while (true) {
        val stateInit = System.currentTimeMillis()
        val state = State(input)
        possibleMoves(input)
        log("Read state in ${System.currentTimeMillis() - stateInit}ms")
        val start = System.currentTimeMillis()
        println(state.bestAction())
        val executionTime = System.currentTimeMillis() - start
        maxTime = max(executionTime, maxTime)
        log("End turn in ${executionTime}ms ")

        log("Max Execution in ${maxTime}ms ")
    }
}