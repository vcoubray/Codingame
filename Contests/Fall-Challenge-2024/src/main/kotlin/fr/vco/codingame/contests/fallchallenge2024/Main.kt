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

const val POD_COST = 1000
const val SPACEPORT_TYPE = 0


open class Building(val type: Int, val id: Int, val pos: Point)
class Spaceport(type: Int, id: Int, pos: Point, val astronauts: Map<Int, Int>) : Building(type, id, pos)
data class Route(val startId: Int, val endId: Int, val capacity: Int)
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

data class PotentialTube(val startId: Int, val endId: Int, val cost: Int)

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
                spaceport.astronauts.flatMap { (type, _) -> Game.buildings.filter { b -> b.type == type && Game.network[b.id].size < MAX_TUNNELS } }
            targets.map { PotentialTube(spaceport.id, it.id, Game.distances[spaceport.id][it.id].toInt() * 10) }
        }

        possibleTubes.sortedBy { it.cost }.forEach { (spaceportId, targetId, cost) ->
            if (Game.network[spaceportId].size < MAX_TUNNELS && Game.network[targetId].size < MAX_TUNNELS) {
                if (!Game.network.hasPath(spaceportId, targetId)) {

                    val route = Route(spaceportId, targetId, 1)
                    if (cost <= resources && Game.canBuild(route)) {
                        actions.add(Tube(Game.buildings[spaceportId], Game.buildings[targetId]))
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


