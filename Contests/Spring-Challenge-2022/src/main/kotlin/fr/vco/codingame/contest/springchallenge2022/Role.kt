package fr.vco.codingame.contest.springchallenge2022

enum class RoleType {
    DEFENDER,
    FARMER,
    ATTACKER
}

data class Role(val type: RoleType, val patrolPath: PatrolPath)

