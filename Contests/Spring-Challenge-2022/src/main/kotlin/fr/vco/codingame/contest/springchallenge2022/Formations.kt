package fr.vco.codingame.contest.springchallenge2022

class Formation(val roles: List<Role>)

class Formations {

    private val defaultDist = BASE_VISION + HERO_VISION / 2
    var formations: List<Formation> = emptyList()

    fun init(myBase: Base, oppBase: Base) {

        val defender1Path = PatrolPath(
            listOf(
                Pos(3 / 16.0, defaultDist).withRef(myBase),
                Pos(1 / 16.0, defaultDist).withRef(myBase)
            )
        )
        val defender2Path = PatrolPath(
            listOf(
                Pos(5 / 16.0, defaultDist).withRef(myBase),
                Pos(7 / 16.0, defaultDist).withRef(myBase),
            )
        )
        val farmerPath = PatrolPath(
            listOf(
                Pos(MAX_X / 2, MAX_Y / 2),
                Pos(3 / 16.0, defaultDist).withRef(oppBase),
                Pos(5 / 16.0, defaultDist).withRef(oppBase)
            )
        )

        val attackerPath = PatrolPath(
            listOf(
                Pos(1 / 4.0, BASE_RANGE).withRef(oppBase),
                Pos(3 / 16.0, BASE_VISION).withRef(oppBase),
                Pos(5 / 16.0, BASE_VISION).withRef(oppBase)
            )
        )

        formations = listOf(
            Formation(
                listOf(
                    Role(RoleType.DEFENDER, defender1Path),
                    Role(RoleType.DEFENDER, defender2Path),
                    Role(RoleType.FARMER, farmerPath)
                )
            ),
            Formation(
                listOf(
                    Role(RoleType.DEFENDER, defender1Path),
                    Role(RoleType.DEFENDER, defender2Path),
                    Role(RoleType.ATTACKER, attackerPath)
                )
            )
        )

    }

    fun getFormation(game: Game): Formation {
        return if (game.turn < 75) formations.first()
        else formations.last()
    }
}
