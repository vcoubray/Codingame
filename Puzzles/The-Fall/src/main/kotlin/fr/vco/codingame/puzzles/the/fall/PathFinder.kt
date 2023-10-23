package fr.vco.codingame.puzzles.the.fall

import java.util.ArrayDeque

class PathFinder(private val board: Board) {

    fun findValidGlobalPaths(start: Position): List<Path> {
        val validPaths = mutableListOf<Path>()
        val toVisit = ArrayDeque<Path>().apply { add(Path(start.index, start.dir)) }

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()

            if (current.currentCell == board.exit) {
                validPaths.add(current)
                continue
            }

            board.neighbors[current.currentDirection]!![current.currentCell]
                .filterNot { (dir, _) -> dir == Direction.NONE || dir == Direction.CRASH }
                .forEach { (dir, cell) ->
                    val path = current.cellPath + cell
                    val directions = current.directions + dir
                    toVisit.add(Path(cell, dir, path, directions))
                }
        }
        return validPaths
    }

    fun findAllGlobalPaths(start: Position): List<Path> {
        val validPaths = mutableListOf<Path>()
        val toVisit = ArrayDeque<Path>().apply { add(Path(start.index, start.dir)) }

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()

            if (current.currentDirection == Direction.NONE || current.currentDirection == Direction.CRASH || current.currentCell == board.exit) {
                if (current.cellPath.size > 1) validPaths.add(current)
                continue
            }

            board.neighbors[current.currentDirection]!![current.currentCell]
                .forEach { (dir, cell) ->
                    val path = current.cellPath + cell
                    val directions = current.directions + dir
                    toVisit.add(Path(cell, dir, path, directions))
                }
        }
        return validPaths
    }

    fun toDetailedPaths(
        globalPath: Path
    ): List<DetailedPath> {
        val cellPaths = mutableListOf<List<Int>>()

        for (i in 0 until globalPath.directions.size - 1) {
            val cellIndex = globalPath.cellPath[i]
            val type = CELL_TYPES[board.cellTypes[cellIndex]]

            if (board.fixedMap[cellIndex]) {
                cellPaths.add(listOf(type.id))
                continue
            }
            val inputDir = globalPath.directions[i]
            val outputDir = globalPath.directions[i + 1]
            cellPaths.add(getPossibleOrientations(type.id, inputDir, outputDir))
        }
        return cellPaths.getCombinations().map { DetailedPath(globalPath.cellPath, it,globalPath.directions) }
    }
}