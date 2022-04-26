package fr.vco.codingame.contest.springchallenge2022

class Player(val game: Game, val formations: Formations) {

    fun play() {
        val formation = formations.getFormation(game)
        game.myHeroes.forEachIndexed { i, hero ->
            val role = formation.roles[i]
            val action = when (role.type) {
                RoleType.DEFENDER -> playDefender(hero, i, role.patrolPath)
                RoleType.ATTACKER -> playAttacker(hero, role.patrolPath)
                RoleType.FARMER -> playFarmer(hero, role.patrolPath)
            }
            println("$action")
        }
    }

    fun playDefender(hero: Entity, i: Int, patrolPath: PatrolPath): Action {
        return game.monsters.filter { it.dist(game.myBase) <= DEFENDER_RANGE }
            .sortedByDescending { it.threadLevel }
            .getOrNull(i)?.let {
                when {
                    game.canKillBeforeReachBase(hero, it) -> Action.Move(it.nextPos)
                    game.canWind(hero, it) -> Action.Wind(game.oppBase.pos)
                    game.canReachToWind(hero, it) -> Action.Move(it.nextPos)
                    game.canControl(hero, it) -> Action.Control(it, game.oppBase.pos)
                    else -> Action.Move(it.nextPos)
                }
            }
            ?: patrolPath.nextPos(hero.pos)?.let { Action.Move(it) }
            ?: Action.Wait
    }

    fun playFarmer(hero: Entity, patrolPath: PatrolPath): Action {
        val target = game.monsters.filter { it.dist(hero) <= HERO_MOVEMENT }
            .maxByOrNull { monster ->
                game.monsters.count { monster.nextPos.dist(it.nextPos) < HERO_ATTACK_RANGE }
            } ?: game.monsters.filter { it.dist(game.myBase) > DEFENDER_RANGE }.minByOrNull { it.dist(hero) }
        return target?.let { Action.Move(it.nextPos) }
            ?: patrolPath.nextPos(hero.pos)?.let { Action.Move(it) }
            ?: Action.Wait
    }

    fun playAttacker(hero: Entity, patrolPath: PatrolPath): Action {
        val action = game.monsters.firstOrNull {
            game.myBase.mana > 10 &&
                it.nearBase &&
                it.threatFor == THREAD_OPP &&
                it.shieldLife == 0 &&
                it.dist(hero) <= RANGE_SHIELD
        }?.let { Action.Shield(it) }
            ?: game.monsters.firstOrNull {
                game.myBase.mana > 10 &&
                    it.dist(game.oppBase) <= BASE_RANGE + 2000 &&
                    it.health > 15 &&
                    it.shieldLife == 0 &&
                    it.dist(hero) <= RANGE_WIND
            }?.let { Action.Wind(game.oppBase.pos) }
            ?: game.monsters.firstOrNull {
                game.myBase.mana > 10 &&
                    it.dist(game.oppBase) <= BASE_RANGE + 2000 &&
                    it.threatFor != THREAD_OPP &&
                    it.shieldLife == 0 &&
                    it.dist(hero) <= RANGE_CONTROL
            }?.let { Action.Control(it, game.oppBase.pos) }
            ?: game.monsters.firstOrNull {
                it.dist(game.oppBase) <= BASE_RANGE + 2000 &&
                it.threatFor != THREAD_OPP
            }?.let { Action.Move(it.nextPos) }
        return if (action == null && hero.shieldLife == 0) Action.Shield(hero)
        else action ?: patrolPath.nextPos(hero.pos)?.let { Action.Move(it) }
        ?: Action.Wait
    }
}