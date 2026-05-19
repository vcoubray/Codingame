package fr.vco.condigame.contests.springchallenge2026

enum class Phase { TRAINING, CHOPPING }

object Planner {

    private fun computePhase(me: Player, trainingPlan: List<TrainingStep>): Phase {
        var inv = me.inventory
        var count = me.trolls.count()
        var idx = count - 1
        while (idx in trainingPlan.indices) {
            val obj = trainingPlan[idx].trainCandidates.firstOrNull()
                ?: return Phase.CHOPPING
            val cost = obj.missingResources(inv, count)
            if (!cost.fruitsSatisfied()) return Phase.TRAINING
            inv = Inventory(
                plum = inv.plum - (count + obj.movementSpeed * obj.movementSpeed),
                lemon = inv.lemon - (count + obj.carryCapacity * obj.carryCapacity),
                apple = inv.apple - (count + obj.harvestPower * obj.harvestPower),
                banana = inv.banana,
                iron = inv.iron - (count + obj.chopPower * obj.chopPower),
                wood = inv.wood
            )
            count += 1
            idx += 1
        }
        return Phase.CHOPPING
    }

    fun plan(
        me: Player,
        trees: List<Tree>,
        trainingPlan: List<TrainingStep>,
        plantingArea: Set<Int>
    ): List<String> {
        val phase = computePhase(me, trainingPlan)
        val step = trainingPlan.getOrNull(me.trolls.count() - 1)
        val collectObjective = step?.collectObjective ?: TrainObjective(0, 0, 0, 0)
        val missingRes = collectObjective.missingResources(me.inventory, me.trolls.count())

        val plantingObjective: Map<String, Int> = when (phase) {
            Phase.TRAINING -> mapOf("PLUM" to 2, "LEMON" to 2, "APPLE" to 2)
            Phase.CHOPPING -> mapOf("BANANA" to plantingArea.size)
        }

        val neededTypes = buildSet {
            if (phase == Phase.TRAINING) {
                if (missingRes.plum > 0) add("PLUM")
                if (missingRes.lemon > 0) add("LEMON")
                if (missingRes.apple > 0) add("APPLE")
                if (me.inventory.banana == 0) add("BANANA")
            } else {
                if (me.inventory.banana < 1) add("BANANA")
            }
        }

        val treeGoals: List<Goal> = trees.map { tree ->
            when (phase) {
                Phase.TRAINING -> when {
                    tree.tileId in plantingArea -> HarvestGoal(tree, neededTypes)
                    tree.type in neededTypes    -> HarvestGoal(tree, neededTypes)
                    else                        -> ChopGoal(tree)
                }
                Phase.CHOPPING -> when {
                    tree.type == "BANANA" && "BANANA" in neededTypes -> HarvestGoal(tree, neededTypes)
                    else                                              -> ChopGoal(tree)
                }
            }
        }

        val emptyPlanterTiles = plantingArea.filter { tileId -> trees.none { it.tileId == tileId } }
        val plantDeficits = plantingObjective.mapValues { (type, target) ->
            maxOf(0, target - trees.count { it.tileId in plantingArea && it.type == type })
        }
        val plantGoals = plantDeficits.flatMap { (type, deficit) ->
            if (deficit <= 0) emptyList()
            else emptyPlanterTiles.map { PlantGoal(it, type, me.shackTiles, me.inventory) }
        }

        val mineGoals = if (missingRes.iron > 0) {
            val closestIron = Game.board.ironTiles.minByOrNull { iron ->
                me.shackTiles.mapNotNull { sh ->
                    Game.board.distances[iron][sh].takeIf { it >= 0 }
                }.minOrNull() ?: Int.MAX_VALUE
            }
            listOfNotNull(closestIron?.let { MineGoal(it) })
        } else emptyList()

        val goals: List<Goal> = treeGoals + plantGoals + mineGoals

        val scored = me.trolls.flatMap { troll ->
            goals.mapNotNull { goal -> goal.score(troll)?.let { Triple(it, troll, goal) } }
        }.sortedByDescending { it.first }

        val assignedTrolls = mutableSetOf<Troll>()
        val claimedGoals = mutableSetOf<Goal>()
        val plantTypeAssigned = mutableMapOf<String, Int>()
        val assignments = mutableListOf<Pair<Troll, Goal>>()

        if (phase == Phase.CHOPPING) {
            val farmerPair = scored.firstOrNull { (_, _, goal) ->
                goal is PlantGoal || (goal is HarvestGoal && goal.tree.type == "BANANA")
            }
            if (farmerPair != null) {
                val (_, farmer, farmerGoal) = farmerPair
                if (farmerGoal is PlantGoal) {
                    plantTypeAssigned[farmerGoal.treeType] =
                        plantTypeAssigned.getOrDefault(farmerGoal.treeType, 0) + 1
                }
                assignments += farmer to farmerGoal
                assignedTrolls += farmer
                claimedGoals += farmerGoal
            }
        }

        for ((_, troll, goal) in scored) {
            if (troll !in assignedTrolls && goal !in claimedGoals) {
                if (goal is PlantGoal) {
                    val assigned = plantTypeAssigned.getOrDefault(goal.treeType, 0)
                    if (assigned >= plantDeficits.getOrDefault(goal.treeType, 0)) continue
                    plantTypeAssigned[goal.treeType] = assigned + 1
                }
                assignments += troll to goal
                assignedTrolls += troll
                claimedGoals += goal
            }
        }

        val dropGoal = DropGoal(me.shackTiles)
        me.trolls
            .filter { it !in assignedTrolls && it.totalCarried > 0 }
            .forEach { assignments += it to dropGoal }

        val intents = assignments.map { (troll, goal) -> troll to goal.toIntent(troll) }
        val result = Movement.resolve(intents, Game.board)
        for ((troll, goal) in assignments) {
            val stepTile = result.stepTiles[troll] ?: troll.tileId
            printLog(goal.toLog(troll, stepTile))
        }
        val actions = result.actions.toMutableList()

        if (step != null) {
            val trollCount = me.trolls.count()
            val candidate = step.trainCandidates.firstOrNull { obj ->
                obj.missingResources(me.inventory, trollCount).isSatisfied()
            }
            if (candidate != null) actions += candidate.toAction()
        }

        return actions.ifEmpty { listOf("WAIT") }
    }
}
