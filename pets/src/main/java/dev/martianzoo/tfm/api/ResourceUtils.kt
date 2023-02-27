package dev.martianzoo.tfm.api

import dev.martianzoo.tfm.api.SpecialClassNames.CLASS
import dev.martianzoo.tfm.api.SpecialClassNames.MEGACREDIT
import dev.martianzoo.tfm.api.SpecialClassNames.PRODUCTION
import dev.martianzoo.tfm.api.SpecialClassNames.STANDARD_RESOURCE
import dev.martianzoo.tfm.pets.ast.ClassName
import dev.martianzoo.tfm.pets.ast.TypeExpr

// Note: this was easier to test in .engine than anywhere near here (ApiHelpersTest)

/**
 * Simple helper functions relating to standard resources, mostly for use by custom instructions.
 */
object ResourceUtils {
  /**
   * Returns a map with six entries, giving [player]'s current production levels, adjusting
   * megacredit product to account for our horrible hack.
   */
  fun lookUpProductionLevels(game: ReadOnlyGameState, player: TypeExpr): Map<ClassName, Int> =
      standardResourceNames(game).associateWith {
        val type = game.resolveType(PRODUCTION.addArgs(player, CLASS.addArgs(it)))
        val rawCount = game.countComponents(type)
        if (it == MEGACREDIT) {
          rawCount - 5
        } else {
          rawCount
        }
      }

  /** Returns the name of every concrete class of type `StandardResource`. */
  fun standardResourceNames(game: ReadOnlyGameState): Set<ClassName> =
      game.getComponents(game.resolveType(CLASS.addArgs(STANDARD_RESOURCE)))
          .map { it.typeExpr.arguments.single().className }
          .toSet()
}
