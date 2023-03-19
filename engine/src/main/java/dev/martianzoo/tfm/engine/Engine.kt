package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.api.GameSetup
import dev.martianzoo.tfm.api.SpecialClassNames.ENGINE
import dev.martianzoo.tfm.data.Actor
import dev.martianzoo.tfm.data.GameEvent.ChangeEvent.Cause
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn
import dev.martianzoo.tfm.pets.ast.Instruction
import dev.martianzoo.tfm.pets.ast.Instruction.Companion.instruction
import dev.martianzoo.tfm.pets.ast.classNames
import dev.martianzoo.tfm.types.MClassLoader

/** Has functions for setting up new games and stuff. */
public object Engine {
  public fun loadClasses(setup: GameSetup): MClassLoader {
    val loader = MClassLoader(setup.authority, autoLoadDependencies = true)

    val classNames =
        listOf(ENGINE) +
            setup.allDefinitions().classNames() + // all cards etc.
            setup.players().classNames()

    loader.loadAll(classNames)
    if ("P" in setup.bundles) loader.load(cn("PreludePhase"))

    loader.frozen = true
    return loader
  }

  public fun newGame(setup: GameSetup): Game {
    val loader = loadClasses(setup)

    // TODO get @createSingletons to work as a real CustomInstruction
    // setup.authority.customInstructions += customInstr(loader)

    val game = Game(setup, loader)
    val agent = game.agent(Actor.ENGINE)

    val result: Result = agent.initiate(instruction("$ENGINE!"))
    require(result.newTaskIdsAdded.none())
    require(game.taskQueue.isEmpty())

    val fakeCause = Cause(Actor.ENGINE, result.changes.first())

    singletonCreateInstructions(loader).forEach {
      agent.initiate(it, fakeCause)
      require(game.taskQueue.isEmpty()) { "Unexpected tasks: ${game.taskQueue}" }
    }
    return game
  }

  fun singletonCreateInstructions(loader: MClassLoader): List<Instruction> {
    val singletonTypes =
        loader.allClasses
            .filter { it.hasSingletonTypes() }
            .flatMap { it.baseType.concreteSubtypesSameClass() }
    return singletonTypes.map { instruction("${it.expressionFull}!") }
  }
}
