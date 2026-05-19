package fr.vco.condigame.contests.springchallenge2026

import kotlin.math.min

sealed class Intent {
    data class Move(val targetTileIds: List<Int>) : Intent()
    data class Act(val command: String) : Intent()
}

sealed class Goal {
    abstract fun score(troll: Troll): Double?
    abstract fun toIntent(troll: Troll): Intent
    abstract fun toLog(troll: Troll, stepDest: Int): String
}

class HarvestGoal(val tree: Tree, val neededTypes: Set<String>) : Goal() {
    override fun score(troll: Troll): Double? {
        if (tree.type !in neededTypes && neededTypes.isNotEmpty()) return null
        val distance = Game.board.distances[troll.tileId][tree.tileId]
        if (distance == -1 || !troll.hasCapacity()) return null
        val turns = troll.turnsToReach(distance)
        val availableFruits = if (turns == 0) tree.fruits else tree.availableFruitsIn(turns)
        val expectedYield = min(troll.harvestPower, min(availableFruits, troll.carryCapacity - troll.totalCarried))
        if (expectedYield == 0) return null
        val distTreeToShack = Game.me().shackTiles
            .map { Game.board.distances[tree.tileId][it] }
            .filter { it >= 0 }
            .minOrNull() ?: return null
        val returnTurns = troll.turnsToReach(distTreeToShack)
        val priorityWeight = if (tree.type in neededTypes) 3.0 else 1.0
        return priorityWeight * expectedYield * 5.0 - turns - returnTurns
    }

    override fun toIntent(troll: Troll): Intent {
        val distance = Game.board.distances[troll.tileId][tree.tileId]
        return if (distance == 0) Intent.Act("HARVEST ${troll.id}")
        else Intent.Move(listOf(tree.tileId))
    }

    override fun toLog(troll: Troll, stepDest: Int): String {
        val (px, py) = Game.board.getCoord(troll.tileId)
        val (fx, fy) = Game.board.getCoord(tree.tileId)
        val (sx, sy) = Game.board.getCoord(stepDest)
        return "[${troll.id}] @($px,$py) HARVEST ${tree.type} final=($fx,$fy) step=($sx,$sy)"
    }
}

class PlantGoal(val targetTileId: Int, val treeType: String, val shackTiles: List<Int>, val inventory: Inventory = Inventory()) : Goal() {
    override fun score(troll: Troll): Double? {
        val distToTarget = Game.board.distances[troll.tileId][targetTileId]
        if (distToTarget == -1) return null
        return if (troll.carriesFruitType(treeType)) {
            8.0 - troll.turnsToReach(distToTarget)
        } else {
            if (!troll.hasCapacity()) return null
            val available = when (treeType) {
                "PLUM"   -> inventory.plum
                "LEMON"  -> inventory.lemon
                "APPLE"  -> inventory.apple
                "BANANA" -> inventory.banana
                else     -> 0
            }
            if (available <= 0) return null
            val distToShack = shackTiles
                .map { Game.board.distances[troll.tileId][it] }
                .filter { it >= 0 }
                .minOrNull() ?: return null
            5.0 - troll.turnsToReach(distToShack)
        }
    }

    override fun toIntent(troll: Troll): Intent {
        return if (troll.carriesFruitType(treeType)) {
            val distToTarget = Game.board.distances[troll.tileId][targetTileId]
            if (distToTarget == 0) Intent.Act("PLANT ${troll.id} $treeType")
            else Intent.Move(listOf(targetTileId))
        } else {
            val minDist = shackTiles.minOf { Game.board.distances[troll.tileId][it] }
            if (minDist == 0) Intent.Act("PICK ${troll.id} $treeType")
            else Intent.Move(shackTiles)
        }
    }

    override fun toLog(troll: Troll, stepDest: Int): String {
        val (px, py) = Game.board.getCoord(troll.tileId)
        val (fx, fy) = Game.board.getCoord(targetTileId)
        val (sx, sy) = Game.board.getCoord(stepDest)
        val phase = if (troll.carriesFruitType(treeType)) "PLANT" else "PICK&PLANT"
        return "[${troll.id}] @($px,$py) $phase $treeType final=($fx,$fy) step=($sx,$sy)"
    }
}

