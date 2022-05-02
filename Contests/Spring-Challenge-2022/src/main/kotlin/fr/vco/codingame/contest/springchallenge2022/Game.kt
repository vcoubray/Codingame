package fr.vco.codingame.contest.springchallenge2022

import java.util.*

class Game(val myBase: Base, val heroesPerPlayer: Int) {
    var turn = 0

    val oppBase: Base = Base(Pos(MAX_X - myBase.pos.x, MAX_Y - myBase.pos.y))
    var entities: List<Entity> = emptyList()
    var myHeroes: List<Entity> = emptyList()
    var oppHeroes: List<Entity> = emptyList()
    var monsters: List<Entity> = emptyList()

    var oppUseControl: Boolean = false

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
        monsters = entities.filter { it.type == MONSTER }.map {
            it.apply {
                it.threadLevel = calculateThreadLevel(THREAD_ME, myBase)
                it.oppThreadLevel = calculateThreadLevel(THREAD_OPP, oppBase)
            }
        }

        if (!oppUseControl) {
            oppUseControl = myHeroes.any { it.isControlled }
        }
    }

    fun canSpell() = myBase.mana >= SPELL_COST
}
