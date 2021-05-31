package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*


data class Player(
    var score: Int = 0,
    var sun: Int = 0,
    var isWaiting: Boolean = false,
    val costs: MutableList<Int> = MutableList(5) { 0 },
//    var firstAction: Boolean = true
) {
//    var score: Int = 0
//    var sun: Int = 0
//    var isWaiting: Boolean = false
//    val costs: MutableList<Int> = MutableList(5) { 0 }
//    var firstAction: Boolean = true


    fun copyFromPlayer(player: Player) {
        this.score = player.score
        this.sun = player.sun
        this.isWaiting = player.isWaiting
        player.costs.forEachIndexed { i, it -> this.costs[i] = it }
    }

    fun canPay(actionId: Int) = sun >= (costs[actionId] + BASE_COST[actionId])

    fun calcScore() = score + sun / 3

    override fun toString() : String {
        return  "sun : ${sun}, score: $score, isWaiting: $isWaiting"
    }

}


data class State(
    var player: Int = ME,
    var day: Int = 0,
    var nutrients: Int  = 0,
    val trees: List<Tree> = List(BOARD_SIZE) { Tree(it) },
    val players: List<Player> = listOf(
        Player(), // ME
        Player() // OPP
    )
) {

//    val players = listOf(
//        //Player(), // Not a real player (just for id 0)
//        Player(), // ME
//        Player() // OPP
//    )

//    val players : Map<Int,Player> = mapOf(ME to Player(), OPP to Player())
//    val trees: List<Tree> = List(BOARD_SIZE) { Tree(it) }
//    private val actions: MutableList<Action> = mutableListOf() //by lazy(::getAvailableActions)
//    fun getAvailableActions(): List<Action>{
//        if (actions.isEmpty()) initAvailableActions()
//        return actions
//    }

    override fun toString(): String {
        return """
            *********************
            Day : $day, Nutrients : $nutrients
            Player : $player, ${players[player]}
            ${trees.filter { it.owner == player }.joinToString("\n            ")}
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
        //actions.clear()
    }

    fun copyFromState(state : State) = apply {
        day = state.day
        player = state.player
        nutrients = state.nutrients
        players.forEachIndexed{i, it -> it.copyFromPlayer(state.players[i]) }
        trees.forEach { tree ->
            tree.size = state.trees[tree.cellIndex].size
            tree.owner = state.trees[tree.cellIndex].owner
            tree.isDormant = state.trees[tree.cellIndex].isDormant
        }

    }

    fun getAvailableActions() : List<Action>{
        val actions = mutableListOf<Action>(WaitAction(player))

        val nonSeedableCell = if (
            players[player].costs[SEED_ACTION] == 0 &&
            players[player].canPay(SEED_ACTION)
        ) {
            trees.filter{it.owner==ME}.flatMap { Board[it.cellIndex].neighIndex}
        } else emptyList()


        if (!players[player].isWaiting) {
            trees.forEach forEachTree@{ tree ->
                if (tree.size == NONE) return@forEachTree
                if (tree.isDormant) return@forEachTree
                if (tree.owner != player) return@forEachTree
                if (tree.size == GREAT &&
                    day >= 12 &&
                    players[player].canPay(COMPLETE_ACTION)
//                    (players[player].costs[3] >= 4 || day > 20)
                ) {
                    actions.add(CompleteAction(player, tree))
                }
                if (tree.size < GREAT && players[player].canPay(GROW_ACTION[tree.size])) {
                    actions.add(GrowAction(player, tree))
                }
                if (tree.size > LITTLE &&
                    //players[player].firstAction &&
                    players[player].costs[SEED_ACTION] == 0 &&
                    players[player].canPay(SEED_ACTION)
                ) {
                    Board[tree.cellIndex].neighByRange[tree.size].forEach { target ->
                        if (trees[target.index].size == NONE && nonSeedableCell.none{target.index == it}) {
                            actions.add(SeedAction(player, tree, target))
                        }
                    }
                }
            }
        }
        return actions
    }

    fun child() = this.copy(
        trees = trees.map{it.copy()},
        players = players.map{it.copy(
            costs = it.costs.toMutableList()
        )}
    )
//    ).also{
//        it.players[ME].copyFromPlayer(this.players[ME])
//        it.players[OPP].copyFromPlayer(this.players[OPP])
//
////        this.trees.forEachIndexed { i, tree ->
////            it.trees[i].isDormant = tree.isDormant
////            it.trees[i].owner = tree.owner
////            it.trees[i].size = tree.size
////        }
//    }

//    fun copyCustom() = State().also {
//      //  it.parentPlayer = player
//        it.nutrients = nutrients
//        it.day = day
//        it.player = player
//        it.players[ME].copyFromPlayer(this.players[ME])
//        it.players[OPP].copyFromPlayer(this.players[OPP])
//
//        this.trees.forEachIndexed { i, tree ->
//            it.trees[i].isDormant = tree.isDormant
//            it.trees[i].owner = tree.owner
//            it.trees[i].size = tree.size
//        }
////        it.actions.clear()
//
//    }


    fun getNextState(action: Action): State {
        return child()
            .also {
            //it.action = action
                when (action) {
                    is CompleteAction -> it.complete(action)
                    is GrowAction -> it.grow(action)
                    is SeedAction -> it.seed(action)
                    is WaitAction -> it.wait(action)
                }
                it.player = if (action.player == ME) OPP else ME //3 - it.player

                if (it.players[ME].isWaiting && it.players[OPP].isWaiting) {
                    it.newDay()
                }
//                it.actions.clear()
        }
    }

    fun complete(action: CompleteAction){
        players[action.player].sun -= players[action.player].costs[COMPLETE_ACTION]
        players[action.player].costs[COMPLETE_ACTION]--
        players[action.player].score += nutrients + BONUS_RICHNESS[Board[action.treeId].richness]
        nutrients--
        trees[action.treeId].size = NONE
        trees[action.treeId].owner = -1
        trees[action.treeId].isDormant = false
    }

    fun grow(action : GrowAction) {
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

    fun wait(action: WaitAction){
        players[action.player].isWaiting = true
    }

    fun newDay() {
        day++
        if (day < MAX_DAY) {
            val invertSunDir = (day + 3) % 6
            trees.forEach {
                if (it.size > 0 && !isShadowed(it, invertSunDir))
                    players[it.owner].sun += it.size
            }

            players[ME].isWaiting = false
            players[OPP].isWaiting = false
//            players[ME].firstAction = true
//            players[OPP].firstAction = true

            trees.forEach { t -> t.isDormant = false }
            player = ME
        }
    }


    fun simulateRandomGame(): Int {
//        log(this)
        while (getStatus() == IN_PROGRESS) {
            val action = getAvailableActions().random()
//            log(action)
            when (action) {
                is CompleteAction -> complete(action)
                is GrowAction -> grow(action)
                is SeedAction -> seed(action)
                is WaitAction -> wait(action)
            }
//            players[ME].firstAction = false
//            players[OPP].firstAction = false
             //3 - player
            if (players[ME].isWaiting && players[OPP].isWaiting) {
                newDay()
            } else {
                player = if(action.player == ME ) OPP else ME
            }
//            actions.clear()
//            log(this)

        }

        return getStatus()
    }


    fun isShadowed(tree: Tree, invertSunDir: Int): Boolean {
        return Board[tree.cellIndex].neighByDirection[invertSunDir].any { trees[it.index].size >= tree.size }
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


}