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
        trees.forEach { it.size = NONE }
        List(input.nextInt()) {
            Tree(
                cellIndex = input.nextInt(),
                size = input.nextInt(),
                owner = if (input.nextInt() == 1) ME else OPP,
                isDormant = input.nextInt() == 1
            )
        }.forEach {
            trees[it.cellIndex].size = it.size
            trees[it.cellIndex].isDormant = it.isDormant
            trees[it.cellIndex].owner = it.owner
        }

    }

    fun currentExecutionTime() = System.currentTimeMillis() - startTime

}


class Player {
    var score: Int = 0
    var sun: Int = 0
    var isWaiting: Boolean = false
    val costs: MutableList<Int> = MutableList(5) { 0 }


    fun copyFromPlayer(player: Player) {
        this.score = player.score
        this.sun = player.sun
        this.isWaiting = player.isWaiting
        player.costs.forEachIndexed { i, it -> this.costs[i] = it }
    }

    fun canPay(actionId: Int) = sun >= costs[actionId]

    fun calcScore() = score + sun / 3

}

class State {

    var player: Int = ME
    var day: Int = 0
    var nutrients = 0

    //    var score: Int = 0
//    var sun: Int = 0
    var action: Action? = null
    val players = listOf(
        Player(), // Not a real player (just for id 0)
        Player(), // ME
        Player() // OPP
    )
    val trees: List<Tree> = List(BOARD_SIZE) { Tree(it) }

    //    val costs: MutableList<Int> = MutableList(5) { 0 }
    val actions: List<Action> by lazy(::getAvailableActions)

    fun calcCosts(player: Int = ME) {
        players[player].costs[SEED_ACTION] = trees.count { it.size == 0 && it.owner == player } + SEED_COST
        players[player].costs[GROW_0_ACTION] = trees.count { it.size == 1 && it.owner == player } + GROW_0_COST
        players[player].costs[GROW_1_ACTION] = trees.count { it.size == 2 && it.owner == player } + GROW_1_COST
        players[player].costs[GROW_2_ACTION] = trees.count { it.size == 3 && it.owner == player } + GROW_2_COST
        players[player].costs[COMPLETE_ACTION] = COMPLETE_COST
    }


    fun initFromGame(game: Game) = apply {
        day = game.day
        nutrients = game.nutrients
//        score = game.score
//        sun = game.sun
        players[ME].sun = game.sun
        players[ME].score = game.score
        players[ME].isWaiting = false
        players[OPP].sun = game.oppSun
        players[OPP].score = game.oppScore
        players[OPP].isWaiting = game.oppIsWaiting

        game.trees.forEach { tree ->
            trees[tree.cellIndex].size = tree.size
            trees[tree.cellIndex].isDormant = tree.isDormant
            trees[tree.cellIndex].owner = tree.owner
        }
        calcCosts(ME)
        calcCosts(OPP)
        player = ME
    }


    fun getAvailableActions(): List<Action> {
        val actions = mutableListOf<Action>()
        if (!players[player].isWaiting) {
            trees.forEach forEachTree@{ tree ->
                if (tree.size == NONE) return@forEachTree
                if (tree.isDormant) return@forEachTree
                if (tree.owner != player) return@forEachTree
                if (tree.size == GREAT && day >= 12 && players[player].canPay(COMPLETE_ACTION)) {
                    actions.add(CompleteAction(tree))
                }
                if (tree.size < GREAT && players[player].canPay(GROW_ACTION[tree.size])) {
                    actions.add(GrowAction(tree))
                }
                if (tree.size > LITTLE &&
                    players[player].costs[SEED_ACTION] == 0 &&
                    players[player].canPay(SEED_ACTION)
                ) {
                    Board[tree.cellIndex].neighByRange[tree.size].forEach {
                        if (trees[it.index].size == NONE) {
                            actions.add(SeedAction(tree, it))
                        }
                    }
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

        it.players[ME].copyFromPlayer(this.players[ME])
        it.players[OPP].copyFromPlayer(this.players[OPP])

        this.trees.forEachIndexed { i, tree ->
            it.trees[i].isDormant = tree.isDormant
            it.trees[i].owner = tree.owner
            it.trees[i].size = tree.size
        }
    }


    fun getNextState(action: Action): State {
        return copy().also {
            it.action = action
            when (action) {
                is CompleteAction -> {
                    it.players[player].sun -= players[player].costs[COMPLETE_ACTION]
                    it.players[player].costs[COMPLETE_ACTION]--
                    it.players[player].score += nutrients + BONUS_RICHNESS[Board[action.tree.cellIndex].richness]
                    nutrients--
                    it.trees[action.tree.cellIndex].size = NONE
                    it.trees[action.tree.cellIndex].owner = -1
                    it.trees[action.tree.cellIndex].isDormant = false

                }
                is GrowAction -> {
                    it.players[player].sun -= it.players[player].costs[GROW_ACTION[action.tree.size]]
                    it.players[player].costs[action.tree.size]--
                    it.players[player].costs[action.tree.size + 1]++
                    it.trees[action.tree.cellIndex].size++
                    it.trees[action.tree.cellIndex].isDormant = true
                }
                is SeedAction -> {
                    it.players[player].sun -= it.players[player].costs[SEED_ACTION]
                    it.players[player].costs[SEED_ACTION]++
                    it.trees[action.target.index].size = SEED
                    it.trees[action.target.index].isDormant = true
                    it.trees[action.target.index].owner = player
                    it.trees[action.source.cellIndex].isDormant = true
                }
                is WaitAction -> {
                    it.players[player].isWaiting = true
                }
            }
            it.player = 3 - player
            if (it.players[ME].isWaiting && it.players[OPP].isWaiting) {
                it.newDay()
            }
        }
    }


    fun newDay() {
        day++
        if (day < MAX_DAY) {
            val invertSunDir = (day + 3) % 6
            trees.forEach{
                if(it.size > 0 && !isShadowed(it, invertSunDir))
                    players[it.owner].sun += it.size
            }

//            players[ME].sun += trees.filter { t -> t.owner == ME && !isShadowed(t, invertSunDir) }
//                .sumOf { t -> t.size }
//            players[OPP].sun += trees.filter { t -> t.owner == OPP && !isShadowed(t, invertSunDir) }
//                .sumOf { t -> t.size }
            players[ME].isWaiting = false
            players[OPP].isWaiting = false

            trees.forEach { t -> t.isDormant = false }
            player = ME
        }
    }


    fun isShadowed(tree: Tree, invertSunDir: Int): Boolean {
        return Board[tree.cellIndex].neighByDirection[invertSunDir].any { trees[it.index].size >= tree.size }
    }

    fun getStatus(): Int {
        return when {
            day < MAX_DAY -> IN_PROGRESS
           // players[ME].calcScore() > 80 -> ME
            players[ME].calcScore() > players[OPP].calcScore() -> ME
            else -> OPP
        }
    }


}