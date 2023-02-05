package dev.martianzoo.tfm.api

import dev.martianzoo.tfm.data.ChangeLogEntry.Cause
import dev.martianzoo.tfm.data.MarsMapDefinition
import dev.martianzoo.tfm.pets.ast.Requirement
import dev.martianzoo.tfm.pets.ast.TypeExpr
import dev.martianzoo.util.Multiset

/** A game engine implements this interface so that [CustomInstruction]s can speak to it. */
interface GameState : ReadOnlyGameState {
  fun applyChange(
      count: Int = 1,
      removing: TypeExpr? = null,
      gaining: TypeExpr? = null,
      cause: Cause? = null,
      amap: Boolean = false,
  )
}

/** The read-only portions of [GameState]. */
interface ReadOnlyGameState {
  val setup: GameSetup

  val authority: Authority
    get() = setup.authority

  val map: MarsMapDefinition
    get() = setup.map

  fun countComponents(typeExpr: TypeExpr): Int

  fun getComponents(typeExpr: TypeExpr): Multiset<TypeExpr>

  fun isMet(requirement: Requirement): Boolean
}
