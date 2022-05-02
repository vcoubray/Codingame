package fr.vco.codingame.contest.springchallenge2022

class Formation(val roles: List<Role>)

class Formations {

    private val defDist = BASE_VISION + HERO_VISION / 2
    private val farmDefDist = BASE_VISION + HERO_VISION * 2
    var formations: Map<String, Formation> = emptyMap()

    var paths: MutableMap<String, PatrolPath> = mutableMapOf()

    fun init(myBase: Base, oppBase: Base) {

        paths["defenderPath"] = PatrolPath(
            listOf(
                Pos(2 / 16.0, defDist).withRef(myBase),
                Pos(6 / 16.0, defDist).withRef(myBase)
            )
        )

        paths["defenderClosePath"] = PatrolPath(
            listOf(
                Pos(2 / 16.0, BASE_RANGE).withRef(myBase),
                Pos(6 / 16.0, BASE_RANGE).withRef(myBase)
            )
        )

        paths["farmerNorthPath"] = PatrolPath(
            listOf(
                Pos(BASE_VISION + HERO_VISION / 2, HERO_VISION / 2).withRef(myBase),
                Pos(MAX_X - HERO_VISION / 2, HERO_VISION / 2).withRef(myBase),
                Pos(MAX_X / 2, MAX_Y / 2).withRef(myBase),
            )
        )

        paths["farmerSouthPath"] = PatrolPath(
            listOf(
                Pos(HERO_VISION / 2, MAX_Y - HERO_VISION / 2).withRef(myBase),
                Pos(MAX_X - BASE_RANGE - HERO_VISION / 2, MAX_Y - HERO_VISION / 2).withRef(myBase),
                Pos(MAX_X / 2, MAX_Y / 2).withRef(myBase),
            )
        )

        paths["farmerDefPath"] = PatrolPath(
            listOf(
                Pos(3 / 16.0, farmDefDist).withRef(myBase),
                Pos(5 / 16.0, farmDefDist).withRef(myBase)
            )
        )


        val attackerRange = BASE_RANGE - WIND_RANGE /2
        paths["attackerPath"] = PatrolPath(
            listOf(
                Pos(1 / 16.0, attackerRange).withRef(oppBase),
                Pos(4 / 16.0, attackerRange).withRef(oppBase),
                Pos(7 / 16.0, attackerRange).withRef(oppBase),
                Pos(4 / 16.0, attackerRange).withRef(oppBase),
            )
        )
    }

    fun getFormation(game: Game): Formation {
        val roles = mutableListOf<Role>()
        if (game.oppHeroes.any { it.dist(game.myBase) < BASE_VISION }) {
            roles.add(Role(RoleType.DEFENDER, paths["defenderClosePath"]!!))
            roles.add(Role(RoleType.FARMER_DEF, paths["farmerDefPath"]!!))
        } else {
            roles.add(Role(RoleType.DEFENDER_FARM, paths["defenderPath"]!!))
            roles.add(Role(RoleType.FARMER, paths["farmerNorthPath"]!!))
        }
        if (game.turn < 75) {
            roles.add(Role(RoleType.FARMER, paths["farmerSouthPath"]!!))
        } else {
            roles.add(Role(RoleType.ATTACKER, paths["attackerPath"]!!))
        }
        return Formation(roles)
    }
}
