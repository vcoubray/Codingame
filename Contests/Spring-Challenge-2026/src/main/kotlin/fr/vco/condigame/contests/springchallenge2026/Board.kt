package fr.vco.condigame.contests.springchallenge2026

class Board(val width: Int, val height: Int, val grid: List<String>) {

    companion object {
        const val GRASS = '.'
        const val WATER = '~'
        const val ROCK = '#'
        const val IRON = '+'
        const val MY_SHACK = '0'
        const val OPP_SHACK = '1'
    }


    val size = width * height
    val neighbours = List(size) { mutableListOf<Int>() }
    val isNextWater = BooleanArray(size) { false }
    val isNextIron = BooleanArray(size) { false }
    val myShackTiles: List<Int>
    val oppShackTiles: List<Int>
    val ironTiles: List<Int>

    val distances: List<IntArray>

    init {

        val dir = listOf(
            0 to 1,
            0 to -1,
            1 to 0,
            -1 to 0
        )

        val isNextMyShack = BooleanArray(size) { false }
        val isNextOppShack = BooleanArray(size) { false }
        grid.forEachIndexed { y, line ->
            line.forEachIndexed { x, _ ->
                val tileId = getTileId(x, y)
                dir.map { (vx, vy) -> x + vx to y + vy }
                    .filter { (nx, ny) -> nx in 0 until width && ny in 0 until height }
                    .forEach { (nx, ny) ->
                        val nTileId = getTileId(nx, ny)
                        val nTile = grid[ny][nx]
                        when (nTile) {
                            GRASS -> neighbours[tileId].add(nTileId)
                            WATER -> isNextWater[tileId] = true
                            IRON -> isNextIron[tileId] = true
                            MY_SHACK -> isNextMyShack[tileId] = true
                            OPP_SHACK -> isNextOppShack[tileId] = true
                        }
                    }
            }
        }
        myShackTiles = isNextMyShack.indices.filter { isNextMyShack[it] && isGrass(it) }
        oppShackTiles = isNextOppShack.indices.filter { isNextOppShack[it] && isGrass(it) }
        ironTiles = isNextIron.indices.filter { isNextIron[it] && isGrass(it) }

        distances = List(size) { computeDistances(it) }
    }

    fun computeDistances(start: Int): IntArray {
        val toVisit = ArrayDeque<Int>().apply { add(start) }
        val distances = IntArray(size) { -1 }
        distances[start] = 0

        while (toVisit.isNotEmpty()) {
            val curr = toVisit.removeFirst()

            neighbours[curr].filter { distances[it] == -1 }
                .forEach {
                    distances[it] = distances[curr] + 1
                    toVisit.addLast(it)
                }
        }

        return distances
    }

    fun findPath(start: Int, target: Int, blockedTiles: Collection<Int>): List<Int> {
        if (start == target) return listOf(start)
        val blocked = blockedTiles.toSet()
        if (target in blocked) return emptyList()
        val parent = IntArray(size) { -1 }
        val visited = BooleanArray(size)
        visited[start] = true
        val queue = ArrayDeque<Int>()
        queue.add(start)
        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            for (n in neighbours[curr]) {
                if (visited[n] || n in blocked) continue
                visited[n] = true
                parent[n] = curr
                if (n == target) {
                    val path = ArrayDeque<Int>()
                    var cur = n
                    while (cur != -1) {
                        path.addFirst(cur)
                        cur = parent[cur]
                    }
                    return path.toList()
                }
                queue.addLast(n)
            }
        }
        return emptyList()
    }

    fun isGrass(tileId: Int): Boolean {
        val (x, y) = getCoord(tileId)
        return grid[y][x] == GRASS
    }

    fun getTileId(x: Int, y: Int) = y * width + x
    fun getCoord(id: Int) = id % width to id / width
}