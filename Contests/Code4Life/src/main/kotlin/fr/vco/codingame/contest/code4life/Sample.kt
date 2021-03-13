package fr.vco.codingame.contest.code4life

import java.util.*

data class Sample(
    val id: Int,
    val owner: Int,
    val rank: Int,
    val gain: String,
    val health: Int,
    val costs: Molecules
) {

    constructor(input: Scanner) : this(
        id = input.nextInt(),
        owner = input.nextInt(),
        rank = input.nextInt(),
        gain = input.next(),
        health = input.nextInt(),
        costs = Molecules(input)
    )

    fun isDiagnosed() = health != -1

    override fun equals(other: Any?): Boolean {
        return if (other is Sample) this.id == other.id
        else false
    }

    override fun hashCode(): Int {
        return id
    }
}


class SampleGroup(private val bot: Bot, private val samples: List<Sample>) {

    val cost = cost()
    val health =  samples.sumBy { it.health }
    val gain = samples.fold(emptyMolecules) { acc, s -> acc.add(s.gain, 1) }
    val rentability = health.toFloat() / cost.sum()

    private fun cost(): Molecules {
        var expertise = bot.expertise
        var totalCost = emptyMolecules
        samples.forEach { s ->
            totalCost += (s.costs - expertise).filterPositive()
            expertise = expertise.add(s.gain, 1)
        }
        return (totalCost - bot.storage).filterPositive()
    }

}

fun List<Sample>.getAllCombinations(bot: Bot, combination: List<Sample> = emptyList(), depth: Int = 3): List<SampleGroup> {
    val combinations = mutableListOf<SampleGroup>()
    if (combination.size >= depth) return combinations
    this.forEach {
        if (!combination.contains(it)) {
            val newCombi = combination + it
            combinations.add(SampleGroup(bot,newCombi))
            combinations.addAll(getAllCombinations(bot,newCombi, depth))
        }

    }
    return combinations
}
