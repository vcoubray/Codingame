package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*
import java.util.*

class Game {
    var day: Int = 0
    var nutrients: Int = INITIAL_NUTRIENTS
    var sun: Int = 0
    var score: Int = 0
    var oppSun: Int = 0
    var oppScore: Int = 0
    var oppIsWaiting: Boolean = false
    var trees: List<Tree> = List(BOARD_SIZE) { Tree(it) }

    var turn = 0

    private var startTime = System.currentTimeMillis()


    fun readInput(input: Scanner) {
        day = input.nextInt() // the game lasts 24 days: 0-23
        startTime = System.currentTimeMillis()
        nutrients = input.nextInt() // the base score you gain from the next COMPLETE action
        sun = input.nextInt() // your sun points
        score = input.nextInt() // your current score
        oppSun = input.nextInt() // opponent's sun points
        oppScore = input.nextInt() // opponent's score
        oppIsWaiting = input.nextInt() != 0 // whether your opponent is asleep until the next day
        trees.forEach{it.size=NONE}
        List(input.nextInt()) {
            Tree(
                input.nextInt(),
                input.nextInt(),
                input.nextInt() != 0,
                input.nextInt() != 0
            )
        }.forEach {
            trees[it.cellIndex].size = it.size
            trees[it.cellIndex].isDormant = it.isDormant
            trees[it.cellIndex].isMine = it.isMine
        }

    }

    fun currentExecutionTime() = System.currentTimeMillis() - startTime

}


class Player{
    val score: Int = 0
    val sun: Int = 0
    val costs: MutableList<Int> = MutableList(5) { 0 }
}

class State {

    var player: Int = ME
    var day: Int = 0
    var nutrients = 0
    var score: Int = 0
    var sun: Int = 0
    var action: Action? = null
    val trees: List<Tree> = List(BOARD_SIZE) { Tree(it) }
    val costs: MutableList<Int> = MutableList(5) { 0 }
    val actions: List<Action> by lazy(::getAvailableActions)

    fun calcCosts(isMe: Boolean = true) {
        costs[SEED_ACTION] = trees.count { it.size == 0 && it.isMine == isMe } + SEED_COST
        costs[GROW_0_ACTION] = trees.count { it.size == 1 && it.isMine == isMe } + GROW_0_COST
        costs[GROW_1_ACTION] = trees.count { it.size == 2 && it.isMine == isMe } + GROW_1_COST
        costs[GROW_2_ACTION] = trees.count { it.size == 3 && it.isMine == isMe } + GROW_2_COST
        costs[COMPLETE_ACTION] = COMPLETE_COST
    }


    fun initFromGame(game: Game) = apply {
        day = game.day
        nutrients = game.nutrients
        score = game.score
        sun = game.sun
        game.trees.forEach { tree ->
            trees[tree.cellIndex].size = tree.size
            trees[tree.cellIndex].isDormant = tree.isDormant
            trees[tree.cellIndex].isMine = tree.isMine
        }
        calcCosts()
    }


    fun getAvailableActions(): List<Action> {
        val actions = mutableListOf<Action>()
        trees.forEach forEachTree@{ tree ->
            if (tree.size == NONE) return@forEachTree
            if (tree.isDormant) return@forEachTree
            if (!tree.isMine) return@forEachTree
            if (tree.size == GREAT && day >=12 && sun >= costs[COMPLETE_ACTION]) actions.add(CompleteAction(tree))
            if (tree.size < GREAT && sun >= costs[GROW_ACTION[tree.size]]) actions.add(GrowAction(tree))
            if (tree.size > LITTLE && costs[SEED_ACTION] == 0 && sun >= costs[SEED_ACTION]) Board[tree.cellIndex].neighByRange[tree.size].forEach {
                if(trees[it.index].size == NONE) {
                    actions.add(SeedAction(tree, it))
                }
            }
        }
        actions.add(WaitAction())
        return actions
    }

    fun copy() = State().also {
        it.player = player
        it.nutrients = nutrients
        it.day = day
        it.score = score
        it.sun = sun
        this.trees.forEachIndexed { i, tree ->
            it.trees[i].isDormant = tree.isDormant
            it.trees[i].isMine = tree.isMine
            it.trees[i].size = tree.size
        }
        this.costs.forEachIndexed{i, cost -> it.costs[i] = cost }
    }


    fun getNextState(action: Action): State {
        return copy().also {
            it.action = action
            when (action) {
                is CompleteAction -> {
                    it.sun -= costs[COMPLETE_ACTION]
                    it.costs[COMPLETE_ACTION]--
                    it.score += nutrients + BONUS_RICHNESS[Board[action.tree.cellIndex].richness]
                    nutrients--
                    it.trees[action.tree.cellIndex].size = NONE
                    it.trees[action.tree.cellIndex].isDormant = false

                }
                is GrowAction -> {
                    it.sun -= costs[GROW_ACTION[action.tree.size]]
                    it.costs[action.tree.size]--
                    it.costs[action.tree.size+1]++
                    it.trees[action.tree.cellIndex].size++
                    it.trees[action.tree.cellIndex].isDormant = true
                }
                is SeedAction -> {
                    it.sun -= costs[SEED_ACTION]
                    it.costs[SEED_ACTION]++
                    it.trees[action.target.index].size = SEED
                    it.trees[action.target.index].isDormant = true
                    it.trees[action.target.index].isMine = true
                    it.trees[action.source.cellIndex].isDormant = true
                }
                is WaitAction -> {
                    it.day++
                    if (day < MAX_DAY) {
                        it.sun += trees.filter { t -> t.isMine && !isShadowed(t,(day+3)%6) }.sumOf { t -> t.size }
                        it.trees.forEach { t -> t.isDormant = false }
                    }
                }
            }
            //it.calcCosts()
        }
    }

    fun isShadowed(tree: Tree, invertSunDir:Int) : Boolean{
        return Board[tree.cellIndex].neighByDirection[invertSunDir].any { trees[it.index].size >= tree.size }
    }

    fun getStatus(): Int {
        return when {
            day < MAX_DAY -> IN_PROGRESS
            score + sun/3 >= 90 -> ME
            else -> OPP
        }
    }




}