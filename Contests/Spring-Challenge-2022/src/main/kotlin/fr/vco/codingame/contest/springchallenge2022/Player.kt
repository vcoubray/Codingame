package fr.vco.codingame.contest.springchallenge2022

import kotlin.math.min

class Player(val game: Game, val formations: Formations) {

    private val targeted = mutableListOf<Int>()
    private val winded = mutableListOf<Int>()

    private val controlPosDest = PatrolPath(
        listOf(
            Pos(3 / 16.0, BASE_VISION).withRef(game.oppBase),
            Pos(1 / 4.0, BASE_RANGE).withRef(game.oppBase),
            Pos(5 / 16.0, BASE_VISION).withRef(game.oppBase)
        )
    )

    fun play() {
        val formation = formations.getFormation(game)
        targeted.clear()
        game.myHeroes.forEachIndexed { i, hero ->
            val role = formation.roles[i]
            val action = when (role.type) {
                RoleType.DEFENDER -> playDefender(hero)
                RoleType.DEFENDER_FARM -> playDefenderFarm(hero)
                RoleType.ATTACKER -> playAttacker(hero)
                RoleType.FARMER -> playFarmer(hero)
                RoleType.FARMER_DEF -> playFarmerDef(hero)
            }
                ?: role.patrolPath.nextPos(hero.pos)?.let { Action.Move(it) }
                ?: Action.Wait
            game.myBase.mana -= action.manaCost()
            println("$action")
//            println("$action ${role.type}")
        }
    }

    private fun Entity.canControl(target: Entity): Boolean {
        return game.canSpell() &&
            target.shieldLife == 0 &&
            this.dist(target) <= CONTROL_RANGE
    }

    private fun Entity.canShield(target: Entity): Boolean {
        return game.canSpell() &&
            target.shieldLife == 0 &&
            this.dist(target) <= SHIELD_RANGE
    }

    private fun Entity.canWind(target: Entity): Boolean {
        return game.canSpell() &&
            target.shieldLife == 0 &&
            this.dist(target) <= WIND_RANGE
    }

    private fun Entity.canKillBeforeReachBase(target: Entity, base: Base = game.myBase): Boolean {
        val timeToKill = target.health / HERO_DAMAGE
        return target.timeToReach(base) > timeToKill + this.timeToReach(target, HERO_ATTACK_RANGE)
    }

    private fun Entity.canReachToWind(target: Entity): Boolean {
        val timeToReach = this.timeToReach(target, WIND_RANGE)
        return timeToReach < target.timeToReach(game.myBase) &&
            timeToReach >= target.shieldLife
    }

    private fun List<Entity>.nearest(pos: Pos) = this.minByOrNull { it.dist(pos) }
    private fun List<Entity>.nearest(base: Base) = this.nearest(base.pos)
    private fun List<Entity>.nearest(entity: Entity) = this.nearest(entity.pos)

    private fun playDefender(hero: Entity): Action? {

        val possibleTargets = game.monsters.filter { it.dist(game.myBase) <= BASE_VISION }
            .filterNot { it.id in targeted }
        return defautltDef(hero, possibleTargets)

    }


    private fun playDefenderFarm(hero: Entity): Action? {
        val possibleTargets = game.monsters.filter { it.dist(game.myBase) <= DEFENDER_RANGE }
            .filterNot { it.id in targeted }
        return defautltDef(hero, possibleTargets)

    }

    private fun playFarmer(hero: Entity): Action? {
        val possibleTargets = game.monsters
            .filterNot { it.id in targeted }
            .filter { it.dist(game.oppBase) > BASE_VISION }

        val target = possibleTargets.filter { it.dist(hero) <= HERO_MOVEMENT }
            .maxByOrNull { monster -> game.monsters.count { monster.nextPos.dist(it.nextPos) < HERO_ATTACK_RANGE } }
            ?: possibleTargets.nearest(hero)

        return target?.let {
            targeted.add(it.id)
            Action.Move(it.nextPos)
        }
    }


