package fr.vco.codingame.puzzles.the.fall



class Path(
    val currentCell: Int,
    val currentDirection: Direction,
    val path: List<Int> = emptyList(),
    val directions: List<Direction> = emptyList()
) {

    override fun equals(other: Any?): Boolean {
        return if (other is Path) other.currentCell == currentCell && other.currentDirection == currentDirection
        else false
    }

    override fun hashCode() = currentCell * 37000 + directions.hashCode()

    fun toRealPath(
        cellTypesMap: List<Int>,
        isFixed: (cellIndex: Int, pathIndex:Int) -> Boolean = { _, _ -> false },
    ): List<RealPath> {
        val realPaths = mutableListOf<List<Int>>()

        for (i in 0 until this.directions.size - 1) {
            val cellIndex = this.path[i]
            val type = CELL_TYPES[cellTypesMap[cellIndex]]

            if (isFixed(cellIndex, i)) {
                realPaths.add(listOf(type.id))
                continue
            }
            val inputDir = this.directions[i]
            val outputDir = this.directions[i + 1]
            realPaths.add(getPossibleOrientations(type.id, inputDir, outputDir))
        }

        return realPaths.getCombinations().map { RealPath(this.path, it) }
    }

    fun getCrossingOffset(path: Path) : Int {
        for (i in this.path.indices) {
            if (i >= path.path.size) return -1
            for (j in i until path.path.size) {
                if (this.path[i] == path.path[j]) return j - i
            }
        }
        return -1
    }

}

class RealPath(
    val path: List<Int> = emptyList(),
    val cellTypes: List<Int> = emptyList()
){

    fun drop(i: Int): RealPath {
        return RealPath(
            path.drop(i),
            cellTypes.drop(i)
        )
    }
}

fun <T : Any> List<List<T>>.getCombination(i: Long): List<T> {

    return buildList {
        var index = i
        this@getCombination.filter { it.isNotEmpty() }.forEach {
            val current = index % it.size
            index /= it.size
            this.add(it[current.toInt()])
        }
    }
}

fun <T : Any> List<List<T>>.getCombinationCount(): Long {
    return this.filter { it.isNotEmpty() }.fold(1L) { acc, a -> a.size * acc }
}

fun <T : Any> List<List<T>>.getCombinations(): List<List<T>> {
    return List(getCombinationCount().toInt()) { getCombination(it.toLong()) }
}