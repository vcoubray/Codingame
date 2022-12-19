package fr.vco.codingame.constest.fallchallenge2022

enum class Owner {
    ME, OPP, NEUTRAL;

    companion object {
        fun fromInt(value: Int) = when (value) {
            1 -> ME
            0 -> OPP
            -1 -> NEUTRAL
            else -> throw Exception("oh no")
        }
    }
}