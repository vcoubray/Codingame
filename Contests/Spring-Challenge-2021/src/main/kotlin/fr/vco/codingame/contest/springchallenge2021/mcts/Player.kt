package fr.vco.codingame.contest.springchallenge2021.mcts

data class Player(
    var score: Int = 0,
    var sun: Int = 0,
    var isWaiting: Boolean = false,
//    val costs: MutableList<Int> = MutableList(5) { 0 },
) {

    val costs: MutableList<Int> = MutableList(5) { 0 }

    fun initFromPlayer(player: Player) {
        this.score = player.score
        this.sun = player.sun
        this.isWaiting = player.isWaiting
        player.costs.forEachIndexed { i, it -> this.costs[i] = it }
    }

    fun canPay(actionId: Int) = sun >= (costs[actionId])

    fun calcScore() = score + sun / 3

    override fun toString(): String {
        return "sun : ${sun}, score: $score, isWaiting: $isWaiting, costs : $costs"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (score != other.score) return false
        if (sun != other.sun) return false
        if (isWaiting != other.isWaiting) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + sun
        result = 31 * result + isWaiting.hashCode()
        return result
    }

}
