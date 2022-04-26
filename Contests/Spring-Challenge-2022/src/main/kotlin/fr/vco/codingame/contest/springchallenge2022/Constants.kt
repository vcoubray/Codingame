package fr.vco.codingame.contest.springchallenge2022

import kotlin.math.hypot

const val MAX_X = 17630
const val MAX_Y = 9000

const val MONSTER = 0
const val MY_HERO = 1
const val OPP_HERO = 2

const val TARGETING_BASE = 1
const val NO_TARGETING = 0

const val THREAD_NOBODY = 0
const val THREAD_ME = 1
const val THREAD_OPP = 2

const val BASE_VISION = 6000
const val HERO_VISION = 2200

const val HERO_MOVEMENT = 800
const val HERO_ATTACK_RANGE = 800
const val HERO_DAMAGE = 2
const val MONSTER_MOVEMENT = 400
const val MONSTER_ATTACK_RANGE = 300
const val BASE_RANGE = 5000
const val RANGE_CONTROL = 2200
const val RANGE_SHIELD = 2200
const val RANGE_WIND = 1280
val DEFENDER_RANGE = hypot(MAX_X.toDouble(), MAX_Y.toDouble()) / 2
