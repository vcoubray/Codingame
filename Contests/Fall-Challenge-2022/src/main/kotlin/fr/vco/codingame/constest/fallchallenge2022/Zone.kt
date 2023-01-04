package fr.vco.codingame.constest.fallchallenge2022

class Zone {
    var oppTileCount = 0
    var oppRobotCount = 0
    var myTileCount = 0
    var myRobotCount = 0
    val tiles = mutableListOf<Tile>()


    val alreadyTargeted = mutableListOf<Int>()
    val tilesToProtect = mutableListOf<Pair<Tile, Int>>()

    var canDoThings = false
    var inConflict = false
    var shouldSpawn = false

    fun addTile(tile: Tile) {
        tiles.add(tile)
        when (tile.owner) {
            Owner.OPP -> {
                oppTileCount++
                oppRobotCount += tile.units
            }

            Owner.ME -> {
                myTileCount++
                myRobotCount += tile.units
                val oppUnits = tile.neighbours.sumOf { if (it.owner == Owner.OPP) it.units else 0 }
                if (oppUnits > 0 && tile.accessible) tilesToProtect.add(tile to oppUnits)
            }

            else -> {
                //Do nothing
            }
        }
    }

    fun computeInfos() {
        canDoThings = myTileCount > 0 && myTileCount != tiles.size
        inConflict = myTileCount > 0 && oppTileCount > 0
        shouldSpawn = myRobotCount == 0 || oppRobotCount > 0

    }
}
