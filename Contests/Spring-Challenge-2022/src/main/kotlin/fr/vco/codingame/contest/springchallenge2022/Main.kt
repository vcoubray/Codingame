package fr.vco.codingame.contest.springchallenge2022

import fr.vco.codingame.contest.springchallenge2022.entities.Entity
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val MAX_X = 17630
const val MAX_Y = 9000

const val BASE_VISION = 6000
const val HERO_VISION = 2200

const val MONSTER = 0
const val MY_HERO = 1
const val OPP_HERO = 2

const val TARGETING_BASE = 1
const val NO_TARGETING = 0

const val THREAD_NOBODY = 0
const val THREAD_ME = 1
const val THREAD_OPP = 2

const val BASE_RANGE = 5000 * 5000
const val RANGE_CONTROL = 2200 * 2200
const val RANGE_SHIELD = 2200 * 2200
const val RANGE_WIND = 1280 * 1280

fun log(message: Any?) = System.err.println(message)

enum class Role {
    DEFENDER,
    ATTACKER
}

fun Int.square() = this * this

data class Pos(val x: Int, val y: Int) {
    operator fun plus(pos: Pos) = Pos(x + pos.x, y + pos.y)
    operator fun minus(pos: Pos) = Pos(x - pos.x, y - pos.y)
    fun dist(pos: Pos) = (pos.x - x).square() + (pos.y - y).square()
    override fun toString() = "$x $y"

}

data class PatrolPath(val path: List<Pos>) {
    var currentPos = 0

    fun nextPos(pos: Pos): Pos {
        if (path[currentPos] == pos) {
            currentPos = (currentPos + 1) % path.size
        }
        return path[currentPos]
    }
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


class Game {
    lateinit var myBase: Base
    lateinit var oppBase: Base
    var entities: List<Entity> = emptyList()
    var myHeroes: List<Hero> = emptyList()
    var monsters: List<Monster> = emptyList()

    val defaultDist = BASE_VISION + HERO_VISION / 2

    val formation = listOf(Role.DEFENDER, Role.DEFENDER, Role.ATTACKER)

    lateinit var defaultPaths: List<PatrolPath>

    fun init(input: Scanner) {
        myBase = Base(Pos(input.nextInt(), input.nextInt()))
        oppBase = Base(Pos(MAX_X - myBase.pos.x, MAX_Y - myBase.pos.y))
        val heroesPerPlayer = input.nextInt()
        defaultPaths = listOf(
            PatrolPath(
                listOf(
                    myBase.fromBase(Pos((cos(PI / 16) * defaultDist).toInt(), (sin(PI / 16) * defaultDist).toInt())),
                    myBase.fromBase(
                        Pos(
                            (cos(3 * PI / 16) * defaultDist).toInt(),
                            (sin(3 * PI / 16) * defaultDist).toInt()
                        )
                    )
                )
            ),
            PatrolPath(
                listOf(
                    myBase.fromBase(
                        Pos(
                            (cos(7 * PI / 16) * defaultDist).toInt(),
                            (sin(7 * PI / 16) * defaultDist).toInt()
                        )
                    ),
                    myBase.fromBase(
                        Pos(
                            (cos(5 * PI / 16) * defaultDist).toInt(),
                            (sin(5 * PI / 16) * defaultDist).toInt()
                        )
                    )
                )
            ),
            PatrolPath(
                listOf(
                    myBase.fromBase(Pos(MAX_X / 2, MAX_Y / 2))
                )
            )
        )
    }

    fun update(input: Scanner) {
        myBase.update(input)
        oppBase.update(input)

        val entityCount = input.nextInt()
        entities = List(entityCount) { Entity(input) }

        myHeroes = entities
            .filter { it.type == MY_HERO }
            .sortedBy { it.id }
            .mapIndexed { i, it ->
                it.toHero().apply {
                    this.role = formation[i]
                    this.patrolPath = defaultPaths[i]
                }
            }
        monsters = entities
            .filter { it.type == MONSTER }
            .map { it.toMonster().apply { calculateThreadLevel(myBase) } }
            .sortedByDescending { it.threadLevel }

        monsters.take(3).forEach { monster ->
            myHeroes.filter { it.role == Role.DEFENDER }
                .filter { it.target == null }
                .minByOrNull { monster.pos.dist(it.pos) }
                ?.target = monster.apply { this.targeted = true }
        }
    }

    fun play() {
        myHeroes.forEach { it.play(this) }
    }

}

data class Hero(
    val id: Int,
    val isGoodGuy: Boolean,
    val pos: Pos,
    var role: Role = Role.DEFENDER,
    var target: Monster? = null,
    var patrolPath: PatrolPath? = null,
) {

    fun play(game: Game) {

        val action = when (role) {
            Role.DEFENDER -> playDefender(game)
            Role.ATTACKER -> playAttacker(game)
        }

        println("$action $role")
    }


    fun playDefender(game: Game): String {
        return target?.let {
            if (game.myBase.mana > 30 &&
                it.pos.dist(game.myBase.pos) < BASE_RANGE &&
                it.shieldLife == 0 &&
                it.pos.dist(pos) < RANGE_WIND
            ) "SPELL WIND ${game.oppBase.pos}"
//            else if (mana > 30 &&
//                it.threatFor != THREAD_OPP &&
//                it.health > 15 &&
//                it.shieldLife == 0 &&
//                it.pos.dist(pos) < RANGE_CONTROL
//            ) "SPELL CONTROL ${target?.id} ${oppBase.pos}"
            else "MOVE ${it.pos + it.dir}"
        }
            ?: patrolPath?.nextPos(pos)?.let { "MOVE $it" }
            ?: "WAIT"
    }

    fun playAttacker(game: Game): String {
        return game.monsters
            .filterNot { it.targeted }
            .minByOrNull { it.pos.dist(this.pos) }?.let {
                if (game.myBase.mana > 30 &&
                    it.threatFor != THREAD_OPP &&
                    it.health > 15 &&
                    it.shieldLife == 0 &&
                    it.pos.dist(pos) < RANGE_CONTROL
                ) {
                    "SPELL CONTROL ${it.id} ${game.oppBase.pos}"
                } else {
                    "MOVE ${it.pos + it.dir}"
                }
            } ?: patrolPath?.nextPos(pos)?.let { "MOVE $it" }
        ?: "WAIT"
    }
}

data class Monster(
    val id: Int,
    val pos: Pos,
    val shieldLife: Int,
    val isControlled: Boolean,
    val health: Int,
    val dir: Pos,
    val nearBase: Int,
    val threatFor: Int,

    ) {
    var targeted: Boolean = false
    var threadLevel: Double = 0.0

    fun calculateThreadLevel(base: Base) {
        threadLevel = when (threatFor) {
            THREAD_ME -> 1000
            THREAD_NOBODY -> 500
            else -> 0
        } + 500 * 1.0 / (base.pos.dist(pos) + 1)
    }
}


fun main() {
    val input = Scanner(System.`in`)

    val game = Game()
    game.init(input)

    log("after init")

    // game loop
    while (true) {
        game.update(input)
        log("after update")
        game.play()
    }
}

