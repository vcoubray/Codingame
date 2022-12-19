package fr.vco.codingame.constest.fallchallenge2022

import java.util.*

fun log(message: Any?) = System.err.println(message)

fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt()
    val height = input.nextInt()
    val board = Board(height, width)


    var totalRecycler = 0
    while (true) {
        var myMatter = input.nextInt()
        val oppMatter = input.nextInt()
        board.updateBoard(input)

        var recyclerCount = board.tiles.count { it.recycler && it.owner == Owner.ME }

        val actions = mutableListOf<Action>()
        val zones = board.computeZones().sortedByDescending { it.tiles.size }
        for (zone in zones) {
            log("${zone.tiles.size} - ${zone.tiles.first().pos}")
            if (zone.myTileCount == 0) continue
            if (zone.myTileCount == zone.tiles.size) continue
            val shouldSpawn = !(zone.myRobotCount > 1 && zone.tiles.none { it.owner == Owner.OPP && it.units > 0 })

            // Build
            val buildTiles = zone.tiles.filter { it.canBuild && it.recyclerScore() > 0 }
            while (myMatter >= 10 && recyclerCount < 5 && totalRecycler < 12) {
                buildTiles.filterNot { it.recycler }.maxByOrNull { it.recyclerScore() }?.let {
                    actions.add(Action.Build(it.pos))
                    it.recycler = true
                    it.neighbours.forEach { n -> n.inRangeOfRecycler = true }
                    myMatter -= 10
                    totalRecycler++
                    recyclerCount++
                } ?: break
            }

            // Moves
            val targets = zone.tiles.filter { it.owner != Owner.ME }.toMutableList()
            zone.tiles.filter { it.owner == Owner.ME && it.units > 0 }.forEach { robot ->
                repeat(robot.units) { _ ->
                    val target = targets.minByOrNull { it.dist(robot) }
                    targets.remove(target)
                    target?.let { actions.add(Action.Move(1, robot.pos, target.pos)) }
                }
            }

            // Spawn
            if (shouldSpawn && myMatter >= 10) {
                zone.tiles.asSequence().filter { it.owner == Owner.ME && it.canSpawn() }
                    .map { it to board.searchPath(it.id) { target -> board.tiles[target].owner == Owner.OPP }.size }
                    .filter { (_, pathSize) -> pathSize > 0 }
                    .sortedBy { (_, pathSize) -> pathSize }
                    .take(myMatter / 10).toList()
                    .forEach { (tile, _) -> actions.add(Action.Spawn(1, tile.pos)) }
            }
        }

        println(actions.takeIf { it.isNotEmpty() }?.joinToString(";") ?: Action.Wait)
    }
}

