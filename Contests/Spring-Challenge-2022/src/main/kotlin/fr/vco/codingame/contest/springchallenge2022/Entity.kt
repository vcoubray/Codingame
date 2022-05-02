package fr.vco.codingame.contest.springchallenge2022

import java.util.*

data class Entity(
    val id: Int,
    val type: Int,
    val pos: Pos,
    val shieldLife: Int,
    val isControlled: Boolean,
    val health: Int,
    val dir: Pos,
    val nearBase: Boolean,
    val threatFor: Int,
) {
    val nextPos = pos + dir
    var threadLevel: Double = 0.0
    var oppThreadLevel: Double = 0.0

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
        nearBase = input.nextInt() == 1,
        threatFor = input.nextInt(),
    )

    fun dist(entity: Entity) = pos.dist(entity.pos)
    fun dist(base: Base) = pos.dist(base.pos)
    fun dist(pos: Pos) = this.pos.dist(pos)

    fun calculateThreadLevel(threatFor: Int, base: Base) : Double {
        return when (this.threatFor) {
            threatFor -> 1000
            THREAD_NOBODY -> 500
            else -> 0
        } + 500 * 1.0 / (base.pos.dist(pos) + 1)
    }

    fun timeToReach(base: Base): Int {
        return ((dist(base) - MONSTER_ATTACK_RANGE) / MONSTER_MOVEMENT)
    }

    fun timeToReach(entity: Entity, attackRange: Int): Int {
        return (dist(entity) - attackRange) / (HERO_MOVEMENT - MONSTER_MOVEMENT)
    }
}