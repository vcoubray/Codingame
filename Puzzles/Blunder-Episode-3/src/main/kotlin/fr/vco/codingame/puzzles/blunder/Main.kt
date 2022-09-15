package fr.vco.codingame.puzzles.blunder

import java.util.*
import kotlin.math.ln
import kotlin.math.pow

val MODELS = mapOf<String, (Double) -> Double>(
    "O(1)" to { 1.0 },
    "O(log n)" to { n -> ln(n) },
    "O(n)" to { n -> n },
    "O(n log n)" to { n -> n * ln(n) },
    "O(n^2)" to { n -> n * n },
    "O(n^2 log n)" to { n -> n * n * ln(n) },
    "O(n^3)" to { n -> n.pow(3.0) },
    "O(2^n)" to { n -> 2.0.pow(n) }
)

fun main() {
    val input = Scanner(System.`in`)
    val N = input.nextInt()

    val sample = List(N) {
        val (num, t) = List(2) { input.nextDouble() }
        num to t
    }
    val mean = sample.map { (_, t) -> t }.average()

    val (n0, t0) = sample.first()
    val (nf, tf) = sample.last()

    MODELS
        .map { (id, function) ->
            val slope = (function(nf) - function(n0))
            val ratio = if (slope != 0.0) (tf - t0) / slope else mean
            val offset = if (slope != 0.0) t0 - (function(n0) * ratio) else 0.0

            id to sample.map { (n, t) -> t - (ratio * function(n) + offset) }.map { it * it }
        }
        .map { (id, residualSquares) -> id to residualSquares.sum() }
        .minByOrNull { (_, squareSum) -> squareSum }
        ?.let { (id, _) -> println(id) }
}