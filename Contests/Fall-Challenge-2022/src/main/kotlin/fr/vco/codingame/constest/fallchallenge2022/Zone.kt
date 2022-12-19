package fr.vco.codingame.constest.fallchallenge2022

class Zone {
    var oppTileCount = 0
    var oppRobotCount = 0
    var myTileCount = 0
    var myRobotCount = 0
    val tiles = mutableListOf<Tile>()

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
            }

            else -> {
                //Do nothing
            }
        }
    }


}