package fr.vco.codingame.contest.springchallenge2021

const val INITIAL_NUTRIENTS = 20
const val MAX_DAY = 24

const val ME = 0
const val OPP = 1
const val DRAW = 2
const val IN_PROGRESS = 3

const val SEED_ACTION = 0
const val GROW_0_ACTION = 1
const val GROW_1_ACTION = 2
const val GROW_2_ACTION = 3
const val COMPLETE_ACTION = 4

val GROW_ACTION = listOf(GROW_0_ACTION,GROW_1_ACTION,GROW_2_ACTION)

const val SEED_COST = 0
const val GROW_0_COST = 1
const val GROW_1_COST = 3
const val GROW_2_COST = 7
const val COMPLETE_COST = 4

val BASE_COST = listOf(SEED_COST,GROW_0_COST, GROW_1_COST, GROW_2_COST, COMPLETE_COST)

const val NONE = -1
const val SEED = 0
const val LITTLE = 1
const val MEDIUM = 2
const val GREAT = 3

val BONUS_RICHNESS = listOf(0, 0, 2, 4)
