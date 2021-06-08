package fr.vco.codingame.contest.springchallenge2021.heuristic

import fr.vco.codingame.contest.springchallenge2021.*
import fr.vco.codingame.contest.springchallenge2021.game.*
import kotlin.math.min

class Heuristic(
    val day: Int,
    val nutrients: Int,
    val sun: Int,
    val score: Int,
    val trees: List<Tree>
) {
    constructor(game: Game) : this(
        day = game.day,
        nutrients = game.nutrients,
        sun = game.sun,
        score = game.score,
        trees = game.realTrees
    )

    val treesIndexes = trees.associateBy { it.cellIndex }
    val myTrees = trees.filter { it.owner == ME }
    val rentability = Board.getRentabilityBoard(myTrees)
    val globalRentability = Board.getRentabilityBoard(myTrees)

    val costs = listOf(
        trees.count { it.size == 0 } + SEED_COST,
        trees.count { it.size == 1 } + GROW_0_COST,
        trees.count { it.size == 2 } + GROW_1_COST,
        trees.count { it.size == 3 } + GROW_2_COST,
        COMPLETE_COST
    )


    fun bestAction(): Action {

        val harvestableTrees = nutrients
        val maxTrees = harvestableTrees - trees.count { it.size == GREAT }
        val maxGreatTrees = min(5, maxTrees)

        val greatTrees = myTrees.filter { it.size == GREAT }
        val mediumTrees = myTrees.filter { it.size == MEDIUM }
        val availableMediumTrees = mediumTrees.filterNot { it.isDormant }
        val availableGreatTrees = greatTrees.filterNot { it.isDormant }
        val availableLittleTrees = myTrees.filter { it.size == LITTLE }.filterNot { it.isDormant }
        val availableSeeds = myTrees.filter { it.size == SEED }.filterNot { it.isDormant }

        val seedTrees = myTrees.filterNot { it.isDormant || it.size <= LITTLE }
        val shouldSeed = when {
            day >= 20 -> false
            seedTrees.isEmpty() -> false
            costs[SEED_ACTION] > sun -> false
            //player.trees.count() < maxTrees &&
            myTrees.count { it.size == SEED } < 1 -> true
            else -> false
        }
        if (shouldSeed) {
            val seedActions = seedTrees.map { t ->
                val targets = Board[t.cellIndex].neighByRange[t.size]
                targets
                    .asSequence()
                    .filter { it.richness > 0 }
                    .filter { c -> trees.none { c.index == it.cellIndex } }
                    .map { SeedAction(ME, t, it) }
                    .toList()
            }.flatten()

            seedActions.maxWithOrNull(
                compareBy(
                    { rentability[it.target] },
                    { Board[it.target].richness },
                    { -globalRentability[it.target] },
                    { -treesIndexes[it.source]!!.size },
                    { -Board[it.source].richness }
                )
            )?.let{return it}
        }

        val shouldComplete = when {
            availableGreatTrees.isEmpty() -> false
            costs[COMPLETE_ACTION] > sun -> false
            day == 23 -> true
            greatTrees.size > maxGreatTrees -> true
            greatTrees.size >= maxGreatTrees && availableMediumTrees.isNotEmpty() &&
                costs[COMPLETE_ACTION] + costs[GROW_2_ACTION] <= sun -> true
            else -> false
        }

        if (shouldComplete) {
            val target = availableGreatTrees.minWithOrNull(
                compareBy({ rentability[it.cellIndex] },
                    { -Board[it.cellIndex].richness })
            )!!
            val cost = costs[COMPLETE_ACTION]
            val gain = nutrients + BONUS_RICHNESS[Board[target.cellIndex].richness]
            if (gain + (sun - cost) / 3 > sun / 3)
                return CompleteAction(ME, target)
        }


        val shouldGrow2 = when {
            availableMediumTrees.isEmpty() -> false
            costs[GROW_2_ACTION] > sun -> false
            day >= 22 -> false
            availableLittleTrees.isNotEmpty() && availableSeeds.isNotEmpty() && mediumTrees.size < 2 &&
                costs[GROW_1_ACTION] + costs[GROW_0_ACTION] - 1 <= costs[GROW_2_ACTION] && day < 8 -> false
            else -> true
        }

        if (shouldGrow2)
            return GrowAction(ME, availableMediumTrees.firstOrNull()!!, costs[GROW_2_ACTION])

        if (availableLittleTrees.isNotEmpty() && costs[GROW_1_ACTION] <= sun && day < 21)
            return GrowAction(ME, availableLittleTrees.firstOrNull()!!, costs[GROW_1_ACTION])

        if (availableSeeds.isNotEmpty() && costs[GROW_0_ACTION] <= sun && day < 20)
            return GrowAction(ME, availableSeeds.firstOrNull()!!, costs[GROW_0_ACTION])

        return WaitAction(ME)
    }

}