    private fun defautltDef(hero: Entity, targets: List<Entity>): Action? {
        log(hero.isControlled)
        if(game.oppUseControl && hero.canShield(hero)) return Action.Shield(hero)
        return targets.maxByOrNull { it.threadLevel }
            ?.let { monster ->
                targeted.add(monster.id)
                when {
                    hero.canWind(monster) &&
                        monster.dist(game.myBase) < BASE_VISION &&
                        monster.id !in winded &&
                        game.oppHeroes.any { it.canWind(monster) } -> {
                        game.monsters.filter { it.dist(hero) <= WIND_RANGE }.forEach { winded.add(it.id) }
                        Action.Wind(game.oppBase.pos)
                    }
                    hero.canKillBeforeReachBase(monster) -> Action.Move(monster.nextPos)
                    hero.canWind(monster) &&
                        monster.id !in winded &&
                        monster.dist(game.myBase) < BASE_RANGE -> {
                        game.monsters.filter { it.dist(hero) <= WIND_RANGE }.forEach { winded.add(it.id) }
                        Action.Wind(game.oppBase.pos)
                    }
                    hero.canReachToWind(monster) -> Action.Move(monster.nextPos)
                    hero.canControl(monster) -> Action.Control(monster, controlPosDest.nextPos() ?: game.oppBase.pos)
                    else -> Action.Move(monster.nextPos)
                }
            }
    }

    private fun playFarmerDef(hero: Entity): Action? {

        val possibleTargets = game.monsters
            .filterNot { it.id in targeted }
            .filter { it.dist(game.myBase) < DEFENDER_RANGE }

        val action = game.oppHeroes.nearest(game.myBase)?.let { oppHero ->
            possibleTargets.nearest(oppHero)?.let { monster ->
                targeted.add(monster.id)
                if (oppHero.dist(monster) < WIND_RANGE && oppHero.dist(game.myBase) < BASE_VISION + WIND_RANGE) {
                    if (hero.canControl(oppHero)) {
                        Action.Control(oppHero, game.oppBase.pos)
                    } else {
                        Action.Move(monster.nextPos)
                    }
                } else {
                    Action.Move(monster.nextPos)
                }
            }
        }

        if (action != null) return action
        val target = possibleTargets.filter { it.dist(hero) <= HERO_MOVEMENT }
            .maxByOrNull { monster -> game.monsters.count { monster.nextPos.dist(it.nextPos) < HERO_ATTACK_RANGE } }
            ?: possibleTargets.minByOrNull { it.dist(hero) }

        return target?.let {
            targeted.add(it.id)
            Action.Move(it.nextPos)
        }
    }

    private fun playAttacker(hero: Entity): Action? {
        val possibleTarget = game.monsters.filter {
            it.dist(game.oppBase) <= BASE_RANGE + WIND_ATTACK_RANGE
                && it.shieldLife == 0
        }
        return possibleTarget
            .filter {
                hero.canWind(it) &&
                    (game.oppHeroes.none { opp -> hero.dist(opp) <= WIND_RANGE } ||
                        game.oppHeroes.none { opp -> opp.canWind(it) } ||
                        it.dist(game.oppBase) - MONSTER_ATTACK_RANGE < WIND_ATTACK_RANGE) &&
                    it.health > 2
            }.minByOrNull { it.dist(game.oppBase) }
            ?.let {
                targeted.add(it.id)
                Action.Wind(hero.pos + it.pos.dir(game.oppBase.pos, WIND_ATTACK_RANGE))
            } ?: possibleTarget.filter {
            hero.canShield(it) &&
                it.dist(game.oppBase) < BASE_RANGE / 2 &&
                it.health > 15 &&
                game.oppHeroes.none { opp -> opp.canKillBeforeReachBase(it, game.oppBase) }
        }.maxByOrNull { it.health }
            ?.let {
                targeted.add(it.id)
                Action.Shield(it)
            } ?: possibleTarget.associateWith {
            game.oppHeroes.filter { opp -> opp.dist(game.oppBase) < BASE_VISION + WIND_RANGE }
                .minByOrNull { opp -> opp.dist(it) }
        }
            .maxByOrNull { (monster, oppHero) -> oppHero?.dist(monster.nextPos) ?: Int.MAX_VALUE }
            ?.let { (target, oppHero) ->
                targeted.add(target.id)
                if (oppHero != null) {
                    val oppHeroNextPos = oppHero.pos + oppHero.pos.dir(target.nextPos, HERO_MOVEMENT)
                    val oppHeroDist = oppHeroNextPos.dist(target.nextPos)
                    val safeDistance = min(HERO_ATTACK_RANGE + 1, -oppHeroDist + 1)
                    val nextPos = target.nextPos + target.nextPos.dir(oppHeroNextPos, -(HERO_ATTACK_RANGE + 1))
                    Action.Move(nextPos)
                } else Action.Move(target.nextPos)
            }
    }


}