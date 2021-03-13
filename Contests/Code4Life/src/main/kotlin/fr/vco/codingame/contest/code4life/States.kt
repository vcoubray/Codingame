package fr.vco.codingame.contest.code4life


abstract class State(game: Game) {

    val availableMolecules: Molecules = game.availableMolecules
    val mySamples: List<Sample> = game.mySamples
    val oppSamples: List<Sample> = game.oppSamples
    val cloudSamples: List<Sample> = game.cloudSamples
    val me: Bot = game.me
    val opp: Bot = game.opp

    abstract fun action(): Action
}

class StartState(game: Game) : State(game) {
    override fun action() = GotoAction(Module.SAMPLES)
}

class MovingState(game: Game) : State(game) {
    override fun action() = MoveAction()
}

class SamplesState(game: Game) : State(game) {
    override fun action(): Action {
        /* TODO : Optimize rank selection */
        val rank = when {
            me.expertise.sum() > 10 -> 3
            me.expertise.sum() > 5 -> 2
            else -> 1
        }
        return if (mySamples.size < MAX_SAMPLES) SearchAction(rank)
        else GotoAction(Module.DIAGNOSIS)
    }

}

class DiagnosisState(game: Game) : State(game) {
    override fun action(): Action {
        /* TODO :
            - Keep Samples if several of them can be done in one time
            - check cloud for interesting samples
         */
        return if (mySamples.isEmpty()) GotoAction(Module.SAMPLES)
        else mySamples.firstOrNull { !it.isDiagnosed() }?.let(::SampleAction)
            ?: mySamples.firstOrNull { me.need(it).molecules.any { (_, m) -> m > 5 } }?.let(::SampleAction)
            ?: GotoAction(Module.MOLECULES)
    }
}

class MoleculesState(game: Game) : State(game) {
    override fun action(): Action {
        /*TODO :
           - Take molecules needed by opponent too in priority
           - Dont wait for molecule if opponent hold them
        */

        log("availables : $availableMolecules")
        mySamples.forEach{log(it.costs)}
        val needed = getBestSampleGroup(me,mySamples)?.cost
        log("Needed : $needed")
        val action = needed?.molecules?.keys?.firstOrNull { needed[it] > 0 }?.let { MoleculeAction(it) }


        return when {
            action != null -> action
            else -> GotoAction(Module.LABORATORY)
        }

    }

    fun getBestSampleGroup(bot: Bot, samples: List<Sample>): SampleGroup? {
        return samples.getAllCombinations(bot)
            .filter {
                val cost = it.cost
                availableMolecules.contains(cost) && (bot.storage + cost).isValid()
            }
//            .apply{
//                log("--")
//                forEach{ log("${it.cost} - ${it.health}") }
//
//            }
            .maxWith(  compareBy({it.health},{ -it.cost.sum()  }))

    }

}


class LaboratoryState(game: Game) : State(game) {
    override fun action(): Action {
        return when {
            /*
             TODO : Goto the diagnosis module directly if there is interesting samples
             */
            mySamples.any { me.canProduce(it) } -> mySamples.firstOrNull { me.canProduce(it) }!!.let(::SampleAction)
            mySamples.size <= 1 -> GotoAction(Module.SAMPLES)
            else -> GotoAction(Module.MOLECULES)
//            mySamples.isEmpty() -> GotoAction(Module.SAMPLES)
//            mySamples.none { me.canProduce(it) } -> GotoAction(Module.MOLECULES)
//            else -> mySamples.firstOrNull { me.canProduce(it) }?.let(::SampleAction) ?: WaitAction()
        }
    }
}
