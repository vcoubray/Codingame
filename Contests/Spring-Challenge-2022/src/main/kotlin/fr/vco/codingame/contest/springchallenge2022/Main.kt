package fr.vco.codingame.contest.springchallenge2022

import java.util.*

const val MONSTER = 0
const val MY_HERO = 1
const val OPP_HERO = 2

fun Int.square() = this * this

data class Entity(
    val id: Int,
    val type: Int,
    val x: Int,
    val y: Int,
    val shieldLife: Int,
    val isControlled: Int,
    val health: Int,
    val vx: Int,
    val vy: Int,
    val nearBase: Int,
    val threatFor: Int,
    var targeted: Boolean = false,
    var target: Entity? = null,
) {
    fun toHero() = Hero(id, type == MY_HERO, x, y)
    fun toMonster() = Monster(id, x, y, health, vx, vy, nearBase, threatFor)

    fun dist(entity: Entity) = dist(entity.x, entity.y)
    fun dist(x: Int, y: Int) = (this.x - x).square() + (this.y - y).square()
}

data class Hero(
    val id: Int,
    val isGoodGuy: Boolean,
    val x: Int,
    val y: Int,
    var target: Entity? = null
) {

    fun play() {
        val action = target?.let { "MOVE ${it.x} ${it.y}" } ?: "WAIT"
        println(action)
    }
}

data class Monster(
    val id: Int,
    val x: Int,
    val y: Int,
    val health: Int,
    val vx: Int,
    val vy: Int,
    val nearBase: Int,
    val threatFor: Int,
)


fun main() {
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
        val entities = List(entityCount) {
            Entity(
                id = input.nextInt(),
                type = input.nextInt(), // 0=monster, 1=your hero, 2=opponent hero
                x = input.nextInt(),
                y = input.nextInt(),
                shieldLife = input.nextInt(),
                isControlled = input.nextInt(),
                health = input.nextInt(),
                vx = input.nextInt(),
                vy = input.nextInt(),
                nearBase = input.nextInt(), // 0=monster with no target yet, 1=monster targeting a base
                threatFor = input.nextInt(), // Given this monster's trajectory, is it a threat to 1=your base, 2=your opponent's base, 0=neither
            )
        }

        val myHeroes = entities.filter { it.type == MY_HERO }.sortedBy { it.id }.map{it.toHero()}
        val monsters = entities.filter { it.type == MONSTER }

        monsters.filterNot { it.threatFor == 2 }
            .filterNot { it.targeted }
            .sortedWith(compareBy({ -it.threatFor }, { it.dist(baseX, baseY) }))
            .take(myHeroes.size)
            .forEach { monster ->
                myHeroes
                    .filter { it.target == null }
                    .minByOrNull { monster.dist(it.x, it.y) }
                    ?.target = monster.apply { this.targeted = true }
            }

        myHeroes.forEach { it.play() }

    }
}

