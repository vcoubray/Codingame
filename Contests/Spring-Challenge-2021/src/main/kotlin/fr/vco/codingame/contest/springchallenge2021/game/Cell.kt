package fr.vco.codingame.contest.springchallenge2021.game

class Cell(
    val index: Int,
    val richness: Int,
    val neighIndex: List<Int>,
    val neighByDirection: MutableList<List<Cell>> = mutableListOf(),
    var neighByRange: List<List<Cell>> = emptyList(),
    var seedableNeighByRange: MutableList<List<Cell>> = mutableListOf()
) {
    override fun toString(): String {
        val sb = StringBuffer("Cell [index = $index; richness = $richness;]\n")
        neighByDirection.forEachIndexed { i, it ->
            sb.append("dir $i : ${it.joinToString(" ") { c -> c.index.toString() }}\n")
        }
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if(other is Cell?) other?.index == index
        else false
    }

    override fun hashCode(): Int {
        return index
    }

}