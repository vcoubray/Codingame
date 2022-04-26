package fr.vco.codingame.contest.springchallenge2022

import java.util.*

class Game(val myBase: Base, val heroesPerPlayer: Int) {
    var turn = 0

    val oppBase: Base = Base(Pos(MAX_X - myBase.pos.x, MAX_Y - myBase.pos.y))
    var entities: List<Entity> = emptyList()
    var myHeroes: List<Entity> = emptyList()
    var oppHeroes: List<Entity> = emptyList()
    var monsters: List<Entity> = emptyList()

    constructor(input: Scanner) : this(
        myBase = Base(Pos(input.nextInt(), input.nextInt())),
        heroesPerPlayer = input.nextInt()
    )

    fun update(input: Scanner) {
        turn++
        myBase.update(input)
        oppBase.update(input)

        val entityCount = input.nextInt()
        entities = List(entityCount) { Entity(input) }
        myHeroes = entities.filter { it.type == MY_HERO }.sortedBy { it.id }
        oppHeroes = entities.filter { it.type == OPP_HERO }.sortedBy { it.id }
        monsters = entities.filter { it.type == MONSTER }.map { it.apply { calculateThreadLevel(myBase) } }
//            .sortedByDescending { it.threadLevel }
    }

    fun canWind(hero: Entity, target: Entity) = myBase.mana > 30 &&
        target.dist(myBase) < BASE_RANGE &&
        target.shieldLife == 0 &&
        target.dist(hero) < RANGE_WIND

    fun canReachToWind(hero: Entity, target: Entity): Boolean {
        val timeToReach = hero.timeToReach(target, RANGE_WIND)
        return timeToReach < target.timeToReach(myBase) &&
            timeToReach >= target.shieldLife
    }

    fun canKillBeforeReachBase(hero: Entity, target: Entity): Boolean {
        val timeToKill = target.health / HERO_DAMAGE
        return target.timeToReach(myBase) > timeToKill + hero.timeToReach(target, HERO_ATTACK_RANGE)
    }

    fun canControl(hero: Entity, target: Entity): Boolean {
        return myBase.mana > 30 &&
            target.threatFor != THREAD_OPP &&
            target.health > 15 &&
            target.shieldLife == 0 &&
            target.dist(hero) < RANGE_CONTROL
    }
}