class DropGoal(val shackTiles: List<Int>) : Goal() {
    override fun score(troll: Troll): Double? = null

    override fun toIntent(troll: Troll): Intent {
        val minDist = shackTiles.minOf { Game.board.distances[troll.tileId][it] }
        return if (minDist == 0) Intent.Act("DROP ${troll.id}")
        else Intent.Move(shackTiles)
    }

    override fun toLog(troll: Troll, stepDest: Int): String {
        val (px, py) = Game.board.getCoord(troll.tileId)
        val nearest = shackTiles.minBy { Game.board.distances[troll.tileId][it] }
        val (fx, fy) = Game.board.getCoord(nearest)
        val (sx, sy) = Game.board.getCoord(stepDest)
        return "[${troll.id}] @($px,$py) DROP final=($fx,$fy) step=($sx,$sy)"
    }
}

class ChopGoal(val tree: Tree) : Goal() {
    override fun score(troll: Troll): Double? {
        if (troll.chopPower == 0 || !troll.hasCapacity()) return null
        val distance = Game.board.distances[troll.tileId][tree.tileId]
        if (distance == -1) return null
        val turns = troll.turnsToReach(distance)
        val expectedYield = min(tree.size, troll.carryCapacity - troll.totalCarried)
        if (expectedYield == 0) return null
        val turnsToChop = (tree.health + troll.chopPower - 1) / troll.chopPower
        val distToShack = Game.me().shackTiles
            .map { Game.board.distances[tree.tileId][it] }
            .filter { it >= 0 }
            .minOrNull() ?: return null
        val returnTurns = troll.turnsToReach(distToShack)
        return expectedYield * 20.0 - turns - turnsToChop - returnTurns
    }

    override fun toIntent(troll: Troll): Intent {
        val distance = Game.board.distances[troll.tileId][tree.tileId]
        return if (distance == 0) Intent.Act("CHOP ${troll.id}")
        else Intent.Move(listOf(tree.tileId))
    }

    override fun toLog(troll: Troll, stepDest: Int): String {
        val (px, py) = Game.board.getCoord(troll.tileId)
        val (fx, fy) = Game.board.getCoord(tree.tileId)
        val (sx, sy) = Game.board.getCoord(stepDest)
        return "[${troll.id}] @($px,$py) CHOP ${tree.type} final=($fx,$fy) step=($sx,$sy)"
    }
}

class MineGoal(val targetTileId: Int) : Goal() {
    override fun score(troll: Troll): Double? {
        if (troll.chopPower == 0 || !troll.hasCapacity()) return null
        val distance = Game.board.distances[troll.tileId][targetTileId]
        if (distance == -1) return null
        val turns = troll.turnsToReach(distance)
        val expectedYield = min(troll.chopPower, troll.carryCapacity - troll.totalCarried)
        val distToShack = Game.me().shackTiles
            .map { Game.board.distances[targetTileId][it] }
            .filter { it >= 0 }
            .minOrNull() ?: return null
        val returnTurns = troll.turnsToReach(distToShack)
        return 100.0 + expectedYield * 5.0 - turns - returnTurns
    }

    override fun toIntent(troll: Troll): Intent {
        val distance = Game.board.distances[troll.tileId][targetTileId]
        return if (distance == 0) Intent.Act("MINE ${troll.id}")
        else Intent.Move(listOf(targetTileId))
    }

    override fun toLog(troll: Troll, stepDest: Int): String {
        val (px, py) = Game.board.getCoord(troll.tileId)
        val (fx, fy) = Game.board.getCoord(targetTileId)
        val (sx, sy) = Game.board.getCoord(stepDest)
        return "[${troll.id}] @($px,$py) MINE final=($fx,$fy) step=($sx,$sy)"
    }
}
