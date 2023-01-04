package fr.vco.codingame.constest.fallchallenge2022

import java.util.*
import kotlin.collections.ArrayDeque

class Game(height: Int, width: Int) {
    private var myMatter = 0
    private var oppMatter = 0
    private val board = Board(height, width)

    private val bfs = BFS(board.tiles)

    private var turn = 0
    private var startTime = 0L

    fun play(input: Scanner) {
        myMatter = input.nextInt()
        oppMatter = input.nextInt()
        startTime = System.currentTimeMillis()
        updateBoard(input)

        val actions = mutableListOf<Action>()
        val zones = computeZones().sortedByDescending { it.tiles.size }
        val inConflict = zones.any{it.inConflict}
        for (zone in zones) {
            if (!zone.canDoThings) continue

            zone.tiles.filter { it.accessible }.forEach { origin ->
                origin.minDistOpp = bfs.searchPath(origin.id) { target ->
                    board[target].accessible && board[target].owner == Owner.OPP
                }.size.takeIf { it > 0 } ?: Int.MAX_VALUE
            }

            if(inConflict) {
                build(zone, actions)
            }
            expands(zone, actions)
            protect(zone, actions)
            moves(zone, actions)

            if (zone.shouldSpawn) {
                spawns(zone, actions)
            }

        }
        actions.add(Action.Message("${System.currentTimeMillis() - startTime}"))
        println(actions.takeIf { it.isNotEmpty() }?.joinToString(";") ?: Action.Wait)
    }

    private fun Tile.interestingTargets(zone: Zone) = this.neighbours.filter { n ->
        n.accessible &&
            n.owner == Owner.NEUTRAL &&
            n.id !in zone.alreadyTargeted
    }

    private fun build(zone: Zone, actions: MutableList<Action>) {
        val myRecyclers = board.tiles.filter { it.recycler }.toMutableList()

        while (myMatter >= 10) {
            val buildTargets =
                zone.tiles.filter { it.canBuild && it.destroyedNeighboursCount < 3 && it.scrapToRecycle >= 25 }
            buildTargets.filter { myRecyclers.none { r -> r.pos.dist(it.pos) < 3 } }
                .maxByOrNull { it.scrapToRecycle }
                ?.let {
                    actions.add(Action.Build(it.pos))
                    myMatter -= 10
                    it.recycler = true
                    it.neighbours.forEach { n -> n.inRangeOfRecycler = true }
                    myRecyclers.add(it)
                } ?: break
        }
    }

    private fun expands(zone: Zone, actions: MutableList<Action>) {

        // Move robots with only one target first
        val myRobots = zone.tiles.filter { it.owner == Owner.ME && it.units > 0 }
        var borderRobots = zone.tiles.filter { it.owner == Owner.ME && it.units > 0 }.filter {
            val targetCount = it.interestingTargets(zone).size
            targetCount > 0 && targetCount <= it.units
        }
        while (borderRobots.isNotEmpty()) {
            borderRobots.forEach { robot ->
                robot.interestingTargets(zone)
                    .forEach { target ->
                        actions.add(Action.Move(1, robot.pos, target.pos))
                        robot.units--
                        zone.alreadyTargeted.add(target.id)
                    }
            }

            borderRobots = myRobots.filter {
                val targetCount = it.interestingTargets(zone).size
                targetCount > 0 && targetCount <= it.units
            }
        }
        // Then move remains border robots to non already targeted border tiles
        zone.tiles.filter { it.owner == Owner.ME && it.units > 0 }
            .forEach { robot ->
                repeat(robot.units) {
                    robot.interestingTargets(zone)
                        .minByOrNull { n -> n.minDistOpp }
                        ?.let { target ->
                            actions.add(Action.Move(1, robot.pos, target.pos))
                            zone.alreadyTargeted.add(target.id)
                            robot.units--
                        }

                }
            }
    }

    private fun protect(zone: Zone, actions: MutableList<Action>) {
        for ((tile, units) in zone.tilesToProtect) {
            if (myMatter < 10) break
            if (!tile.shouldBeMine && units > 1) {
                actions.add(Action.Build(tile.pos))
                myMatter -= 10
            } else {
                actions.add(Action.Spawn(1, tile.pos))
                myMatter -= 10
            }
        }

    }

    private fun moves(zone: Zone, actions: MutableList<Action>) {

        // Move all other robots to the closet not mine tile
        val notMines = zone.tiles.filter { it.owner != Owner.ME && it.accessible }
        zone.tiles.filter { it.owner == Owner.ME && it.units > 0 }
            .forEach { robot ->
                repeat(robot.units) {
                    val target = if (zone.oppTileCount > 0) robot.neighbours.filter { n -> n.accessible }
                        .minByOrNull { n -> n.minDistOpp }
                    else notMines.minByOrNull { it.dist(robot) }
                    target?.let {
                        actions.add(Action.Move(1, robot.pos, target.pos))
                        zone.alreadyTargeted.add(target.id)
                        robot.units--
                    }
                }
            }


    }

    private fun spawns(zone: Zone, actions: MutableList<Action>) {
        val spawnTargets =
            zone.tiles.filter {
                it.canSpawn() && it.neighbours.any { n ->
                    n.accessible && n.owner != Owner.ME && n.id !in zone.alreadyTargeted
                }
            }
                .sortedBy { it.minDistOpp }
                .toMutableList()
        while (myMatter >= 10 && spawnTargets.isNotEmpty()) {
            spawnTargets.removeFirstOrNull()?.let { target ->
                actions.add(Action.Spawn(1, target.pos))
                myMatter -= 10
            }
        }
    }

    private fun updateBoard(input: Scanner) {
        board.update(input)
        if (turn == 0) {
            val myOrigin = board.myTiles.first { it.units == 0 }
            val oppOrigin = board.oppTiles.first { it.units == 0 }
            bfs.bfs(myOrigin.id) { current, bfsNode -> board[current].minDistMine = bfsNode.depth }
            bfs.bfs(oppOrigin.id) { current, bfsNode ->
                board.tiles[current].minDistOpp = bfsNode.depth
                board.tiles[current].shouldBeMine = board[current].minDistMine <= board[current].minDistOpp
            }
        }
        turn++
    }

    private fun computeZones(): List<Zone> {
        val zones = mutableListOf<Zone>()
        val visited = mutableListOf<Int>()

        board.tiles.forEach {
            if (!it.recycler && it.scrapAmount > 0 && it.id !in visited) {
                zones.add(getZone(it.id, visited))
            }
        }

        return zones
    }

    private fun getZone(tileId: Int, visited: MutableList<Int>): Zone {
        val zone = Zone()
        val toVisit = ArrayDeque<Int>().apply { add(tileId) }
        while (toVisit.isNotEmpty()) {
            val currentId = toVisit.removeFirst()
            if (currentId in visited) continue
            val current = board.tiles[currentId]
            zone.addTile(current)
            visited.add(currentId)
            current.neighbours.forEach { neighbor ->
                if (!neighbor.recycler && neighbor.scrapAmount > 0 && neighbor.id !in visited) {
                    toVisit.addLast(neighbor.id)
                }
            }
        }
        zone.computeInfos()
        return zone
    }

}
