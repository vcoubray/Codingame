package fr.vco.codingame.contest.springchallenge2021

import kotlin.system.measureTimeMillis

//object Minimax {
//
//    fun getBestAction(rootIndex: Int) : String {
//        lateinit var action :String
//        measureTimeMillis {
//            action = NODES_POOL[rootIndex].children.maxByOrNull(::calcScore)?.let {
//                log("Node found : [$it] -> ${NODES_POOL[it]}")
//                NODES_POOL[it].action
//            } ?: "WAIT"
//        }.let{log("Minimax executed in $it ms")}
//        return action
//    }
//
//    fun calcScore(index: Int): Double {
//        return when {
//            NODES_POOL[index].children.isEmpty() -> NODES_POOL[index].nodeScore
//            NODES_POOL[index].myTurn -> NODES_POOL[index].children.map(::calcScore).maxOrNull()!!.toDouble()
//            !NODES_POOL[index].myTurn -> NODES_POOL[index].children.map(::calcScore).minOrNull()!!.toDouble()
//            else -> 0.0 // Should not happend
//        }
//
//    }
//
//}