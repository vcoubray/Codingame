package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*


data class PlayerBit(
    var score: Int = 0,
    var sun: Int = 0,
    var isWaiting: Boolean = false,
    val trees: LongArray = LongArray(4) { 0L },
    val isDormant: Trees = 0

) {

//    val costs by lazy {
//        listOf(
//            treesSize[0].size() + SEED_COST,
//            treesSize[1].size() + GROW_0_COST,
//            treesSize[2].size() + GROW_1_COST,
//            treesSize[3].size() + GROW_2_COST,
//            COMPLETE_COST
//        )
//    }


    constructor(score: Int, sun: Int, isWaiting: Boolean, trees: List<Tree>) : this(
        score = score,
        sun = sun,
        isWaiting = isWaiting,
        trees = longArrayOf(
            trees.filter { it.size == 0 }.getBits(),
            trees.filter { it.size == 1 }.getBits(),
            trees.filter { it.size == 2 }.getBits(),
            trees.filter { it.size == 3 }.getBits()
        ),
        isDormant = trees.filter { it.isDormant }.getBits()
    )


    //val trees = treesSize.reduce { acc, l -> acc or l }
    //val treesIndexes = trees.getIndexes()
    val sizeMap = trees.reduce { acc, l -> acc or l }.getIndexes().map { it to getTreeSize(it) }.toMap()
//    val sizeMap2 = trees.flatMapIndexed{ size, t -> (0 until BOARD_SIZE) }
    fun getTreeSize(idTree: Int) = trees.indexOfFirst { it[idTree] == 1L }


    fun copyComplete(action: CompleteAction, nutrients: Int): PlayerBit {
        return this.copy(
            sun = sun - action.cost,
            score = score + nutrients + BONUS_RICHNESS[Board[action.treeId].richness],
            trees = longArrayOf(
                trees[SEED],
                trees[LITTLE],
                trees[MEDIUM],
                trees[GREAT].removeTree(action.treeId)
            )
        )
    }

    fun copyGrow(action: GrowAction): PlayerBit {
        val newTrees = trees.copyOf()
        newTrees[action.size] = newTrees[action.size].removeTree(action.treeId)
        newTrees[action.size + 1] = newTrees[action.size + 1].addTree(action.treeId)
        return this.copy(
            sun = sun - action.cost,
            isDormant = isDormant.addTree(action.treeId),
            trees = newTrees
        )
    }

    fun copySeed(action: SeedAction): PlayerBit {
        return this.copy(
            sun = sun - action.cost,
            isDormant = isDormant
                .addTree(action.source)
                .addTree(action.target),
            trees = longArrayOf(
                trees[SEED].addTree(action.target),
                trees[LITTLE],
                trees[MEDIUM],
                trees[GREAT]
            )
        )
    }

    fun copyWait(): PlayerBit {
        return copy(
            isWaiting = true,
            trees = trees.copyOf()
        )
    }

    fun copyNewDay(invertSunDir: Int, treesMap: Map<Int, Int>): PlayerBit {

        var income = 0
        sizeMap.forEach { (idTree, size) ->
            if (Board[idTree].neighByDirection[invertSunDir].filterIndexed { i, cell ->
                    val shadowSize = treesMap[cell.index] ?: 0
                    shadowSize > i && shadowSize >= size
                }.isEmpty()) income += size
        }

        return copy(
            sun = sun + income,
            isWaiting = false,
            isDormant = 0,
            trees = trees.copyOf()
        )
    }


    fun calcScore() = score + sun / 3

    override fun toString(): String {
        return "sun : ${sun}, score: $score, isWaiting: $isWaiting, trees : $sizeMap"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerBit

        if (score != other.score) return false
        if (sun != other.sun) return false
        if (isWaiting != other.isWaiting) return false
        if (!trees.contentEquals(other.trees)) return false
        if (isDormant != other.isDormant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + sun
        result = 31 * result + isWaiting.hashCode()
        result = 31 * result + trees.contentHashCode()
        result = 31 * result + isDormant.hashCode()
        return result
    }

}


data class StateBits(
    var player: Int = ME,
    var day: Int = 0,
    var nutrients: Int = 0,
//    val players: List<PlayerBit> = listOf(
//        PlayerBit(), // ME
//        PlayerBit() // OPP
//    )
    val players: Array<PlayerBit> = arrayOf(
        PlayerBit(), // ME
        PlayerBit() // OPP
    )
) {

    companion object {
        val INVERT_SUN_DIR = List(MAX_DAY) { (it + 3) % 6 }.toTypedArray()
    }


    constructor(game: Game) : this(
        player = ME,
        day = game.day,
        nutrients = game.nutrients,
        players = arrayOf(
            PlayerBit(game.score, game.sun, false, game.realTrees.filter { it.owner == ME }),
            PlayerBit(game.oppScore, game.oppSun, game.oppIsWaiting, game.realTrees.filter { it.owner == OPP })
        )
    )

    //val opp by lazy { if (player == ME) OPP else ME }


    override fun toString(): String {
        return """
            *********************
            Day : $day, Nutrients : $nutrients
            Player : $player 
            ME : ${players[ME]}
            OPP: ${players[OPP]}
        """.trimIndent()
    }

    fun getAvailableActions(): List<Action> {
        val actions = mutableListOf<Action>(WaitAction(player))


        val costs = listOf(
            players[player].trees[0].size() + SEED_COST,
            players[player].trees[1].size() + GROW_0_COST,
            players[player].trees[2].size() + GROW_1_COST,
            players[player].trees[3].size() + GROW_2_COST,
            COMPLETE_COST
        )

        val sun = players[player].sun
        val canSeed = players[player].trees[SEED].size() == 0 && sun >= costs[SEED_ACTION]
        //val canSeed = players[player].trees[0].size() == 0 && players[player].canPay(SEED_ACTION)
        val canComplete = day >= 12 && sun >= costs[COMPLETE_ACTION]

        val nonSeedableCell =
            if (canSeed) players[player].sizeMap.keys.flatMap { Board[it].neighIndex }
            else emptyList()


        val totalTrees = players[ME].sizeMap + players[OPP].sizeMap

        if (!players[player].isWaiting) {
            players[player].sizeMap.forEach forEachLoop@{ (treeId, size) ->
                if (players[player].isDormant[treeId] == 1L) return@forEachLoop
                if (canComplete && size == GREAT) {
                    actions.add(CompleteAction(player, treeId))
                } else if (size < GREAT && sun > costs[GROW_ACTION[size]]) {
                    actions.add(GrowAction(player, treeId, size))
                }
                if (size >= MEDIUM && canSeed) {
                    Board[treeId].neighByRange[size].forEach { target ->
                        if (totalTrees[target.index] != null && nonSeedableCell.none { c -> target.index == c }) {
                            actions.add(SeedAction(player, treeId, target.index))
                        }
                    }
                }
            }
        }
        return actions
    }

    fun getNextState(action: Action): StateBits {
        return when (action) {
            is CompleteAction -> copyComplete(action)
            is GrowAction -> grow(action)
            is SeedAction -> seed(action)
            is WaitAction -> wait(action)
        }
    }

    fun copyComplete(action: CompleteAction): StateBits {
        return this.copy(
            player = otherPlayer(),
            nutrients = nutrients - 1,
//            players = players.mapIndexed { i, p ->
//                if (action.player == i) p.copyComplete(
//                    action,
//                    nutrients
//                ) else p.copy()
//            }.toTypedArray()
            players = arrayOf(
                if (action.player == ME) players[ME].copyComplete(action,nutrients) else players[ME].copy(),
                if (action.player == OPP) players[OPP].copyComplete(action,nutrients) else players[OPP].copy()
            )

        )
    }

    fun grow(action: GrowAction): StateBits {
        return this.copy(
            player = otherPlayer(),
//            players = players.mapIndexed { i, p -> if (action.player == i) p.copyGrow(action) else p.copy() }
//                .toTypedArray()
            players = arrayOf(
                if (action.player == ME) players[ME].copyGrow(action) else players[ME].copy(),
                if (action.player == OPP) players[OPP].copyGrow(action) else players[OPP].copy()
            )
        )
    }

    fun seed(action: SeedAction): StateBits {
        return this.copy(
            player = otherPlayer(),
//            players = players.mapIndexed { i, p -> if (action.player == i) p.copySeed(action) else p.copy() }
//                .toTypedArray()
            players = arrayOf(
                if (action.player == ME) players[ME].copySeed(action) else players[ME].copy(),
                if (action.player == OPP) players[OPP].copySeed(action) else players[OPP].copy()
            )
        )
    }

    fun wait(action: WaitAction): StateBits {
        return if (players[otherPlayer()].isWaiting) copyNewDay(action)
        else this.copy(
            player = otherPlayer(),
//            players = players.mapIndexed { i, p -> if (action.player == i) p.copyWait() else p.copy() }.toTypedArray()
            players = arrayOf(
                if (action.player == ME) players[ME].copyWait() else players[ME].copy(),
                if (action.player == OPP) players[OPP].copyWait() else players[OPP].copy()
            )
        )
    }

    fun copyNewDay(action: WaitAction): StateBits {

        return if (day + 1 < MAX_DAY) {
            val invertSunDir = INVERT_SUN_DIR[day]
            val treesMap = players[ME].sizeMap + players[OPP].sizeMap
            this.copy(
                player = ME,
                day = day + 1,
//                players = players.map { it.copyNewDay(invertSunDir, treesMap) }.toTypedArray()
                players = arrayOf(
                    if (action.player == ME) players[ME].copyNewDay(invertSunDir, treesMap) else players[ME].copy(),
                    if (action.player == OPP) players[OPP].copyNewDay(invertSunDir, treesMap) else players[OPP].copy()
                )
            )
        } else this.copy(day = day + 1)
    }

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
//            //3 - player
//            if (players[ME].isWaiting && players[OPP].isWaiting) {
//                newDay()
//            } else {
//                player = if (action.player == ME) OPP else ME
//            }
////            actions.clear()
////            log(this)
//
//        }
//
//        return getStatus()
//    }

    fun otherPlayer() = if (player == ME) OPP else ME

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