package fr.vco.codingame.contest.springchallenge2022

import java.util.*

const val MAX_X = 17630
const val MAX_Y = 9000

const val MONSTER = 0
const val MY_HERO = 1
const val OPP_HERO = 2

const val TARGETING_BASE = 1
const val NO_TARGETING = 0

const val THREAD_NOBODY = 0
const val THREAD_ME = 1
const val THREAD_OPP = 2

const val RANGE_CONTROL = 2200 * 2200
const val RANGE_SHIELD = 2200 * 2200
const val RANGE_WIND = 1280 * 1280

fun log(message: Any?) = System.err.println(message)

fun Int.square() = this * this

data class Pos(val x: Int, val y: Int) {
    operator fun plus(pos: Pos) = Pos(x + pos.x, y + pos.y)
    operator fun minus(pos: Pos) = Pos(x - pos.x, y - pos.y)
    fun dist(pos: Pos) = (pos.x - x).square() + (pos.y - y).square()
    override fun toString() = "$x $y"

}

data class Base(val pos: Pos) {
    var health: Int = 0
    var mana: Int = 0

    fun update(input: Scanner) {
        health = input.nextInt()
        mana = input.nextInt()
    }

    fun fromBase(pos: Pos): Pos {
        return if (this.pos.x == 0) pos
        else this.pos - pos
    }
}

data class Entity(
    val id: Int,
    val type: Int,
    val pos: Pos,
    val shieldLife: Int,
    val isControlled: Boolean,
    val health: Int,
    val dir: Pos,
    val nearBase: Int,
    val threatFor: Int,
    var targeted: Boolean = false,
    var target: Entity? = null,
) {

    constructor(input: Scanner) : this(
        id = input.nextInt(),
        type = input.nextInt(),
        pos = Pos(
            x = input.nextInt(),
            y = input.nextInt()
        ),
        shieldLife = input.nextInt(),
        isControlled = input.nextInt() == 1,
        health = input.nextInt(),
        dir = Pos(
            x = input.nextInt(),
            y = input.nextInt()
        ),
        nearBase = input.nextInt(),
        threatFor = input.nextInt(),
    )

    fun toHero() = Hero(id, type == MY_HERO, pos)
    fun toMonster() = Monster(id, pos, health, dir, nearBase, threatFor)

}

data class Hero(
    val id: Int,
    val isGoodGuy: Boolean,
    val pos: Pos,
    var target: Entity? = null,
    var defaultPos: Pos? = null,
) {

    fun play(mana: Int, oppBase: Base) {
        val action = target?.let {
            if (mana > 30 &&
                it.health >10 &&
                it.shieldLife == 0 &&
                it.pos.dist(pos) < RANGE_CONTROL
            ) "SPELL CONTROL ${target?.id} ${oppBase.pos}"
            else "MOVE ${it.pos + it.dir}"
        }
            ?: defaultPos?.let { "MOVE $it" }
            ?: "WAIT"
        println(action)
    }
}

data class Monster(
    val id: Int,
    val pos: Pos,
    val health: Int,
    val dir: Pos,
    val nearBase: Int,
    val threatFor: Int,
)


fun main() {
    val input = Scanner(System.`in`)

    val myBase = Base(Pos(input.nextInt(), input.nextInt()))
    val oppBase = Base(Pos(MAX_X - myBase.pos.x, MAX_Y - myBase.pos.y))

    val heroesPerPlayer = input.nextInt() // Always 3

    val defaultPos = listOf(
        myBase.fromBase(Pos(7000, 1750)),
        myBase.fromBase(Pos(3500, 6500)),
        myBase.fromBase(Pos(MAX_X / 2, MAX_Y / 2))
    )

    // game loop
    while (true) {
        myBase.update(input)
        oppBase.update(input)

        val entityCount = input.nextInt()
        val entities = List(entityCount) { Entity(input) }

        val myHeroes = entities
            .filter { it.type == MY_HERO }
            .sortedBy { it.id }
            .mapIndexed { i, it -> it.toHero().apply { this.defaultPos = defaultPos[i] } }
        val monsters = entities.filter { it.type == MONSTER }

        monsters.filterNot { it.threatFor == THREAD_OPP }
            .filterNot { it.targeted }
            .sortedWith(compareBy({ -it.threatFor }, { it.pos.dist(myBase.pos) }))
            .take(myHeroes.size)
            .forEach { monster ->
                myHeroes
                    .filter { it.target == null }
                    .minByOrNull { monster.pos.dist(it.pos) }
                    ?.target = monster.apply { this.targeted = true }
            }

        myHeroes.forEach { it.play(myBase.mana, oppBase) }

    }
}

