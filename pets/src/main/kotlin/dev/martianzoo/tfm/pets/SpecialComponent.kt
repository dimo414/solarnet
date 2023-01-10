package dev.martianzoo.tfm.pets

import dev.martianzoo.tfm.pets.ast.TypeExpression.ClassExpression
import dev.martianzoo.tfm.pets.ast.TypeExpression.Companion.gte
import dev.martianzoo.tfm.pets.ast.TypeExpression.GenericTypeExpression

enum class SpecialComponent {
  Component,
  Default,
  End,
  Ok,
  Player,
  Production,
  StandardResource,
  This,
  UseAction,
  ;

  val type: GenericTypeExpression = gte(name)
  val classEx: ClassExpression = ClassExpression(name)
}
