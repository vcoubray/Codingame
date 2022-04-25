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
    val nearBase: Int,
    val threatFor: Int,
) {
    val nextPos = pos + dir
    var targeted: Boolean = false
    var target: Entity? = null
    var threadLevel: Double = 0.0


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

    fun dist(entity: Entity) = pos.dist(entity.pos)
    fun dist(base: Base) = pos.dist(base.pos)
    fun dist(pos: Pos) = this.pos.dist(pos)


    fun calculateThreadLevel(base: Base) {
        threadLevel = when (threatFor) {
            THREAD_ME -> 1000
            THREAD_NOBODY -> 500
            else -> 0
        } + 500 * 1.0 / (base.pos.dist(pos) + 1)
    }


}