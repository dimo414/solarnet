package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.api.GameSetup
import dev.martianzoo.tfm.api.SpecialClassNames.GAME
import dev.martianzoo.tfm.data.ChangeRecord.Cause
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn
import dev.martianzoo.tfm.pets.ast.Expression.Companion.expression
import dev.martianzoo.tfm.pets.ast.Instruction.Companion.instruction
import dev.martianzoo.tfm.pets.ast.classNames
import dev.martianzoo.tfm.types.PClass
import dev.martianzoo.tfm.types.PClassLoader
import dev.martianzoo.tfm.types.PType

/** Has functions for setting up new games and stuff. */
public object Engine {
  public fun loadClasses(setup: GameSetup): PClassLoader {
    val loader = PClassLoader(setup.authority, autoLoadDependencies = true)

    val classNames =
        listOf(GAME) +
        setup.allDefinitions().classNames() + // all cards etc.
        (1..setup.players).map { cn("Player$it" ) }

    loader.loadAll(classNames)
    loader.frozen = true
    return loader
  }

  public fun newGame(setup: GameSetup): Game {
    val loader = loadClasses(setup)
    val game = Game(setup, loader)

    game.execute(instruction("Game!"), initialCause = null, hidden = true).single()
    val cause = Cause.from(game.toComponent(expression("Game"))!!, 0)

    val singletons: List<PType> = singletons(loader.allClasses)
    game.executeAll(
        singletons.map { instruction("${it.expressionFull}!") },
        withEffects = true,
        cause = cause,
        hidden = true)
    return game
  }

  private fun singletons(all: Set<PClass>): List<PType> =
      all.filter { it.hasSingletonTypes() }.flatMap { it.baseType.concreteSubtypesSameClass() }
}
