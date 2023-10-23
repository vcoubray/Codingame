package fr.vco.codingame.puzzles.the.fall

class Path(
    val currentCell: Int,
    val currentDirection: Direction,
    val cellPath: List<Int> = emptyList(),
    val directions: List<Direction> = emptyList(),
) {

    override fun equals(other: Any?): Boolean {
        return if (other is Path) other.currentCell == currentCell && other.currentDirection == currentDirection
        else false
    }

    override fun hashCode() = currentCell * 37000 + directions.hashCode()

    fun isCrossing(path: Path): Boolean {
        for (i in this.cellPath.indices) {
            if (i >= path.cellPath.size) return false

            if(this.cellPath[i] == path.cellPath[i]) return true

            if (i > 0) {
                val pathCellPrev = path.cellPath[i - 1]
                val cellPrev = this.cellPath[i - 1]
                if (path.cellPath[i] == cellPrev && pathCellPrev == this.cellPath[i]) return true
            }
        }
        return false
    }
}

class DetailedPath(
    val path: List<Int>,
    val cellTypes: List<Int>,
    val directions: List<Direction> = emptyList(),
) {
    val size = cellTypes.size
}