package fr.vco.codingame.contest.springchallenge2021.mcts

data class Player(
    var score: Int = 0,
    var sun: Int = 0,
    var isWaiting: Boolean = false,
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

}
