package fr.vco.codingame.contest.springchallenge2022

data class PatrolPath(val path: List<Pos>) {
    var currentPos = 0

    fun nextPos(pos: Pos): Pos? {
        if (path.isEmpty()) return null
        if (path[currentPos] == pos) {
            currentPos = (currentPos + 1) % path.size
        }
        return path[currentPos]
    }
}