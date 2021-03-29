package fr.vco.codingame.contest.code4life


abstract class State(game: Game) {

    val availableMolecules: Molecules = game.availableMolecules
    val mySamples: List<Sample> = game.mySamples
    val oppSamples: List<Sample> = game.oppSamples
    val cloudSamples: List<Sample> = game.cloudSamples
    val me: Bot = game.me
    val opp: Bot = game.opp

    abstract fun action(): Action

    fun getBestSampleGroup(bot: Bot, samples: List<Sample>): SampleGroup? {
        return samples.filter{it.isDiagnosed()}.getAllCombinations(bot)
            .filter {
                val cost = it.cost
                availableMolecules.contains(cost) && (bot.storage + cost).isValid()
            }
            .maxWith(compareBy({ it.health }, { -it.cost.sum() }))
    }
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
            me.expertise.sum() > 15 -> 3
            me.expertise.sum() > 10 -> 2
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


        val notDiagnosed = mySamples.filter { !it.isDiagnosed() }
        val notMakable =  mySamples.filter { me.need(it).molecules.any { (_, m) -> m > 5 } }
        val cost = getBestSampleGroup(me, mySamples)?.cost?.sum()

        return when {
            mySamples.isEmpty() -> GotoAction(Module.SAMPLES)
            notDiagnosed.isNotEmpty() -> SampleAction(notDiagnosed.first())
            notMakable.isNotEmpty() ->  SampleAction(notMakable.first())
            cost == 0 -> GotoAction(Module.LABORATORY)
            else -> GotoAction(Module.MOLECULES)
        }
    }
}

class MoleculesState(game: Game) : State(game) {
    override fun action(): Action {
        /*TODO :
           - Take molecules needed by opponent too in priority
           - Dont wait for molecule if opponent hold them
        */

        log("Availables : $availableMolecules")
        mySamples.forEach { log(it.costs) }
        val oppNeeded = getBestSampleGroup(opp, oppSamples)?.cost?: emptyMolecules
        val needed = getBestSampleGroup(me, mySamples)?.cost?: emptyMolecules

        val moleculesScore = moleculeType.map {
            when {
                oppNeeded.contains(it)  && needed.contains(it) -> it to 2
                needed.contains(it) -> it to 2
                oppNeeded.contains(it) -> it to 1
                else -> it to 0
            }
        }

        log("Needed : $needed")
        val action = moleculesScore.filter{(_,score) -> score> 0}.maxBy { (_,score)-> score }?.let { MoleculeAction(it.first) }
        return when {
            me.storage.sum() >=10 -> GotoAction(Module.LABORATORY)
            action != null -> action
            else -> GotoAction(Module.LABORATORY)
        }

    }


}


class LaboratoryState(game: Game) : State(game) {
    override fun action(): Action {
        /*
         TODO : Goto the diagnosis module directly if there is interesting samples
         */



        return when {
            mySamples.any { me.canProduce(it) } -> mySamples.firstOrNull { me.canProduce(it) }!!.let(::SampleAction)
            mySamples.size <= 1 -> GotoAction(Module.SAMPLES)
            else -> GotoAction(Module.MOLECULES)
//            mySamples.isEmpty() -> GotoAction(Module.SAMPLES)
//            mySamples.none { me.canProduce(it) } -> GotoAction(Module.MOLECULES)
//            else -> mySamples.firstOrNull { me.canProduce(it) }?.let(::SampleAction) ?: WaitAction()
        }
    }
}
