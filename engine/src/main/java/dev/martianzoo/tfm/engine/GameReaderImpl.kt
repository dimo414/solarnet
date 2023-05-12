package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.api.Authority
import dev.martianzoo.tfm.api.Type
import dev.martianzoo.tfm.api.TypeInfo
import dev.martianzoo.tfm.engine.Component.Companion.toComponent
import dev.martianzoo.tfm.engine.Game.ComponentGraph
import dev.martianzoo.tfm.engine.Game.SnReader
import dev.martianzoo.tfm.pets.ast.Expression
import dev.martianzoo.tfm.pets.ast.Metric
import dev.martianzoo.tfm.pets.ast.Metric.Count
import dev.martianzoo.tfm.pets.ast.Metric.Plus
import dev.martianzoo.tfm.pets.ast.Metric.Scaled
import dev.martianzoo.tfm.pets.ast.Requirement
import dev.martianzoo.tfm.pets.ast.Requirement.And
import dev.martianzoo.tfm.pets.ast.Requirement.Counting
import dev.martianzoo.tfm.pets.ast.Requirement.Exact
import dev.martianzoo.tfm.pets.ast.Requirement.Max
import dev.martianzoo.tfm.pets.ast.Requirement.Min
import dev.martianzoo.tfm.pets.ast.Requirement.Or
import dev.martianzoo.tfm.pets.ast.ScaledExpression.Scalar.ActualScalar
import dev.martianzoo.tfm.types.MClassTable
import dev.martianzoo.tfm.types.Transformers
import kotlin.math.min

internal class GameReaderImpl(val table: MClassTable, val components: ComponentGraph) :
    SnReader, TypeInfo {
  override val authority: Authority by table::authority
  override val transformers: Transformers by table::transformers

  override fun isAbstract(e: Expression) = resolve(e).abstract

  override fun ensureNarrows(wide: Expression, narrow: Expression) =
      resolve(narrow).ensureNarrows(resolve(wide), this)

  override fun resolve(expression: Expression) = table.resolve(expression)

  override fun evaluate(requirement: Requirement): Boolean =
      when (requirement) {
        is Counting -> {
          val actual = count(Count(requirement.scaledEx.expression))
          val target = (requirement.scaledEx.scalar as ActualScalar).value
          when (requirement) {
            is Min -> actual >= target
            is Max -> actual <= target
            is Exact -> actual == target
          }
        }
        is Or -> requirement.requirements.any { evaluate(it) }
        is And -> requirement.requirements.all { evaluate(it) }
        is Requirement.Transform -> error("should have been transformed by now: $requirement")
      }

  override fun count(metric: Metric): Int =
      when (metric) {
        is Count -> components.count(resolve(metric.expression), this)
        is Scaled -> count(metric.metric) / metric.unit
        is Metric.Max -> min(count(metric.metric), metric.maximum)
        is Plus -> metric.metrics.sumOf(::count)
        is Metric.Transform -> error("should have been transformed by now: $metric")
      }

  override fun count(type: Type) = components.count(table.resolve(type), this)

  override fun countComponent(concreteType: Type) = countComponent(concreteType.toComponent(this))

  override fun countComponent(component: Component) = components.countComponent(component)

  override fun getComponents(type: Type) =
      components.getAll(table.resolve(type), this).map { it.mtype }
}
