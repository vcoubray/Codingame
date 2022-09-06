package fr.vco.codingame.puzzles.mars.lander.engine


data class Surface(
    val height: Int,
    val width: Int,
    val segments: List<Segment>
) {

    var distanceMax: Double = 0.0
    val widthRange = 0.0..width.toDouble()
    val heightRange = 0.0..height.toDouble()


    init {

        val landingZoneIndex = segments.indexOfFirst { it.start.y == it.end.y }
        segments[landingZoneIndex].isLandingZone = true

        var sum = 0.0
        for (i in landingZoneIndex - 1 downTo 0) {
            segments[i].distanceToLanding = sum
            segments[i].proportion = { segment, x -> (x - segment.end.x) / (segment.start.x - segment.end.x) }
            sum += segments[i].length

        }
        distanceMax = sum
        sum = 0.0
        for (i in landingZoneIndex + 1 until segments.size) {
            segments[i].distanceToLanding = sum
            segments[i].proportion = { segment, x -> (x - segment.start.x) / (segment.end.x - segment.start.x) }
            sum += segments[i].length
        }

        if (sum > distanceMax) {
            distanceMax = sum
        }

    }

    fun cross(p1x: Double, p1y: Double, p2x: Double, p2y: Double): Double {
        for (segment in segments) {
            segment.cross(p1x, p1y, p2x, p2y)?.let {
                return segment.distanceToLanding(it.x)
            }
        }
        return -1.0
    }
}
