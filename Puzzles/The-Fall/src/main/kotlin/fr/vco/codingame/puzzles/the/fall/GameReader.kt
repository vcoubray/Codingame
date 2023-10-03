package fr.vco.codingame.puzzles.the.fall

import java.util.*
import kotlin.math.absoluteValue


object GameReader {
    data class Position2D(val x: Int, val y: Int, val dir: Direction)

    private val input = Scanner(System.`in`)
    private var width = 0

    fun readBoard(): Board {
        width = input.nextInt()
        val height = input.nextInt()

        val cellTypes = mutableListOf<Int>()
        val fixedMap = mutableListOf<Boolean>()
        repeat(height) {
            repeat(width) {
                val cellType = input.nextInt()
                fixedMap.add(cellType < 0)
                cellTypes.add(cellType.absoluteValue)
            }
        }

        val exit = (height - 1) * width + input.nextInt()
        val neighbors = listOf(Direction.TOP, Direction.RIGHT, Direction.LEFT)
            .associateWith { dir ->
                cellTypes.mapIndexed { i, cellType ->
                    getExits(cellType, dir, fixedMap[i])
                        .map { Position2D(i % width + it.x, i / width + it.y, it) }
                        .filter { (nx, ny) -> nx in 0 until width && ny in 0 until height }
                        .map { it.dir to it.y * width + it.x }
                }
            }

        return Board(width, height, exit, cellTypes, fixedMap, neighbors)
    }

    fun readIndy() = readPosition()
    fun readRocks(): List<Position> {
        val r = input.nextInt()
        return List(r) { readPosition() }
    }

    private fun readPosition() = toPosition(input.nextInt(), input.nextInt(), Direction.valueOf(input.next()))
    private fun toPosition(x: Int, y: Int, dir: Direction) = Position(y * width + x, dir)
}
