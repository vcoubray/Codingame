package fr.vco.codingame.contests.fallchallenge2024


import kotlin.math.sign
import kotlin.math.sqrt

fun readlnInt() = readln().toInt()
fun readlnInts() = readln().split(" ").map { it.toInt() }


fun Int.pow2() = this * this

data class Point(val x: Int, val y: Int) {
    fun distance(p: Point) = sqrt(((x - p.x).pow2() + (y - p.y).pow2()).toDouble())
}


const val MAX_BUILDINGS = 150
const val MAX_TUNNELS = 4

const val SPACEPORT_TYPE = 0
const val POD_COST = 1000
const val TELEPORT_COST = 5000


open class Building(val type: Int, val id: Int, val pos: Point, var hasTeleport: Boolean = false)
class Spaceport(type: Int, id: Int, pos: Point, val astronauts: Map<Int, Int>) : Building(type, id, pos)
data class Route(val startId: Int, val endId: Int, var capacity: Int)
class Pod(val id: Int, val path: List<Int>)


fun isOnRoute(point: Point, s1: Point, s2: Point): Boolean {
    val EPSILON = 0.0000001
    return (s1.distance(point) + s2.distance(point) - s1.distance(s2) in -EPSILON..EPSILON)
}

fun orientation(p1: Point, p2: Point, p3: Point): Int {
    val prod = (p3.y - p1.y) * (p2.x - p1.x) - (p2.y - p1.y) * (p3.x - p1.x)
    return prod.sign
}

fun segmentsIntersect(a: Point, b: Point, c: Point, d: Point): Boolean {
    return orientation(a, b, c) * orientation(a, b, d) < 0 && orientation(c, d, a) * orientation(c, d, b) < 0
}

object Game {
    var resources = 0
    lateinit var routes: MutableList<Route>
    lateinit var network: List<MutableList<Int>>
    lateinit var pods: MutableList<Pod>
    val buildings = mutableListOf<Building>()
    val spaceports = mutableListOf<Spaceport>()

    val distances = List(MAX_BUILDINGS) { MutableList(MAX_BUILDINGS) { -1.0 } }

    fun update() {
        // Update Resources
        resources = readlnInt()
        System.err.println("Start Resources : $resources")

        // Update Routes
        network = List(MAX_BUILDINGS) { mutableListOf() }
        val numTravelRoutes = readlnInt()
        routes = MutableList(numTravelRoutes) {
            val (buildingId1, buildingId2, capacity) = readlnInts()
            network[buildingId1].add(buildingId2)
            network[buildingId2].add(buildingId1)
            Route(buildingId1, buildingId2, capacity)
        }

        // Update pods
        val numPods = readlnInt()
        pods = MutableList(numPods) {
            val podProperties = readlnInts()
            val podId = podProperties.first()
            val path = podProperties.drop(2)
            Pod(podId, path)
        }

        // Update buildings 
        val numNewBuildings = readlnInt()
        repeat(numNewBuildings) {
            val buildingProperties = readlnInts()
            val (type, id, x, y) = buildingProperties

            if (type == SPACEPORT_TYPE) {
                val astronauts =
                    buildingProperties.drop(4).groupBy { it }.map { (type, astronauts) -> type to astronauts.size }
                        .toMap()
                val building = Spaceport(type, id, Point(x, y), astronauts)
                spaceports.add(building)
                buildings.add(building)
            } else {
                buildings.add(Building(type, id, Point(x, y)))
            }

            for (i in 0..id) {
                val dist = buildings[id].pos.distance(buildings[i].pos)
                distances[id][i] = dist
                distances[i][id] = dist
            }
        }

    }


    fun canBuild(route: Route): Boolean {
        for (b in buildings) {
            if (b.id == route.startId || b.id == route.endId) continue
            if (isOnRoute(buildings[b.id].pos, buildings[route.startId].pos, buildings[route.endId].pos)) return false
        }
        for (r in routes) {
            if (r.startId == route.startId || r.startId == route.endId || r.endId == route.startId || r.endId == route.endId) continue
            if (segmentsIntersect(
                    buildings[r.startId].pos,
                    buildings[r.endId].pos,
                    buildings[route.startId].pos,
                    buildings[route.endId].pos
                )
            ) {
                return false
            }
        }
        return true
    }

    fun hasPathForType(start: Int, type: Int): Boolean {
        val toVisit = ArrayDeque<Int>().apply { add(start) }
        val visited = BooleanArray(network.size) { false }
        visited[start] = true

        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()

            for (n in network[current].filterNot { visited[it] }) {
                if (buildings[n].type == type) return true
                visited[n] = true
                toVisit.add(n)
            }
        }
        return false
    }

}

