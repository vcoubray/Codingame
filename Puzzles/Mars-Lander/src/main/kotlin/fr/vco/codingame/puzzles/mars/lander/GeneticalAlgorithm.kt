package fr.vco.codingame.puzzles.mars.lander

import fr.vco.codingame.puzzles.mars.lander.engine.Action
import fr.vco.codingame.puzzles.mars.lander.engine.State
import fr.vco.codingame.puzzles.mars.lander.engine.Surface
import fr.vco.codingame.puzzles.mars.lander.engine.generateAction
import kotlin.math.roundToInt
import kotlin.random.Random

class Chromosome(var id: Int, var actions: Array<Action>) {
    var score = 0.0
    var cumulativeScore = 0.0
    var state: State? = null

    override fun equals(other: Any?): Boolean {
        return if (other is Chromosome) id == other.id
        else false
    }

    override fun hashCode(): Int {
        return id
    }
}

class GeneticAlgorithm(
    var initialState: State,
    var surface: Surface,
    var chromosomeSize: Int,
    var populationSize: Int,
    var mutationProbability: Double,
    var elitismPercent: Double,
    var speedMax: Double,
    var speedWeight: Double
) {

    var population = generateRandomPopulation()
    var generationCount = 0
    var chromosomeIndex = 0


    fun generateChromosome(): Chromosome {
        return Chromosome(
            chromosomeIndex++,
            (0 until chromosomeSize).map {
                generateAction()
            }.toTypedArray()
        )
    }

    fun generateRandomPopulation(): Array<Chromosome> {
        return Array(populationSize) { generateChromosome() }
    }

    fun evaluation() {
        population.forEach {
            val state = initialState.copy()
            it.score = state.play(it.actions, surface, speedMax, speedWeight)
            it.state = state
        }
    }


    fun normalizeScores() {
        val sum = population.sumOf { it.score }
        for (chromosome in population) {
            chromosome.cumulativeScore = chromosome.score / sum
        }
    }

    fun cumulativeScores() {
        normalizeScores()
        population.sortBy { it.cumulativeScore }
        var cumul = 0.0
        for (chromosome in population) {
            chromosome.cumulativeScore += cumul
            cumul = chromosome.cumulativeScore
        }
    }


    fun wheelSelection(): Chromosome {
        val rnd = Random.nextDouble(1.0)
        var i = -1
        do {
            i++
        } while (i < population.size && rnd > population[i].cumulativeScore)

        return population[i]
    }

    fun weightCrossOver(parent1: Chromosome, parent2: Chromosome): Pair<Chromosome, Chromosome> {

        val child1Actions = mutableListOf<Action>()
        val child2Actions = mutableListOf<Action>()

        for (i in 0 until chromosomeSize) {
            val weight = Random.nextDouble(1.0)

            val rotate1 = weight * parent1.actions[i].rotate + (1.0 - weight) * parent2.actions[i].rotate
            val rotate2 = (1.0 - weight) * parent1.actions[i].rotate + weight * parent2.actions[i].rotate
            val power1 = weight * parent1.actions[i].power + (1.0 - weight) * parent2.actions[i].power
            val power2 = (1.0 - weight) * parent1.actions[i].power + weight * parent2.actions[i].power
            child1Actions.add(Action(rotate1.roundToInt(), power1.roundToInt()))
            child2Actions.add(Action(rotate2.roundToInt(), power2.roundToInt()))
        }

        return Chromosome(chromosomeIndex++, child1Actions.toTypedArray()) to Chromosome(
            chromosomeIndex++,
            child2Actions.toTypedArray()
        )

    }


    fun mutation(chromosome: Chromosome) {
        for (i in chromosome.actions.indices) {
            if (Random.nextDouble(1.0) < mutationProbability) {
                chromosome.actions[i] = generateAction()
            }
        }
    }

    fun nextGeneration() {

        var eliteSize = (populationSize * elitismPercent).toInt()
        if (eliteSize % 2 != 0) {
            eliteSize++
        }
        cumulativeScores()
        val children = mutableListOf<Chromosome>()
        while (children.size < populationSize - eliteSize) {
            val parent1 = wheelSelection()
            var parent2: Chromosome? = null
            while (parent2 == null || parent2.id == parent1.id) {
                parent2 = wheelSelection()
            }
            val (child1, child2) = weightCrossOver(parent1, parent2).also { (a, b) -> mutation(a);mutation(b) }
            children.add(child1)
            children.add(child2)
        }
        population = population.takeLast(eliteSize).toTypedArray() + children.toTypedArray()
    }

    fun search(timeout: Int): Chromosome {
        val start = System.currentTimeMillis()
        lateinit var result: Chromosome
        generationCount = 0
        var bestScore = 0.0
        while (System.currentTimeMillis() - start < timeout) {
            evaluation()
            nextGeneration()
            generationCount++

            for (chromosome in population) {
                if (chromosome.score > bestScore) {
                    result = chromosome
                    bestScore = chromosome.score
                }
            }
        }
        System.err.println("$generationCount -> $bestScore")
        return result
    }


}