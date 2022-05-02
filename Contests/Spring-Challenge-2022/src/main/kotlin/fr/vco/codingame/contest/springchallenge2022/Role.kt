package fr.vco.codingame.contest.springchallenge2022

enum class RoleType {
    DEFENDER_FARM,
    DEFENDER,
    FARMER,
    ATTACKER,
    FARMER_DEF,
}

data class Role(val type: RoleType, val patrolPath: PatrolPath)
