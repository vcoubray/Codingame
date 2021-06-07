package test

import fr.vco.codingame.contest.springchallenge2021.IN_PROGRESS
import fr.vco.codingame.contest.springchallenge2021.log
import fr.vco.codingame.contest.springchallenge2021.mcts.State


fun main() {

    val game = initTestGame()

//    val stateBits = StateBits(game)
    val state = State().loadFromGame(game)

    log(state.getAvailableActions())
    log(state.getAvailableActionsNew())
//    val stateTmp = State().loadFromState(state).apply(::log)
//    while (stateTmp.getStatus() == IN_PROGRESS) {
//        val action = stateTmp.getAvailableActions().apply(::log).random().apply(::log)
//        stateTmp.play(action)
//        log(stateTmp)
//        log("---------")
//    }

    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuAction(state, 1000)
    simuActionNew(state, 1000)
    simuActionNew(state, 1000)
    simuActionNew(state, 1000)
    simuActionNew(state, 1000)
    simuActionNew(state, 1000)
    simuActionNew(state, 1000)
    simuActionNew(state, 1000)
    simuActionNew(state, 1000)
    simuActionRollout(state, 1000)
    simuActionRollout(state, 1000)
    simuActionRollout(state, 1000)
    simuActionRollout(state, 1000)
    simuActionRollout(state, 1000)
    simuActionRollout(state, 1000)
    simuActionRollout(state, 1000)
    simuActionRollout(state, 1000)



//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state2, 1000)
//    simuAction(state2, 1000)
//    simuAction(state2, 1000)
//    simuAction(state2, 1000)
//    simuAction(state2, 1000)
//    simuAction(state2, 1000)
//    simuActionNew(state2, 1000)
//    simuActionNew(state2, 1000)
//    simuActionNew(state2, 1000)
//    simuActionNew(state2, 1000)
//    simuActionNew(state2, 1000)




//    measureTimeMillis {
//        var stateBit = StateBits(game).apply(::log)
//        while (stateBit.getStatus() == IN_PROGRESS) {
//            val action = stateBit.getAvailableActions().apply(::log).random().apply(::log)
//            stateBit = stateBit.getNextState(action).apply(::log)
//        }
//    }.let { println("Bits - elapsed time : $it ms") }


//    var stateTemp = state.apply(::log)
//    while (stateTemp.getStatus() == IN_PROGRESS) {
//        val action = stateTemp.getAvailableActions().apply(::log).random().apply(::log)
////            val action = WaitAction(ME)
//        stateTemp = stateTemp.getNextState(action).apply(::log)
//    }
//
//
//    simuAction(stateBits, 1000)
//    simuAction(stateBits, 1000)
//    simuAction(stateBits, 1000)
//    simuAction(stateBits, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuAction(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//    simuActionOld(state, 1000)
//
//
//
//    simu(state, 500)
//    simu(state, 500)
//    simu(state, 500)
//    simu(state, 500)
//    simu(state, 500)
//    simu(state, 500)
//    simu(state, 500)
//    simu(state, 500)
//
//    simuRandom(state, 1)
//    simuRandom(state, 1)
//    simuRandom(state, 1)
//    simuRandom(state, 1)
//    simuRandom(state, 1)


}



