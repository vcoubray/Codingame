package fr.vco.codingame.puzzles.mars.lander.algorithm

import fr.vco.codingame.puzzles.mars.lander.*
import fr.vco.codingame.puzzles.mars.lander.engine.Action
import fr.vco.codingame.puzzles.mars.lander.engine.CapsuleState
import fr.vco.codingame.puzzles.mars.lander.engine.Surface
import fr.vco.codingame.puzzles.mars.lander.engine.play
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class GeneticAlgorithm(
    private val surface: Surface,
    private val initialState: CapsuleState,
) {

    var population = Array(POPULATION_SIZE) { generateChromosome() }
    private var children = List(POPULATION_SIZE) {
        Chromosome(List(CHROMOSOME_SIZE) {
            Action(0, 0)
        }.toTypedArray())
    }.toTypedArray()

    private val workingState = initialState.copy()
    private var bestChromosome = population.first()

    init {
        population.forEach(::evaluation)
    }

    private fun generateChromosome() = Chromosome(
        List(CHROMOSOME_SIZE) { Action(0, 0).apply(Action::randomize) }.toTypedArray()
    )

    private fun evaluation(chromosome: Chromosome) {
        workingState.loadFrom(initialState)
        val fitnessResult = workingState.play(chromosome.actions, surface)
        chromosome.state.loadFrom(workingState)
        chromosome.score = computeScore(fitnessResult)
        if (chromosome.score > bestChromosome.score) {
            bestChromosome = chromosome
        }
    }


    private fun computeScore(fitnessResult: FitnessResult): Double {

        val rotateMax = 80.0

        val normalizedXSpeed = max(0.0, (SPEED_MAX - fitnessResult.xSpeedOverflow) / SPEED_MAX)
        val normalizedYSpeed = max(0.0, (SPEED_MAX - fitnessResult.ySpeedOverflow) / SPEED_MAX)
        val normalizedRotate = (rotateMax - fitnessResult.rotateOverflow) / rotateMax
        val normalizedDistance = (surface.distanceMax - fitnessResult.distance) / surface.distanceMax

        return normalizedXSpeed * X_SPEED_WEIGHT + normalizedYSpeed * Y_SPEED_WEIGHT + normalizedRotate * ROTATE_WEIGHT + normalizedDistance * DISTANCE_WEIGHT
    }


    private fun selection() = Random.nextInt(POPULATION_MIDDLE) + POPULATION_MIDDLE

    private fun crossoverAndMutate(
        parent1: Chromosome,
        parent2: Chromosome,
        children1: Chromosome,
        children2: Chromosome,
    ) {

        val weight = Random.nextDouble(0.8) + 0.1
        val oppWeight = (1 - weight)
        for (i in 0 until CHROMOSOME_SIZE) {

            children1.actions[i].rotate =
                (weight * parent1.actions[i].rotate + oppWeight * parent2.actions[i].rotate).roundToInt()
            children1.actions[i].power =
                (weight * parent1.actions[i].power + oppWeight * parent2.actions[i].power).roundToInt()
            children2.actions[i].rotate =
                (oppWeight * parent1.actions[i].rotate + weight * parent2.actions[i].rotate).roundToInt()
            children2.actions[i].power =
                (oppWeight * parent1.actions[i].power + weight * parent2.actions[i].power).roundToInt()
        }
        children1.score = -1.0
        children2.score = -1.0

        mutation(children1)
        mutation(children2)

    }

    private fun mutation(chromosome: Chromosome) {
        for (action in chromosome.actions) {
            if (Random.nextDouble(1.0) < MUTATION_PROBABILITY) {
                action.randomize()
            }
        }
    }

    private fun nextGeneration() {
        population.sortBy { it.score }

        for (i in 0 until CHILDREN_SIZE / 2) {

            val parentId1 = selection()
            var parentId2 = -1
            while (parentId2 == -1 || parentId2 == parentId1) {
                parentId2 = selection()
            }
            crossoverAndMutate(
                population[parentId1],
                population[parentId2],
                children[i * 2],
                children[i * 2 + 1]
            )

            evaluation(children[i * 2])
            evaluation(children[i * 2 + 1])
        }

        for (i in CHILDREN_SIZE until POPULATION_SIZE) {
            children[i] = population[i]
        }

        val temp = population
        population = children
        children = temp
    }


    fun search(timeout: Long): Chromosome {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeout) {
            nextGeneration()
        }
        return bestChromosome
    }
}