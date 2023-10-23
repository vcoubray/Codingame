package fr.vco.codingame.puzzles.the.fall

class CellType(
    val id: Int,
    orientations: List<Int> = listOf(),
    val exitDirection: (Direction) -> Direction,
) {
    val rotate = mapOf(
        Rotation.RIGHT to (orientations.firstOrNull()?: id),
        Rotation.LEFT to (orientations.lastOrNull()?: id)
    )

    private val allOrientations = listOf(id) + orientations

    fun allExitDirections(inputDir: Direction) = allOrientations.map { CELL_TYPES[it].exitDirection(inputDir) }.toSet()
    fun possibleOrientations(inputDir: Direction, outputDir: Direction) =
        allOrientations.filter { CELL_TYPES[it].exitDirection(inputDir) == outputDir }

    fun getRotations() = allOrientations.associateWith {
        val distance = CELL_TYPES[id].allOrientations.indexOf(it)
        when (distance) {
            0 -> listOf()
            1 -> listOf(Rotation.RIGHT)
            2 -> listOf(Rotation.RIGHT, Rotation.RIGHT)
            3 -> listOf(Rotation.LEFT)
            else -> throw IllegalArgumentException("[$id] can't be rotate in [$it]")
        }
    }
}

val CELL_TYPES = listOf(
    CellType(0) { _ -> Direction.NONE },
    CellType(1) { _ -> Direction.TOP },
    CellType(
        2,
        listOf(3)
    ) { inputDir -> if (inputDir == Direction.LEFT || inputDir == Direction.RIGHT) inputDir else Direction.NONE },
    CellType(3, listOf(2)) { inputDir -> if (inputDir == Direction.TOP) Direction.TOP else Direction.NONE },
    CellType(
        4,
        listOf(5)
    ) { inputDir -> if (inputDir == Direction.RIGHT) Direction.TOP else if (inputDir == Direction.TOP) Direction.RIGHT else Direction.CRASH },
    CellType(
        5,
        listOf(4)
    ) { inputDir -> if (inputDir == Direction.LEFT) Direction.TOP else if (inputDir == Direction.TOP) Direction.LEFT else Direction.CRASH },
    CellType(
        6,
        listOf(7, 8, 9)
    ) { inputDir -> if (inputDir == Direction.LEFT || inputDir == Direction.RIGHT) inputDir else Direction.CRASH },
    CellType(
        7,
        listOf(8, 9, 6)
    ) { inputDir -> if (inputDir == Direction.TOP || inputDir == Direction.RIGHT) Direction.TOP else Direction.NONE },
    CellType(
        8,
        listOf(9, 6, 7)
    ) { inputDir -> if (inputDir == Direction.RIGHT || inputDir == Direction.LEFT) Direction.TOP else Direction.NONE },
    CellType(
        9,
        listOf(6, 7, 8)
    ) { inputDir -> if (inputDir == Direction.LEFT || inputDir == Direction.TOP) Direction.TOP else Direction.NONE },
    CellType(10, listOf(11, 12, 13)) { inputDir -> if (inputDir == Direction.TOP) Direction.RIGHT else if (inputDir == Direction.LEFT) Direction.CRASH else Direction.NONE },
    CellType(11, listOf(12, 13, 10)) { inputDir -> if (inputDir == Direction.TOP) Direction.LEFT else if (inputDir == Direction.RIGHT) Direction.CRASH else Direction.NONE },
    CellType(12, listOf(13, 10, 11)) { inputDir -> if (inputDir == Direction.RIGHT) Direction.TOP else Direction.NONE },
    CellType(13, listOf(10, 11, 12)) { inputDir -> if (inputDir == Direction.LEFT) Direction.TOP else Direction.NONE }
)


val EXIT_DIRECTIONS = Direction.values().associateWith { dir -> CELL_TYPES.map { it.allExitDirections(dir) } }
fun getExits(cellType: Int, inputDir: Direction, isFixed: Boolean): Set<Direction> {
    return if (isFixed) setOf(CELL_TYPES[cellType].exitDirection(inputDir))
    else EXIT_DIRECTIONS[inputDir]!![cellType]
}

val POSSIBLE_ORIENTATIONS = Direction.values().associateWith { inputDir ->
    Direction.values().associateWith { outputDir ->
        CELL_TYPES.map { it.possibleOrientations(inputDir, outputDir) }
    }
}

fun getPossibleOrientations(cellType: Int, inputDir: Direction, outputDir: Direction) =
    POSSIBLE_ORIENTATIONS[inputDir]!![outputDir]!![cellType]


val ROTATIONS = CELL_TYPES.map { it.getRotations() }
fun getRotations(origin: Int, target: Int): List<Rotation> {
    return ROTATIONS.getOrNull(origin)?.get(target)
        ?: throw IllegalArgumentException("[$origin] can't be rotate in [$target]")
}