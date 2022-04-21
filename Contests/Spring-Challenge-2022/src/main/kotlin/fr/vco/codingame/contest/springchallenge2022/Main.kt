package fr.vco.codingame.contest.springchallenge2022

import java.util.*
import java.io.*
import java.math.*

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main(args : Array<String>) {
    val input = Scanner(System.`in`)
    val baseX = input.nextInt() // The corner of the map representing your base
    val baseY = input.nextInt()
    val heroesPerPlayer = input.nextInt() // Always 3

    // game loop
    while (true) {
        for (i in 0 until 2) {
            val health = input.nextInt() // Your base health
            val mana = input.nextInt() // Ignore in the first league; Spend ten mana to cast a spell
        }
        val entityCount = input.nextInt() // Amount of heros and monsters you can see
        for (i in 0 until entityCount) {
            val id = input.nextInt() // Unique identifier
            val type = input.nextInt() // 0=monster, 1=your hero, 2=opponent hero
            val x = input.nextInt() // Position of this entity
            val y = input.nextInt()
            val shieldLife = input.nextInt() // Ignore for this league; Count down until shield spell fades
            val isControlled = input.nextInt() // Ignore for this league; Equals 1 when this entity is under a control spell
            val health = input.nextInt() // Remaining health of this monster
            val vx = input.nextInt() // Trajectory of this monster
            val vy = input.nextInt()
            val nearBase = input.nextInt() // 0=monster with no target yet, 1=monster targeting a base
            val threatFor = input.nextInt() // Given this monster's trajectory, is it a threat to 1=your base, 2=your opponent's base, 0=neither
        }
        for (i in 0 until heroesPerPlayer) {

            // Write an action using println()
            // To debug: System.err.println("Debug messages...");


            // In the first league: MOVE <x> <y> | WAIT; In later leagues: | SPELL <spellParams>;
            println("WAIT")
        }
    }
}