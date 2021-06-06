package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*


data class State(
    var player: Int = ME,
    var day: Int = 0,
    var nutrients: Int = 0,
) {

    val trees: List<Tree> = List(BOARD_SIZE) { Tree(it) }
    val players: List<Player> = listOf(
        Player(), // ME
        Player() // OPP
    )

    companion object {
        val INVERT_SUN_DIR = List(MAX_DAY) { (it + 3) % 6 }.toTypedArray()
    }

    override fun toString(): String {
        return """
            *********************
            Day : $day, Nutrients : $nutrients, player : $player
            ME : ${players[ME]}
            OPP : ${players[OPP]}
            ${trees.filter { it.owner != NONE }.joinToString("\n            ")}
        """.trimIndent()
    }

    private fun calcCosts(player: Int = ME) {
        players[player].costs[SEED_ACTION] = trees.count { it.size == 0 && it.owner == player } + SEED_COST
        players[player].costs[GROW_0_ACTION] = trees.count { it.size == 1 && it.owner == player } + GROW_0_COST
        players[player].costs[GROW_1_ACTION] = trees.count { it.size == 2 && it.owner == player } + GROW_1_COST
        players[player].costs[GROW_2_ACTION] = trees.count { it.size == 3 && it.owner == player } + GROW_2_COST
        players[player].costs[COMPLETE_ACTION] = COMPLETE_COST
    }


    fun loadFromGame(game: Game) = apply {
        day = game.day
        nutrients = game.nutrients

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

    fun loadFromState(state: State) = apply {
        day = state.day
        player = state.player
        nutrients = state.nutrients
        players.forEachIndexed { i, it -> it.initFromPlayer(state.players[i]) }
        trees.forEach { tree ->
            tree.size = state.trees[tree.cellIndex].size
            tree.owner = state.trees[tree.cellIndex].owner
            tree.isDormant = state.trees[tree.cellIndex].isDormant
        }

    }


    fun getAvailableActions(): List<Action> {
        if (players[player].isWaiting) return listOf(WaitAction(player))

        val actions = mutableListOf<Action>()
        val seedActions = mutableListOf<Action>()
        val nonSeedableCell = if (
            players[player].costs[SEED_ACTION] == 0
        ) {
            trees.filter { it.owner == player }.flatMap { Board[it.cellIndex].neighIndex }
        } else emptyList()


        trees.forEach forEachTree@{ tree ->
            if (tree.size == NONE) return@forEachTree
            if (tree.isDormant) return@forEachTree
            if (tree.owner != player) return@forEachTree
            if (tree.size == GREAT && day >= 12 &&
                players[player].canPay(COMPLETE_ACTION)
            ) {
                actions.add(CompleteAction(player, tree))
            } else if (tree.size < GREAT && players[player].canPay(GROW_ACTION[tree.size])) {
                actions.add(GrowAction(player, tree, GROW_ACTION[tree.size]))
            }
            if (tree.size > LITTLE && players[player].costs[SEED_ACTION] == 0) {
                Board[tree.cellIndex].neighByRange[tree.size]
                    .filter { target -> trees[target.index].size == NONE && nonSeedableCell.none { target.index == it } }
                    .forEach { target -> seedActions.add(SeedAction(player, tree, target)) }
            }
        }
        actions.addAll(seedActions.takeIf { it.isNotEmpty() } ?: listOf(WaitAction(player)))
        return actions
    }


    fun getAvailableActionsNew(): List<Action> {
        if (players[player].isWaiting) return listOf(WaitAction(player))

        val actions = mutableListOf<Action>()
        val seedActions = mutableListOf<Action>()
        val nonSeedableCell = if (
            players[player].costs[SEED_ACTION] == 0
        ) {
            trees.filter { it.owner == ME }.flatMap { Board[it.cellIndex].neighIndex }
        } else emptyList()

        val playerTrees = trees.filter { it.owner == player && !it.isDormant }

        val canComplete = day >= 12 && players[player].canPay(COMPLETE_ACTION)
            && (players[player].costs[COMPLETE_ACTION] > 4 || day >= 21)

        val completeActions = if (canComplete) {
            playerTrees.filter { it.size == GREAT }.map { CompleteAction(player, it) }
        } else emptyList()

//        val growActions = if(completeActions.isEmpty() ){
//            playerTrees.filter{it.size< GREAT && players[player].canPay(GROW_ACTION[it.size])).map{GrowAction(player, it,GROW_ACTION[it.size])}
//        }else emptyList()
//
        playerTrees.forEach { tree ->
            if (tree.size == GREAT && day >= 12 &&
                players[player].canPay(COMPLETE_ACTION)
            ) {
                actions.add(CompleteAction(player, tree))
            } else if (tree.size < GREAT && players[player].canPay(GROW_ACTION[tree.size])) {
                actions.add(GrowAction(player, tree, players[player].costs[GROW_ACTION[tree.size]]))
            }
            if (tree.size > LITTLE && players[player].costs[SEED_ACTION] == 0) {
                val targets = Board[tree.cellIndex].neighByRange[tree.size]
                    .filter { target -> trees[target.index].size == NONE && nonSeedableCell.none { target.index == it } }
//                    .takeIf { it.isNotEmpty() } ?: targets)
                    .forEach { target -> seedActions.add(SeedAction(player, tree, target)) }
//                Board[tree.cellIndex].neighByRange[tree.size].forEach { target ->
//                    if (trees[target.index].size == NONE && nonSeedableCell.none { target.index == it }) {
//                        actions.add(SeedAction(player, tree, target))
//                    }
//                }
            }
        }
        actions.addAll(seedActions.takeIf { it.isNotEmpty() } ?: listOf(WaitAction(player)))
        return actions//.takeIf{it.isNotEmpty()}?:listOf(WaitAction(player))
    }


    fun play(action: Action) :State {

        when (action) {
            is CompleteAction -> complete(action)
            is GrowAction -> grow(action)
            is SeedAction -> seed(action)
            is WaitAction -> wait(action)
        }
        player = switchPlayer(action.player)

        if (players[ME].isWaiting && players[OPP].isWaiting) {
            newDay()
        }
        return this
    }



    fun complete(action: CompleteAction) {
        players[action.player].sun -= players[action.player].costs[COMPLETE_ACTION]
        players[action.player].costs[GROW_ACTION[MEDIUM]]--
        players[action.player].score += nutrients + BONUS_RICHNESS[Board[action.treeId].richness]
        nutrients--
        trees[action.treeId].size = NONE
        trees[action.treeId].owner = NONE
        trees[action.treeId].isDormant = false
    }

    fun grow(action: GrowAction) {
        players[action.player].sun -= players[action.player].costs[GROW_ACTION[action.size]]
        players[action.player].costs[action.size]--
        players[action.player].costs[action.size + 1]++
        trees[action.treeId].size++
        trees[action.treeId].isDormant = true
    }

    fun seed(action: SeedAction) {
        players[action.player].sun -= players[action.player].costs[SEED_ACTION]
        players[action.player].costs[SEED_ACTION]++
        trees[action.target].size = SEED
        trees[action.target].isDormant = true
        trees[action.target].owner = action.player
        trees[action.source].isDormant = true
    }

    fun wait(action: WaitAction) {
        players[action.player].isWaiting = true
    }

    fun newDay() {
        day++
        if (day < MAX_DAY) {
            val invertSunDir = INVERT_SUN_DIR[day]
            trees.forEach {
                if (it.size > 0 && !isShadowed(it, invertSunDir)) {
                    players[it.owner].sun += it.size
                }
                it.isDormant = false
            }
            players[ME].isWaiting = false
            players[OPP].isWaiting = false
            player = ME
        }
    }


    fun getAvailableActionsForRollout(): List<Action> {
        if (players[player].isWaiting) return listOf(WaitAction(player))


        val canComplete = day >= 12 && players[player].canPay(COMPLETE_ACTION)
            && (players[player].costs[COMPLETE_ACTION] > 4 || day >= 21)
        val canGrow = day < 19

        val activeTrees = trees.filter { it.size != NONE && it.owner == player && !it.isDormant }

        val actions =
            activeTrees.filter { it.size == GREAT && canComplete }.map { CompleteAction(player, it) }
                .takeIf { it.isNotEmpty() }
                ?: activeTrees.filter { canGrow && it.size < GREAT && players[player].canPay(GROW_ACTION[it.size]) }
                    .map { GrowAction(player, it, GROW_ACTION[it.size]) }

        return actions.takeIf { it.isNotEmpty() }
            ?: activeTrees.filter { it.size > LITTLE && players[player].costs[SEED_ACTION] == 0 }
                .flatMap { tree ->
                    val nonSeedableCell = trees.filter { it.owner == player }.flatMap { Board[it.cellIndex].neighIndex }
                    Board[tree.cellIndex].neighByRange[tree.size]
                        .filter { target -> trees[target.index].size == NONE && nonSeedableCell.none { target.index == it } }
                        .map { target -> SeedAction(player, tree, target) }
                }.takeIf { it.isNotEmpty() } ?: listOf(WaitAction(player))
    }







    fun simulateRandomGame(): Int {
        while (getStatus() == IN_PROGRESS) {
            val action = getAvailableActions().random()
//            val action = actions

            when (action) {
                is CompleteAction -> complete(action)
                is GrowAction -> grow(action)
                is SeedAction -> seed(action)
                is WaitAction -> wait(action)
            }
            if (players[ME].isWaiting && players[OPP].isWaiting) {
                newDay()
            } else {
                player = switchPlayer(action.player)
            }
        }

        return getStatus()
    }


    fun isShadowed(tree: Tree, invertSunDir: Int): Boolean {
        return Board[tree.cellIndex].neighByDirection[invertSunDir].filterIndexed { i, cell ->
            val shadowSize = trees[cell.index].size
            shadowSize > i && shadowSize >= tree.size
        }.isNotEmpty()
    }

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


//    fun getScore(): Float {
//        val diff = players[ME].calcScore() - players[OPP].calcScore()
//        return when {
//            diff > 0 -> 1f
//            diff < 0 -> 0f
//            else -> 0.5f
//         }
//    }

    fun getScore(): Float {
        val myScore = players[ME].calcScore().toFloat()
        val oppScore = players[OPP].calcScore().toFloat()
        return when {
            myScore > oppScore -> {
                val diff = myScore - oppScore
                if (diff > 5) 1.0f + (diff - 5f) * 0.001f
                else 0.5f + 0.5f * diff / 5f
            }
            myScore < oppScore -> {
                val diff = oppScore - myScore
                if (diff > 5f) -1.0f - (diff - 5f) * 0.001f
                else -0.5f - 0.5f * diff / 5f

            }
            else -> {
                val myTrees = trees.count { it.owner == ME }
                val oppTrees = trees.count { it.owner == OPP }
                when {
                    myTrees > oppTrees -> 0.25f + myScore * 0.001f
                    myTrees < oppTrees -> -0.25f + myScore * 0.001f
                    else -> myScore * 0.001f
                }
            }
        }
    }

    fun switchPlayer(player: Int) = if (player == ME) OPP else ME

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (player != other.player) return false
        if (day != other.day) return false
        if (nutrients != other.nutrients) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player
        result = 31 * result + day
        result = 31 * result + nutrients
        return result
    }


}