fun List<List<Int>>.hasPath(start: Int, destination: Int): Boolean {
    val toVisit = ArrayDeque<Int>().apply { add(start) }
    val visited = BooleanArray(size) { false }
    visited[start] = true

    while (toVisit.isNotEmpty()) {
        val current = toVisit.removeFirst()

        for (n in this[current].filterNot { visited[it] }) {
            if (n == destination) return true
            visited[n] = true
            toVisit.add(n)
        }
    }
    return false
}


data class PotentialTube(val startId: Int, val endId: Int, val cost: Int, val isTeleport: Boolean = false)

fun main() {

    val podlessRoutes = ArrayDeque<Route>()

    while (true) {
        Game.update()
        var resources = Game.resources

        val actions = mutableListOf<Action>()

        while (podlessRoutes.isNotEmpty() && POD_COST <= resources) {
            val route = podlessRoutes.removeFirst()
            val pod = Pod(Game.pods.size, listOf(route.startId, route.endId, route.startId))
            actions.add(AddPod(pod))
            Game.pods.add(pod)
            resources -= POD_COST
        }

        val possibleTubes = Game.spaceports.flatMap { spaceport ->
            val targets =
                spaceport.astronauts.flatMap { (type, _) -> Game.buildings.filter { b -> b.type == type } }
            targets.flatMap {
                listOf(
                    PotentialTube(spaceport.id, it.id, Game.distances[spaceport.id][it.id].toInt() * 10),
                    PotentialTube(spaceport.id, it.id, TELEPORT_COST, true)
                )
            }
        }

        possibleTubes.sortedBy { if (it.isTeleport) it.cost else it.cost + POD_COST }
            .forEach { (spaceportId, targetId, cost, isTeleport) ->
                val spaceport = Game.buildings[spaceportId]
                val target = Game.buildings[targetId]

                if (!Game.network.hasPath(spaceportId, targetId) &&
                    !Game.hasPathForType(spaceportId, Game.buildings[targetId].type)
                ) {

                    val route = Route(spaceportId, targetId, 1)
                    if (isTeleport) {
                        val canBuildTeleport = !spaceport.hasTeleport && !target.hasTeleport && cost <= resources
                        if (canBuildTeleport) {
                            actions.add(Teleport(spaceport, target))
                            Game.network[spaceportId].add(targetId)
                            Game.network[targetId].add(spaceportId)
                            spaceport.hasTeleport = true
                            target.hasTeleport = true
                            route.capacity = 0
                            Game.routes.add(route)
                            resources -= cost
                        }
                    } else {
                        val spacePortTunnels = Game.network[spaceportId].size - if (spaceport.hasTeleport) 1 else 0
                        val targetTunnels = Game.network[targetId].size - if (target.hasTeleport) 1 else 0

                        val canBuildTunnel =
                            Game.canBuild(route) && spacePortTunnels < MAX_TUNNELS && targetTunnels < MAX_TUNNELS

                        if (cost <= resources && canBuildTunnel) {
                            actions.add(Tube(spaceport, Game.buildings[targetId]))
                            Game.network[spaceportId].add(targetId)
                            Game.network[targetId].add(spaceportId)
                            Game.routes.add(route)
                            resources -= cost

                            if (POD_COST <= resources) {
                                val pod = Pod(Game.pods.size, listOf(spaceportId, targetId, spaceportId))
                                actions.add(AddPod(pod))
                                Game.pods.add(pod)
                                resources -= POD_COST
                            } else {
                                podlessRoutes.addLast(route)
                            }
                        }
                    }

                }
            }

        System.err.println("resources = $resources")
        println(actions.takeIf { it.isNotEmpty() }?.joinToString(";") { it.play() } ?: Wait.play())
    }
}

sealed interface Action {
    fun play(): String
}

object Wait : Action {
    override fun play() = "WAIT"
}

class Tube(val start: Building, val end: Building) : Action {
    override fun play() = "TUBE ${start.id} ${end.id}"
}

class AddPod(val pod: Pod) : Action {
    override fun play() = "POD ${pod.id} ${pod.path.joinToString(" ")}"
}

class Teleport(val start: Building, val end: Building) : Action {
    override fun play() = "TELEPORT ${start.id} ${end.id}"
}

class DestroyPod(val pod: Pod) : Action {
    override fun play() = "DESTROY ${pod.id}"
}


