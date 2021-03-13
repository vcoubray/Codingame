package fr.vco.codingame.contest.code4life

import java.util.*

fun log(message: Any?) = System.err.println(message.toString())

enum class Module { DIAGNOSIS, MOLECULES, SAMPLES, LABORATORY, START_POS }


class Game(val projects: List<Molecules>, input: Scanner) {

    val availableMolecules: Molecules
    val mySamples: List<Sample>
    val oppSamples: List<Sample>
    val cloudSamples: List<Sample>
    val me: Bot
    val opp: Bot

    init {
        val bots = List(2) { Bot(input) }
        me = bots.first()
        opp = bots.last()
        availableMolecules = Molecules(input)

        val sampleCount = input.nextInt()
        val samples = List(sampleCount) { Sample(input) }
        mySamples = samples.filter { it.owner == 0 }
        oppSamples = samples.filter { it.owner == 1 }
        cloudSamples = samples.filter { it.owner == -1 }
    }

    fun currentState(): State =
        if (me.isMoving()) MovingState(this)
        else when (me.module) {
            Module.SAMPLES -> SamplesState(this)
            Module.DIAGNOSIS -> DiagnosisState(this)
            Module.LABORATORY -> LaboratoryState(this)
            Module.MOLECULES -> MoleculesState(this)
            Module.START_POS -> StartState(this)
        }
}


fun main() {
    val input = Scanner(System.`in`)
    val projectCount = input.nextInt()
    val projects = List(projectCount) { Molecules(input) }

    // game loop
    while (true) {
        println(Game(projects, input).currentState().action())
    }
}