package fr.vco.condigame.contests.springchallenge2026

import kotlin.math.max
import kotlin.math.min


const val ME = 0
const val OPP = 1

const val TREE_SIZE_MAX = 4

class TreeType(
    val type: String,
    val cooldown: Int,
    val waterCooldown: Int,
    val healths: List<Int>
)

val TREE_TYPES = mapOf(
    "PLUM" to TreeType("PLUM", 8,3, listOf(6,8,10,12)),
    "LEMON" to TreeType("LEMON", 8,3, listOf(6,8,10,12)),
    "APPLE" to TreeType("APPLE", 9,2, listOf(11,14,17,20)),
    "BANANA" to TreeType("BANANA", 6,4, listOf(3,4,5,6)),

)



fun main() {
    Game.init()
    val me = Game.me()
    val plantingArea = (me.shackTiles + me.shackTiles.flatMap { Game.board.neighbours[it] }).toSet()
    val trainingPlan = listOf(
        TrainingStep(
            collectObjective = TrainObjective(1, 1, 1, 1),
            trainCandidates  = listOf(TrainObjective(2, 2, 2, 2), TrainObjective(1, 1, 1, 1))
        ),
        TrainingStep(
            collectObjective = TrainObjective(2, 2, 2, 2),
            trainCandidates  = listOf(TrainObjective(2, 2, 2, 2), TrainObjective(2, 2, 1, 2))
        ),
        TrainingStep(
            collectObjective = TrainObjective(2, 2, 1, 2),
            trainCandidates  = listOf(TrainObjective(2, 2, 1, 2), TrainObjective(2, 2, 0, 2))
        ),
    )

    while (true) {
        Game.update()
        println(Planner.plan(me, Game.trees, trainingPlan, plantingArea).joinToString(";"))
    }
}


object Game {

    lateinit var board: Board
    lateinit var trees: List<Tree>
    val players = List(2) { Player(it) }

    fun init() {
        val (width, height) = readInts()
        val grid = List(height) { readln() }
        board = Board(width, height, grid)

        players[ME].shackTiles = board.myShackTiles
        players[OPP].shackTiles = board.oppShackTiles
    }

    fun update() {
        // Update inventories
        repeat(2) {
            val (plum, lemon, apple, banana, iron, wood) = readInts()
            players[it].inventory = Inventory(plum, lemon, apple, banana, iron, wood)
        }

        // Update trees
        val treesCount = readInt()
        trees = List(treesCount) {
            val tree = readln().split(" ")
            val type = tree.first()
            val (x, y, size, health, fruits, cooldown) = tree.drop(1).map { it.toInt() }
            Tree(type, board.getTileId(x, y), size, health, fruits, cooldown)
        }

        // Update trolls
        val trollsCount = readInt()
        val trolls = List(trollsCount) {
            val (id, player, x, y, movementSpeed, carryCapacity, harvestPower, chopPower, carryPlum, carryLemon, carryApple, carryBanana, carryIron, carryWood) = readInts()
            Troll(
                id,
                player,
                board.getTileId(x, y),
                movementSpeed,
                carryCapacity,
                harvestPower,
                chopPower,
                carryPlum,
                carryLemon,
                carryApple,
                carryBanana,
                carryIron,
                carryWood
            )
        }
        players.forEach { player ->
            player.trolls = trolls.filter { it.player == player.id }
        }
    }

    fun me() = players[ME]
    fun opp() = players[OPP]

}

data class Player(
    val id: Int,
    var shackTiles: List<Int> = emptyList(),
    var inventory: Inventory = Inventory(),
    var trolls: List<Troll> = emptyList()
)


data class TrainObjective(
    val movementSpeed: Int,
    val carryCapacity: Int,
    val harvestPower: Int,
    val chopPower: Int,
) {
    fun missingResources(inventory: Inventory, trollCount: Int) = Inventory(
        plum = trollCount + movementSpeed * movementSpeed,
        lemon = trollCount + carryCapacity * carryCapacity,
        apple = trollCount + harvestPower * harvestPower,
        banana = 0,
        iron = trollCount + chopPower * chopPower,
        wood = 0,
    ) - inventory

    fun toAction() = "Train $movementSpeed $carryCapacity $harvestPower $chopPower"
}

data class TrainingStep(
    val collectObjective: TrainObjective,
    val trainCandidates: List<TrainObjective>
)

data class Inventory(
    val plum: Int = 0,
    val lemon: Int = 0,
    val apple: Int = 0,
    val banana: Int = 0,
    val iron: Int = 0,
    val wood: Int = 0
) {

    operator fun minus(inventory: Inventory) = Inventory(
        plum = plum - inventory.plum,
        lemon = lemon - inventory.lemon,
        apple = apple - inventory.apple,
        banana = banana - inventory.banana,
        iron = iron - inventory.iron,
        wood = wood - inventory.wood
    )

    fun isSatisfied() = plum <= 0 && lemon <= 0 && apple <= 0 && iron <= 0
    fun fruitsSatisfied() = plum <= 0 && lemon <= 0 && apple <= 0
}

data class Tree(
    val type: String,
    val tileId: Int,
    val size: Int,
    val health: Int,
    val fruits: Int,
    val cooldown: Int,
) {
    fun availableFruitsIn(turns : Int) : Int {
        val cooldownRefresh = if (Game.board.isNextWater[tileId]) {
            TREE_TYPES[type]!!.waterCooldown
        } else {
            TREE_TYPES[type]!!.cooldown
        }

        val futureFruits = fruits + (turns - cooldown) / cooldownRefresh + 1 - (TREE_SIZE_MAX - size)
        return min(3, max(0, futureFruits))
    }
}

data class Troll(
    val id: Int,
    val player: Int,
    val tileId: Int,
    val movementSpeed: Int,
    val carryCapacity: Int,
    val harvestPower: Int,
    val chopPower: Int,
    val carryPlum: Int,
    val carryLemon: Int,
    val carryApple: Int,
    val carryBanana: Int,
    val carryIron: Int,
    val carryWood: Int
) {
    fun carryFruit() = carryPlum > 0 || carryLemon > 0 || carryApple > 0 || carryBanana > 0
    fun carriesFruitType(type: String) = when (type) {
        "PLUM"   -> carryPlum   > 0
        "LEMON"  -> carryLemon  > 0
        "APPLE"  -> carryApple  > 0
        "BANANA" -> carryBanana > 0
        else     -> false
    }
    val totalCarried get() = carryPlum + carryLemon + carryApple + carryBanana + carryIron + carryWood
    fun hasCapacity() = totalCarried < carryCapacity
    fun turnsToReach(distance: Int) = (distance + movementSpeed - 1) / movementSpeed
}