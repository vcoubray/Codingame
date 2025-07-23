import kotlin.math.abs

fun readInt() = readln().toInt()
fun readInts() = readln().split(" ").map { it.toInt() }
operator fun <T> List<T>.component6() = this[5]


data class Position(val x: Int, val y: Int) {
    operator fun plus(p: Position) = Position(x + p.x, y + p.y)
    operator fun unaryMinus() = Position(-x, -y)
    fun distanceManhattan(p: Position) = abs(x - p.x) + abs(y - p.y)
}

class Board(val width: Int, val height: Int) {

    companion object {
        const val EMPTY = 0
        const val SMALL_COVER = 1
        const val GREAT_COVER = 2

        val MANHATTAN_DIRECTIONS = listOf(
            Position(0, 1),
            Position(0, -1),
            Position(1, 0),
            Position(-1, 0)
        )
        val SPLASH_DIRECTIONS = MANHATTAN_DIRECTIONS + listOf(
            Position(0, 0),
            Position(1, 1),
            Position(1, -1),
            Position(-1, 1),
            Position(-1, -1)
        )
    }

    val grid = IntArray(width * height) { 0 }
    lateinit var splashRange: List<List<Int>>
    lateinit var neighbours: List<List<Int>>
    val distances = List(grid.size) { start ->
        List(grid.size) { dest ->
            idToPos(start).distanceManhattan(idToPos(dest))
        }
    }

    val damageReductions = Array(width * height) { IntArray(width * height) { 100 } }

    fun readGrid() {
        repeat(height) { i ->
            readInts().chunked(3) {
                val (x, y, tileType) = it
                grid[posToId(x, y)] = tileType
            }
        }
        neighbours = computeNeighbours(MANHATTAN_DIRECTIONS)
        splashRange = computeNeighbours(SPLASH_DIRECTIONS)
    }

    private fun computeNeighbours(directions: List<Position>): List<List<Int>> {
        return grid.mapIndexed { i, tileType ->
            val pos = idToPos(i)
            directions.map { dir -> pos + dir }
                .filter { it.isInGrid() }
                .map { it.toId() }
        }
    }

    private fun computeCover(): List<List<Int>> {
        grid.forEachIndexed { i, tileType ->
            if (tileType != EMPTY) {
                val coverPos = idToPos(i)
                val damageReduction = if (tileType == SMALL_COVER) {
                    50
                } else {
                    25
                }
                MANHATTAN_DIRECTIONS.forEach { dir ->
                    val coveredPos = coverPos + dir
                    if (coveredPos.isInGrid()) {
                        val coverDirection = -dir

                        val xRange = when (coverDirection.x) {
                            0 -> 0 until width
                            1 -> coverPos.x + 1 until width
                            else -> 0..coverPos.x - 1
                        }

                        val yRange = when (coverDirection.y) {
                            0 -> 0 until height
                            1 -> coverPos.y + 1 until height
                            else -> 0..coverPos.y - 1
                        }

                        for (y in yRange) {
                            for (x in xRange) {
                                val tileId = posToId(x, y)
                                if (tileId !in splashRange[coverPos.toId()] && damageReductions[coveredPos.toId()][tileId] > damageReduction) {
                                    damageReductions[coveredPos.toId()][tileId] = damageReduction
                                }
                            }
                        }
                    }
                }
            }
        }
        return emptyList()
    }

    fun getDamages(shooter: Agent, target: Agent): Int {
        val distance = distances [shooter.tileId][target.tileId]
        val rawDamage = when {
            distance <= shooter.optimalRange -> shooter.soakingPower
            distance <= shooter.optimalRange * 2 -> shooter.soakingPower / 2
            else -> 0
        }

        return rawDamage * damageReductions[target.tileId][shooter.tileId] / 100
    }


    fun idToPos(id: Int) = Position(id % width, id / width)
    fun posToId(x: Int, y: Int) = y * width + x
    fun posToId(pos: Position) = pos.y * width + pos.x
    fun Position.toId() = y * width + x
    fun Position.isInGrid() = x in 0 until width && y in 0 until height
}

data class AgentDef(
    val id: Int,
    val player: Int,
    val shootCooldown: Int,
    val optimalRange: Int,
    val soakingPower: Int
)

data class Agent(
    val id: Int,
    val player: Int,
    val shootCooldown: Int,
    val optimalRange: Int,
    val soakingPower: Int,
    var tileId: Int,
    var cooldown: Int = -1,
    var splashBombs: Int = 0,
    var wetness: Int = 0
) {
    constructor(def: AgentDef, titleId: Int, coolDown: Int, splashBombs: Int, wetness: Int) : this(
        id = def.id,
        player = def.player,
        shootCooldown = def.shootCooldown,
        optimalRange = def.optimalRange,
        soakingPower = def.soakingPower,
        tileId = titleId,
        cooldown = coolDown,
        splashBombs = splashBombs,
        wetness = wetness
    )

    fun isAtRange(tileId: Int, board: Board) = board.distances[this.tileId][tileId] <= optimalRange * 2
}


fun main() {

    val me = readInt() // Your player id (0 or 1)
    val agentDataCount = readInt() // Total number of agents in the game

    val agents = List(agentDataCount) {
        val (id, player, shootCooldown, optimalRange, soakingPower, _) = readInts()
        AgentDef(id, player, shootCooldown, optimalRange, soakingPower)
    }.associateBy { it.id }

    val (width, height) = readInts()
    val board = Board(width, height)
    board.readGrid()


    // game loop
    while (true) {
        val agentCount = readInt()
        val livingAgents = List(agentCount) {
            val (id, x, y, cooldown, splashBombs, wetness) = readInts()
            Agent(agents[id]!!, board.posToId(x, y), cooldown, splashBombs, wetness)
        }
        val myAgentCount = readInt()
        val (myAgents, oppAgents) = livingAgents.filter { it.wetness < 100 }.partition { it.player == me }

        agents.forEach(System.err::println)
        myAgents.forEach(System.err::println)

        myAgents.forEach { agent ->
            val actions = mutableListOf<String>()

            val dest = oppAgents.minBy{board.distances[it.tileId][agent.tileId]}
            if(board.distances[dest.tileId][agent.tileId] > 1) {
                actions.add(moveAction(board.idToPos(dest.tileId)))
            }

            if(agent.cooldown==0) {
                val target = oppAgents.maxBy { board.getDamages(agent, it) }
                if (agent.isAtRange(target.tileId, board)) {
                    actions.add(shootAction(target))
                }
            } else {
                actions.add(hunkerDownAction())
            }

            println("${agent.id};${actions.joinToString(";")}")
        }
    }

}



fun moveAction(x: Int, y: Int) = "MOVE $x $y"
fun moveAction(pos: Position) = "MOVE ${pos.x} ${pos.y}"
fun shootAction(target: Agent) = "SHOOT ${target.id}"
fun throwAction(x: Int, y: Int) = "THROW $x $y"
fun hunkerDownAction() = "HUNKER_DOWN"
