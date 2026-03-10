import java.util.*

fun main() {
    val input = Scanner(System.`in`)
    val myId = input.nextInt()
    val width = input.nextInt()
    val height = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    for (i in 0 until height) {
        val row = input.nextLine()
    }
    val snakebotsPerPlayer = input.nextInt()
    for (i in 0 until snakebotsPerPlayer) {
        val mySnakebotId = input.nextInt()
    }
    for (i in 0 until snakebotsPerPlayer) {
        val oppSnakebotId = input.nextInt()
    }

    // game loop
    while (true) {
        val powerSourceCount = input.nextInt()
        for (i in 0 until powerSourceCount) {
            val x = input.nextInt()
            val y = input.nextInt()
        }
        val snakebotCount = input.nextInt()
        for (i in 0 until snakebotCount) {
            val snakebotId = input.nextInt()
            val body = input.next()
        }

        // Write an action using println()
        // To debug: System.err.println("Debug messages...");

        println("WAIT")
    }
}