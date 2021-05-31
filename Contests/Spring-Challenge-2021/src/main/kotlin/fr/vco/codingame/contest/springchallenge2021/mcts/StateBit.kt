package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*
import kotlin.math.max


data class PlayerBit(
    var score: Int = 0,
    var sun: Int = 0,
    var isWaiting: Boolean = false,
    val treesSize: List<Long> = List(4) { 0 },
    val isDormant: Trees = 0

) {

    val costs = listOf(
        treesSize[0].size() + SEED_COST,
        treesSize[1].size() + GROW_0_COST,
        treesSize[2].size() + GROW_1_COST,
        treesSize[3].size() + GROW_2_COST,
        COMPLETE_COST
    )


    constructor(score: Int, sun: Int, isWaiting: Boolean, trees: List<Tree>) : this(
        score = score,
        sun = sun,
        isWaiting = isWaiting,
        listOf(
            trees.filter { it.size == 0 }.getBits(),
            trees.filter { it.size == 1 }.getBits(),
            trees.filter { it.size == 2 }.getBits(),
            trees.filter { it.size == 3 }.getBits()
        ),
        isDormant = trees.filter { it.isDormant }.getBits()
    )

    val trees by lazy { treesSize.reduce { acc, l -> acc or l } }
    val treesIndexes by lazy { trees.getIndexes() }
    val sizeMap by lazy { treesIndexes.map { it to getTreeSize(it) }.toMap() }


    fun canPay(actionId: Int) = sun >= costs[actionId]

    fun getTreeSize(idTree: Int) = treesSize.indexOfFirst { it[idTree] == 1L }

    fun calcScore() = score + sun / 3

    override fun toString(): String {
        return "sun : ${sun}, score: $score, isWaiting: $isWaiting, trees : ${trees.print()}"
    }

}


