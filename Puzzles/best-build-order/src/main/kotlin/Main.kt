import java.io.BufferedReader


fun main() {
    //<BAT_COUNT>
    //<BAT_NAME> <TIME> <COST>
    //<UNIT_COUNT>
    //<UNIT_NAME> <BAT_NAME> <TIME> <COST>
    //<Objective>
    val input: String = """
        1
        barrack worker 100 150
        4
        worker base 15 50
        space-soldier barrack 30 50
        heavy-soldier barrack 30 100
        medic barrack 45 75
        space-soldier 20
        """.trimIndent()

    val inputStream = input.byteInputStream()
    val reader = BufferedReader(inputStream.reader())
    val buildingCount = reader.readLine().toInt()
    val buildings = List(buildingCount) { reader.readLine() }
    val unitCount = reader.readLine().toInt()
    val units = List(unitCount) { reader.readLine() }
    val objectives = reader.readLine()

    println(buildings)
    println(units)

    val plans  = listOf(
        Plan(100, cost = Stock(minerals = 150, workers = 1), gain = Stock(barracks = 1, workers = 1)), // Barrack
        Plan(15, cost = Stock(minerals = 50, base = 1), gain = Stock(base = 1, workers = 1)), // Worker
        Plan(30, cost = Stock(minerals = 50, barracks = 1), gain = Stock(barracks = 1, spaceSoldiers = 1)), // Solider
        Plan(30, cost = Stock(minerals = 100, barracks = 1), gain = Stock(barracks = 1, heavies = 1)), // Heavy Solider
        Plan(45, cost = Stock(minerals = 75, barracks = 1), gain = Stock(barracks = 1, medics = 1)), // Medic
    )
}

data class Stock(
    val minerals: Int = 0, // 1023 - 10
    val base: Int = 0, // 1  - 1
    val barracks: Int = 0, // 31 - 5
    val workers: Int = 0, // 127  - 7 
    val spaceSoldiers: Int = 0, // 127 -7
    val heavies: Int = 0, // 127 - 7 
    val medics: Int = 0, // 127 - 7 
) {
    operator fun plus(stock: Stock) = Stock(
        minerals + stock.minerals,
        base + stock.base,
        barracks + stock.barracks,
        workers + stock.workers,
        spaceSoldiers + stock.spaceSoldiers,
        heavies + stock.heavies,
        medics + stock.medics
    )

    operator fun minus(stock: Stock) = Stock(
        minerals - stock.minerals,
        base - stock.base,
        barracks - stock.barracks,
        workers - stock.workers,
        spaceSoldiers - stock.spaceSoldiers,
        heavies - stock.heavies,
        medics - stock.medics
    )


    fun isBetter(stock: Stock): Boolean {
        return minerals > stock.minerals ||
            base > stock.base ||
            barracks > stock.barracks ||
            workers > stock.workers ||
            spaceSoldiers > stock.spaceSoldiers ||
            heavies > stock.heavies ||
            medics > stock.medics
    }
}

data class Plan(
    val time: Int,
    val cost: Stock,
    val gain: Stock,
)


class State(
    val time: Int,
    val stock: Stock,
    val plans: List<Plan>,
    val inProgress: Map<Int, List<Stock>> = emptyMap()
) {

    fun next(): List<State> {
        val newTime = this.time + 1
        
        var newStock = stock.copy(
            minerals = this.stock.minerals + this.stock.workers * 5
        )
        
        inProgress[newTime]?.forEach{
            newStock += it
        } 
            
        listOf(
            State(
                newTime,
                newStock,
                plans,
                inProgress.filter{(k,_)-> k > newTime}
            )
        )
        return emptyList()
    }
    
    fun possibleActions() {
        
    }
}


