package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*


data class Player(
    var score: Int = 0,
    var sun: Int = 0,
    var isWaiting: Boolean = false,
    val costs: Array<Int> = Array(5) { 0 },
) {

    fun initFromPlayer(player: Player) {
        this.score = player.score
        this.sun = player.sun
        this.isWaiting = player.isWaiting
        player.costs.forEachIndexed { i, it -> this.costs[i] = it }
    }

    fun canPay(actionId: Int) = sun >= (costs[actionId])

    fun calcScore() = score + sun / 3

    override fun toString(): String {
        return "sun : ${sun}, score: $score, isWaiting: $isWaiting"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (score != other.score) return false
        if (sun != other.sun) return false
        if (isWaiting != other.isWaiting) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + sun
        result = 31 * result + isWaiting.hashCode()
        return result
    }

}


data class State(
    var player: Int = ME,
    var day: Int = 0,
    var nutrients: Int = 0,
    val trees: Array<Tree> = Array(BOARD_SIZE) { Tree(it) },
    val players: Array<Player> = arrayOf(
        Player(), // ME
        Player() // OPP
    )
) {

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


    fun initFromGame(game: Game) = apply {
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

    fun initFromState(state: State) = apply {
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

        val nonSeedableCell = if (
            players[player].costs[SEED_ACTION] == 0
        ) {
            trees.filter { it.owner == ME }.flatMap { Board[it.cellIndex].neighIndex }
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
                actions.add(GrowAction(player, tree))
            }
            if (tree.size > LITTLE &&
                //players[player].firstAction &&
                players[player].costs[SEED_ACTION] == 0 &&
                players[player].canPay(SEED_ACTION)
            ) {

                val targets = Board[tree.cellIndex].neighByRange[tree.size]
                    .filter{target -> trees[target.index].size == NONE}
                (targets.filter{target -> nonSeedableCell.none { target.index == it }}
                    .takeIf{ it.isNotEmpty() }?:targets)
                    .forEach{ target -> actions.add(SeedAction(player, tree, target)) }
//                Board[tree.cellIndex].neighByRange[tree.size].forEach { target ->
//                    if (trees[target.index].size == NONE && nonSeedableCell.none { target.index == it }) {
//                        actions.add(SeedAction(player, tree, target))
//                    }
//                }
            }
        }
        return actions.takeIf{it.isNotEmpty()}?:listOf(WaitAction(player))
    }

    fun basicCopy() = this.copy(
        trees = trees.copyOf(),
        players = players.map {
            it.copy(
                costs = it.costs.copyOf()
            )
        }.toTypedArray()
    )

    fun getNextState(action: Action): State {
        return basicCopy().also {
            when (action) {
                is CompleteAction -> it.complete(action)
                is GrowAction -> it.grow(action)
                is SeedAction -> it.seed(action)
                is WaitAction -> it.wait(action)
            }
            it.player = switchPlayer(action.player)

            if (it.players[ME].isWaiting && it.players[OPP].isWaiting) {
                it.newDay()
            }
        }
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
                    it.isDormant = false
                }

            }
            players[ME].isWaiting = false
            players[OPP].isWaiting = false
            player = ME
        }
    }


    fun simulateRandomGame(): Int {
        while (getStatus() == IN_PROGRESS) {
            val action = getAvailableActions().random()
//            log(this)
//            log(action)
//            log("--------")
            when (action) {
                is CompleteAction -> complete(action)
                is GrowAction -> grow(action)
                is SeedAction -> seed(action)
                is WaitAction -> wait(action)
            }
            //3 - player
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