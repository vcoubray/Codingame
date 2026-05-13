import java.util.*
import java.io.*
import java.math.*


fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt()
    val height = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    for (i in 0 until height) {
        val line = input.nextLine()
    }

    // game loop
    while (true) {
        for (i in 0 until 2) {
            val plum = input.nextInt()
            val lemon = input.nextInt()
            val apple = input.nextInt()
            val banana = input.nextInt()
            val iron = input.nextInt()
            val wood = input.nextInt()
        }
        val treesCount = input.nextInt()
        for (i in 0 until treesCount) {
            val type = input.next()
            val x = input.nextInt()
            val y = input.nextInt()
            val size = input.nextInt()
            val health = input.nextInt()
            val fruits = input.nextInt()
            val cooldown = input.nextInt()
        }
        val trollsCount = input.nextInt()
        for (i in 0 until trollsCount) {
            val id = input.nextInt()
            val player = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
            val movementSpeed = input.nextInt()
            val carryCapacity = input.nextInt()
            val harvestPower = input.nextInt()
            val chopPower = input.nextInt()
            val carryPlum = input.nextInt()
            val carryLemon = input.nextInt()
            val carryApple = input.nextInt()
            val carryBanana = input.nextInt()
            val carryIron = input.nextInt()
            val carryWood = input.nextInt()
        }

        // Write an action using println()
        // To debug: System.err.println("Debug messages...");


        // valid actions:
        // MOVE <id> <x> <y>
        // HARVEST <id> - when you are on the same cell as a tree
        // DROP <id> - when you are next to your shack and carry items
        println("MOVE 0 7 7")
    }
}