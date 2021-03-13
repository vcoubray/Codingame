package fr.vco.codingame.contest.code4life

import java.util.*

data class Bot(
    val module: Module,
    val eta: Int,
    val score: Int,
    val storage: Molecules,
    val expertise: Molecules
) {
    constructor(input: Scanner) : this(
        module = Module.valueOf(input.next()),
        eta = input.nextInt(),
        score = input.nextInt(),
        storage = Molecules(input),
        expertise = Molecules(input)
    )

    fun canProduce(sample: Sample): Boolean {
        return storage.contains(realCosts(sample))
    }

    fun need(sample: Sample): Molecules {
        return realCosts(sample) - storage
    }

    fun realCosts(sample: Sample): Molecules {
        return (sample.costs - expertise).filterPositive()
    }

    fun neededMolecule(samples: List<Sample>): Molecules {
        var expertiseSimulated = expertise
        var totalCost = emptyMolecules
        samples.forEach { s ->
            totalCost += (s.costs - expertiseSimulated).filterPositive()
            expertiseSimulated = expertiseSimulated.add(s.gain, 1)
        }
        return (totalCost - storage).filterPositive()
    }

    fun isMoving() = eta > 0

}