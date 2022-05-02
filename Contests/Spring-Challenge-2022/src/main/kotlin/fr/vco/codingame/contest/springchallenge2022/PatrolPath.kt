package fr.vco.codingame.contest.springchallenge2022

data class PatrolPath(private val path: List<Pos>) {
    private var currentPos = 0

    fun nextPos(pos: Pos): Pos? {
        return nextPos(path[currentPos] == pos)
    }

    fun nextPos(increment : Boolean = true): Pos? {
        if (path.isEmpty()) return null
        if (increment) {
            currentPos = (currentPos + 1) % path.size
        }
        return path[currentPos]
    }
}