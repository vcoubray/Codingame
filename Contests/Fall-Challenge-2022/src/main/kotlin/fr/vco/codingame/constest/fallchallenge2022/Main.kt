package fr.vco.codingame.constest.fallchallenge2022

import java.util.*
import kotlin.math.min

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
        val startTime = System.currentTimeMillis()
        board.updateBoard(input)

        var recyclerCount = board.tiles.count { it.recycler && it.owner == Owner.ME }

        val actions = mutableListOf<Action>()
        val zones = board.computeZones().sortedByDescending { it.tiles.size }
        for (zone in zones) {
            log("${zone.tiles.size} - ${zone.tiles.first().pos} ${zone.myRobotCount} $myMatter")
            if (zone.myTileCount == 0) continue
            if (zone.myTileCount == zone.tiles.size) continue
            val shouldSpawn = zone.myRobotCount == 0 || zone.tiles.any { it.owner == Owner.OPP && it.units > 0 }

            // Build
            val buildTiles = zone.recyclerTargets.filter { it.canBuild }.toMutableList()
            while (myMatter >= 10 && buildTiles.isNotEmpty()) {
                buildTiles.removeFirstOrNull()
                    ?.let {
                        actions.add(Action.Build(it.pos))
                        it.recycler = true
                        it.neighbours.forEach { n -> n.inRangeOfRecycler = true }
                        myMatter -= 10
                        totalRecycler++
                        recyclerCount++
                    } ?: break
            }

            while (myMatter >= 10) {
                zone.tiles.filter { it.owner == Owner.ME && !it.recycler && it.units == 0 && it.neighbours.any { n -> n.owner == Owner.OPP && n.units > 0 } }
                    .minByOrNull { it.minDistMine}
                    ?.let {
                        actions.add(Action.Build(it.pos))
                        myMatter -=10
                        it.recycler = true
                        it.neighbours.forEach { n -> n.inRangeOfRecycler = true }
                    }
                    ?: break

            }
            // Moves
//            val targets = zone.tiles
//                .filter { it.owner != Owner.ME && it.neighbours.any { n -> n.owner == Owner.ME } && !it.recycler }
//                .sortedBy { it.minDistOpp }
            val robots = zone.tiles.filter { it.owner == Owner.ME && it.units > 0 }.toMutableList()

            val targets = mutableListOf<Tile>()
            robots.forEach { robot ->
                repeat(robot.units) {
                    val target =
                        robot.neighbours.firstOrNull { it.owner != Owner.ME && it.canCross(1) && it !in targets }
                            ?: zone.tiles.filter { it.owner != Owner.ME && it.canCross() }
                                .minByOrNull { it.dist(robot) }
                    target?.let {
                        actions.add(Action.Move(1, robot.pos, target.pos))
                        targets.add(target)
                    }
                }
            }

//            targets.forEach { target ->
//                val robot = robots.minByOrNull { it.dist(target) }
//                robot?.let {
//                    robot.units--
//                    if (robot.units == 0) robots.remove(robot)
//                    actions.add(Action.Move(1, robot.pos, target.pos))
//
//                }
//            }

            // Spawn
            val spawnTarget = zone.tiles.filter { it.owner == Owner.ME && !it.recycler && it.neighbours.any { n -> n.scrapAmount >0 && n.owner != Owner.ME } }
                .map {it to it.neighbours.sumOf{n -> if (n.owner == Owner.OPP) it.units else 0}}
                .sortedByDescending { (_,units) -> units }
                .toMutableList()
            while (myMatter >= 10 && spawnTarget.isNotEmpty()) {
                spawnTarget.removeFirstOrNull()
                    ?.let { (tile, units) ->
                        val spawnQty = min (units+1, myMatter/10)
                        actions.add(Action.Spawn(spawnQty, tile.pos))
                        myMatter -= 10 *spawnQty
                    }
                    ?: break
            }
//            if (shouldSpawn && myMatter >= 10) {
//                zone.tiles.asSequence().filter { it.owner == Owner.ME && it.canSpawn() }
//                    .map { it to board.searchPath(it.id) { target -> board.tiles[target].owner == Owner.OPP }.size }
//                    //.filter { (_, pathSize) -> pathSize > 0 }
//                    .sortedBy { (_, pathSize) -> pathSize }
//                    .take(myMatter / 10)//.toList()
//                    .forEach { (tile, _) ->
//                        myMatter -= 10
//                        actions.add(Action.Spawn(1, tile.pos))
//                    }
//            }
        }
        actions.add(Action.Message("${System.currentTimeMillis() - startTime}"))
        println(actions.takeIf { it.isNotEmpty() }?.joinToString(";") ?: Action.Wait)
    }
}

