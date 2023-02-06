package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.engine.LiveNodes.LiveEffect
import dev.martianzoo.tfm.pets.AstTransforms
import dev.martianzoo.tfm.pets.SpecialClassNames.THIS
import dev.martianzoo.tfm.types.Dependency.TypeDependency
import dev.martianzoo.tfm.types.PType
import dev.martianzoo.util.toSetStrict

/** An instance of a concrete PType; a game state is made up of a multiset of these. */
public data class Component(private val ptype: PType) {
  init {
    require(!ptype.abstract) { "Component can't be of an abstract type: ${ptype.toTypeExprFull()}" }
  }

  public fun hasType(thatType: PType) = ptype.isSubtypeOf(thatType)

  public val asTypeExpr = ptype.toTypeExprMinimal()

  public val dependencies: Set<Component> by lazy {
    if (ptype.isClassType) {
      setOf()
    } else {
      ptype.allDependencies.types.map { Component((it as TypeDependency).ptype) }.toSetStrict()
    }
  }

  internal fun effects(game: Game): Set<LiveEffect> {
    val classFx = ptype.pclass.classEffects
    return classFx.map {
      var fx = it
      fx = AstTransforms.replaceTypes(fx, THIS.type, ptype.toTypeExprMinimal())
      // specialize for deps... owner...
      LiveNodes.from(fx, game)
    }.toSetStrict()
  }
  override fun toString() = "[$ptype]"

  fun describe(): String {
    return """
      TODO
    """.trimIndent()
  }
}
