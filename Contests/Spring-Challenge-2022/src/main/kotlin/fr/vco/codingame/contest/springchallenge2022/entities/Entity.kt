package fr.vco.codingame.contest.springchallenge2022.entities

import fr.vco.codingame.contest.springchallenge2022.Hero
import fr.vco.codingame.contest.springchallenge2022.MY_HERO
import fr.vco.codingame.contest.springchallenge2022.Monster
import fr.vco.codingame.contest.springchallenge2022.Pos
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
    fun toMonster() = Monster(id, pos,shieldLife, isControlled, health, dir, nearBase, threatFor)

}