data class StateBits(
    var player: Int = ME,
    var day: Int = 0,
    var nutrients: Int = 0,
    //val trees: List<Tree> = List(BOARD_SIZE) { Tree(it) },
    val players: List<PlayerBit> = listOf(
        PlayerBit(), // ME
        PlayerBit() // OPP
    )
) {

    constructor(game: Game) : this(
        player = ME,
        day = game.day,
        nutrients = game.nutrients,
        players = listOf(
            PlayerBit(game.score, game.sun, false, game.realTrees.filter { it.owner == ME }),
            PlayerBit(game.oppScore, game.oppSun, game.oppIsWaiting, game.realTrees.filter { it.owner == OPP })
        )
    )

    val opp by lazy { if (player == ME) OPP else ME }


    override fun toString(): String {
        return """
            *********************
            Day : $day, Nutrients : $nutrients
            Player : $player 
            ME : ${players[ME]}
            OPP: ${players[OPP]}
        """.trimIndent()
    }


//    fun initFromGame(game: Game) = apply {
//        day = game.day
//        nutrients = game.nutrients
//
//        player[ME] = PlayerBit(game.sun, game.score, false, game.realTrees.filter{it.owner == ME})
//
//        players[ME].sun = game.sun
//        players[ME].score = game.score
//        players[ME].isWaiting = false
//        players[OPP].sun = game.oppSun
//        players[OPP].score = game.oppScore
//        players[OPP].isWaiting = game.oppIsWaiting

    //        game.getTreesBit { it.size == 0 && it.owner == ME }
//        game.trees.forEach { tree ->
//            trees[tree.cellIndex].size = tree.size
//            trees[tree.cellIndex].isDormant = tree.isDormant
//            trees[tree.cellIndex].owner = tree.owner
//        }
//        calcCosts(ME)
//        calcCosts(OPP)
//        player = ME
    //actions.clear()
//    }
//
//    fun copyFromState(state : State) = apply {
//        day = state.day
//        player = state.player
//        nutrients = state.nutrients
//        players.forEachIndexed{i, it -> it.copyFromPlayer(state.players[i]) }
//        trees.forEach { tree ->
//            tree.size = state.trees[tree.cellIndex].size
//            tree.owner = state.trees[tree.cellIndex].owner
//            tree.isDormant = state.trees[tree.cellIndex].isDormant
//        }
//
//    }
//
    fun getAvailableActions(): List<Action> {
        val actions = mutableListOf<Action>(WaitAction(player))

        val nonSeedableCell =
            if (players[player].trees[0].size() == 0) players[player].treesIndexes.flatMap { Board[it].neighIndex }
            else emptyList()
        //val activeTrees = players[player].trees and players[player].isDormant.inv()

        val totalTrees = players[ME].trees or players[OPP].trees
        if (!players[player].isWaiting) {
            players[player].treesIndexes.forEach forEachLoop@{
                when {
                    players[player].isDormant[it] == 1L -> return@forEachLoop
                    players[player].treesSize[GREAT][it] == 1L -> {
                        if (day >= 12 && players[player].canPay(COMPLETE_ACTION))
                            actions.add(CompleteAction(player, it))
                        if (players[player].canPay(SEED_ACTION))
                            Board[it].neighByRange[GREAT].forEach { target ->
                                if (totalTrees[target.index] == 0L && nonSeedableCell.none { c -> target.index == c }) {
                                    actions.add(SeedAction(player, it, target.index))
                                }
                            }
                    }
                    players[player].treesSize[MEDIUM][it] == 1L -> {
                        if (players[player].canPay(GROW_2_ACTION))
                            actions.add(GrowAction(player, it, MEDIUM))
                        if (players[player].canPay(SEED_ACTION))
                            Board[it].neighByRange[MEDIUM].forEach { target ->
                                if (totalTrees[target.index] == 0L && nonSeedableCell.none { c -> target.index == c }) {
                                    actions.add(SeedAction(player, it, target.index))
                                }
                            }
                    }
                    players[player].treesSize[LITTLE][it] == 1L -> {
                        if (players[player].canPay(GROW_1_ACTION))
                            actions.add(GrowAction(player, it, LITTLE))
                    }
                    players[player].treesSize[SEED][it] == 1L -> {
                        if (players[player].canPay(GROW_0_ACTION))
                            actions.add(GrowAction(player, it, SEED))
                    }
                }

            }
//            players[player].trees3.getIndexes().forEach{actions.add(CompleteAction(player, it))}
//
//            players[player].trees.getIndexes().forEach forEachTree@{ tree ->
//                if (players[player].isDormant[tree]) return@forEachTree
//                if (players[player].trees3[tree] == 1L &&
//                    day >= 12 &&
//                    players[player].canPay(COMPLETE_ACTION)
////                    (players[player].costs[3] >= 4 || day > 20)
//                ) {
//                    actions.add(CompleteAction(player, tree))
//                }
//                if (tree.size < GREAT && players[player].canPay(GROW_ACTION[tree.size])) {
//                    actions.add(GrowAction(player, tree))
//                }
//                if (tree.size > LITTLE &&
//                    //players[player].firstAction &&
//                    players[player].costs[SEED_ACTION] == 0 &&
//                    players[player].canPay(SEED_ACTION)
//                ) {
//                    Board[tree.cellIndex].neighByRange[tree.size].forEach { target ->
//                        if (trees[target.index].size == NONE && nonSeedableCell.none{target.index == it}) {
//                            actions.add(SeedAction(player, tree, target))
//                        }
//                    }
//                }
//            }
        }
        return actions
    }

    //
//    fun child() = this.copy(
//        trees = trees.map{it.copy()},
//        players = players.map{it.copy(
//            costs = it.costs.toMutableList()
//        )}
//    )
//
    fun getNextState(action: Action): StateBits {
        return when (action) {
            is CompleteAction -> complete(action)
            is GrowAction -> grow(action)
            is SeedAction -> seed(action)
            is WaitAction -> wait(action)
        }
    }



fun completePlayer(current: Int, action: CompleteAction): PlayerBit {
    return if (current == action.player) {
        players[current].copy(
            sun = players[current].sun - players[current].costs[COMPLETE_ACTION],
            score = players[current].score + nutrients + BONUS_RICHNESS[Board[action.treeId].richness],
            treesSize = listOf(
                players[current].treesSize[SEED],
                players[current].treesSize[LITTLE],
                players[current].treesSize[MEDIUM],
                players[current].treesSize[GREAT].removeTree(action.treeId)
            )

        )
    } else players[current].copy(treesSize = players[current].treesSize.toMutableList())
}

fun complete(action: CompleteAction): StateBits {
    return this.copy(
        player = opp,
        nutrients = nutrients - 1,
        players = listOf(
            completePlayer(ME, action),
            completePlayer(OPP, action)
        )
    )
}

fun growPlayer(current: Int, action: GrowAction): PlayerBit {

    return if (current == action.player) {
        val newTrees = players[current].treesSize.toMutableList()
        newTrees[action.size] = newTrees[action.size].removeTree(action.treeId)
        newTrees[action.size+1] = newTrees[action.size + 1].addTree(action.treeId)

        players[current].copy(
            sun = players[current].sun - players[current].costs[GROW_ACTION[action.size]],
            isDormant = players[current].isDormant.addTree(action.treeId),
            treesSize = newTrees
        )
    } else players[current].copy(treesSize = players[current].treesSize.toMutableList())
}

fun grow(action: GrowAction): StateBits {
    return this.copy(
        player = opp,
        players = listOf(
            growPlayer(ME, action),
            growPlayer(OPP, action)
        )
    )

}

fun playerSeed(current: Int, action: SeedAction): PlayerBit {
    return if (current == action.player) {
        val newDormant = players[current].isDormant
            .addTree(action.source)
            .addTree(action.target)
        players[current].copy(
            sun = players[current].sun - players[current].costs[SEED_ACTION],
            isDormant = newDormant,
            treesSize = listOf(
                players[current].treesSize[SEED].addTree(action.target),
                players[current].treesSize[LITTLE],
                players[current].treesSize[MEDIUM],
                players[current].treesSize[GREAT]
            )
        )
    } else players[current].copy(treesSize = players[current].treesSize.toMutableList())
}

fun seed(action: SeedAction): StateBits {

    return this.copy(
        player = opp,
        players = listOf(
            playerSeed(ME, action),
            playerSeed(OPP, action)
        )
    )

}

fun playerWait(current: Int, action: Action): PlayerBit {
    return if (current == action.player)
        players[current].copy(
            isWaiting = true,
            treesSize = players[current].treesSize.toMutableList()
        )
    else players[current].copy(treesSize = players[current].treesSize.toMutableList())
}

fun wait(action: WaitAction): StateBits {
    return if (players[opp].isWaiting) newDay()
    else this.copy(
        player = opp,
        players = listOf(
            playerWait(ME, action),
            playerWait(OPP, action)
        )
    )
}


fun playerNewDay(current: Int, shadow: Map<Int, Int>, treesMap: Map<Int, Int>): PlayerBit {

    val income = players[ME].treesIndexes.fold(0) { acc, idTree ->
        acc + if (shadow[idTree] ?: 0 < treesMap[idTree]!!) treesMap[idTree]!! else 0
    }
    return players[current].copy(
        sun = players[current].sun + income,
        isWaiting = false,
        isDormant = 0,
        treesSize = players[current].treesSize.toMutableList()
    )
}

fun newDay(): StateBits {

    return if (day < MAX_DAY - 1) {
        val sunDir = day % 6
        val shadow = mutableMapOf<Int, Int>()

        val treesMap = players[ME].sizeMap + players[OPP].sizeMap
        treesMap.forEach { (idTree, size) ->
            for (i in 0 until size) {
                val index = Board[idTree].neighByDirection[sunDir].getOrNull(i)?.index ?: break
                shadow[index] = max(shadow[index] ?: 0, size)
            }
        }

        this.copy(
            player = ME,
            day = day + 1,
            players = listOf(
                playerNewDay(ME, shadow, treesMap),
                playerNewDay(OPP, shadow, treesMap)
            )
        )
    } else this.copy(day = day + 1)
}

//
//
//    fun simulateRandomGame(): Int {
////        log(this)
//        while (getStatus() == IN_PROGRESS) {
//            val action = getAvailableActions().random()
////            log(action)
//            when (action) {
//                is CompleteAction -> complete(action)
//                is GrowAction -> grow(action)
//                is SeedAction -> seed(action)
//                is WaitAction -> wait(action)
//            }
////            players[ME].firstAction = false
////            players[OPP].firstAction = false
//             //3 - player
//            if (players[ME].isWaiting && players[OPP].isWaiting) {
//                newDay()
//            } else {
//                player = if(action.player == ME ) OPP else ME
//            }
////            actions.clear()
////            log(this)
//
//        }
//
//        return getStatus()
//    }
//
    fun getStatus(): Int {
        return when {
            day < MAX_DAY -> IN_PROGRESS
            players[ME].calcScore() > players[OPP].calcScore() -> ME
            players[OPP].calcScore() > players[ME].calcScore() -> OPP
//            trees.count { it.owner == ME } > trees.count { it.owner == OPP } -> ME
//            trees.count { it.owner == OPP } > trees.count { it.owner == ME } -> OPP
            else -> DRAW
        }
    }
//
//
}