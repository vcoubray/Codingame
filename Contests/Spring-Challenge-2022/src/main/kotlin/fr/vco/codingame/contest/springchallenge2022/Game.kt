package fr.vco.codingame.contest.springchallenge2022

import java.util.*

class Game(val myBase: Base, val heroesPerPlayer: Int) {

    val oppBase: Base = Base(Pos(MAX_X - myBase.pos.x, MAX_Y - myBase.pos.y))
    var entities: List<Entity> = emptyList()
    var myHeroes: List<Entity> = emptyList()
    var oppHeroes: List<Entity> = emptyList()
    var monsters: List<Entity> = emptyList()

    val defaultDist = BASE_VISION + HERO_VISION / 2
    val formation = listOf(Role.DEFENDER, Role.DEFENDER, Role.ATTACKER)
    val defaultPaths: List<PatrolPath>

    constructor(input: Scanner) : this(
        myBase = Base(Pos(input.nextInt(), input.nextInt())),
        heroesPerPlayer = input.nextInt()
    )

    init {
        defaultPaths = listOf(
            PatrolPath(
                listOf(
                    Pos(1 / 16.0, defaultDist).withRef(myBase),
                    Pos(3 / 16.0, defaultDist).withRef(myBase)
                )
            ),
            PatrolPath(
                listOf(
                    Pos(7 / 16.0, defaultDist).withRef(myBase),
                    Pos(5 / 16.0, defaultDist).withRef(myBase)
                )
            ),
            PatrolPath(
                listOf(
                    Pos(MAX_X / 2, MAX_Y / 2),
                    Pos(3 / 16.0, defaultDist).withRef(oppBase),
                    Pos(5 / 16.0, defaultDist).withRef(oppBase)
                )
            )
        )
    }

    fun update(input: Scanner) {
        myBase.update(input)
        oppBase.update(input)

        val entityCount = input.nextInt()
        entities = List(entityCount) { Entity(input) }

        myHeroes = entities.filter { it.type == MY_HERO }.sortedBy { it.id }
        oppHeroes = entities.filter { it.type == OPP_HERO }.sortedBy { it.id }
        monsters = entities.filter { it.type == MONSTER }.map { it.apply { calculateThreadLevel(myBase) } }
//            .sortedByDescending { it.threadLevel }

        monsters
            .filter { it.dist(myBase) <= DEFENDER_RANGE }
            .sortedByDescending { it.threadLevel }
            .take(3)
            .forEach { monster ->
                myHeroes.filterIndexed { i, _ -> formation[i] == Role.DEFENDER }
                    .filter { it.target == null }
                    .minByOrNull { monster.dist(it) }
                    ?.target = monster.apply { this.targeted = true }
            }
    }

    fun play() {
        myHeroes.forEachIndexed { i, hero ->
            val role = formation[i]
            val action = when (role) {
                Role.DEFENDER -> playDefender(hero, defaultPaths[i])
                Role.ATTACKER -> playAttacker(hero, defaultPaths[i])
            }
            println("$action $role")
        }
    }


    fun playDefender(hero: Entity, patrolPath: PatrolPath): String {
        return hero.target?.let {
            if (myBase.mana > 30 &&
                it.dist(myBase) < BASE_RANGE &&
                it.shieldLife == 0 &&
                it.dist(hero) < RANGE_WIND
            ) "SPELL WIND ${oppBase.pos}"
            else if (myBase.mana > 30 &&
                it.threatFor != THREAD_OPP &&
                it.health > 15 &&
                it.shieldLife == 0 &&
                it.dist(hero) < RANGE_CONTROL
            ) "SPELL CONTROL ${hero.target?.id} ${oppBase.pos}"
            else "MOVE ${it.nextPos}"
        }
            ?: patrolPath.nextPos(hero.pos)?.let { "MOVE $it" }
            ?: "WAIT"
    }

    fun playAttacker(hero: Entity, patrolPath: PatrolPath): String {
        val target = monsters.filter { it.dist(hero) <= HERO_MOVEMENT }
            .maxByOrNull { monster ->
                monsters.count { monster.nextPos.dist(it.nextPos) < HERO_ATTACK_RANGE }
            }
        return target?.let { "MOVE ${it.nextPos}" }
            ?: monsters
                .filterNot { it.targeted }
                .minByOrNull { it.dist(hero) }?.let {
                    if (myBase.mana > 30 &&
                        it.threatFor != THREAD_OPP &&
                        it.health > 15 &&
                        it.shieldLife == 0 &&
                        it.dist(hero) < RANGE_CONTROL
                    ) {
                        "SPELL CONTROL ${it.id} ${oppBase.pos}"
                    } else {
                        "MOVE ${it.nextPos}"
                    }
                } ?: patrolPath.nextPos(hero.pos)?.let { "MOVE $it" }
            ?: "WAIT"
    }
}