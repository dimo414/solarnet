package dev.martianzoo.tfm.types

import dev.martianzoo.tfm.api.TypeInfo
import dev.martianzoo.tfm.pets.ast.Requirement
import dev.martianzoo.tfm.pets.ast.TypeExpression
import dev.martianzoo.tfm.pets.ast.TypeExpression.ClassLiteral
import dev.martianzoo.tfm.pets.ast.TypeExpression.GenericTypeExpression

interface PType : TypeInfo {
  val pclass: PClass // TODO should this really be shared?
  val dependencies: DependencyMap
  val refinement: Requirement?

  fun isSubtypeOf(that: PType): Boolean

  infix fun intersect(that: PType): PType?

  override fun toTypeExpression(): TypeExpression

  data class ClassPType(override val pclass: PClass) : PType {
    override val dependencies = DependencyMap()
    override val refinement = null

    override val abstract by pclass::abstract

    override fun isSubtypeOf(that: PType) =
        that is ClassPType && this.pclass.isSubclassOf(that.pclass)

    override fun intersect(that: PType): PType? {
      if (that !is ClassPType) return null
      val inter = (this.pclass intersect that.pclass) ?: return null
      return ClassPType(inter)
    }

    override fun toTypeExpression() = ClassLiteral(pclass.name)
    override fun toString() = toTypeExpression().toString()
  }

  data class GenericPType(
      override val pclass: PClass,
      override val dependencies: DependencyMap,
      override val refinement: Requirement?,
  ) : PType {
    override val abstract: Boolean =
        pclass.abstract || dependencies.abstract || refinement != null

    override fun isSubtypeOf(that: PType) =
        that is GenericPType &&
            pclass.isSubclassOf(that.pclass) &&
            dependencies.specializes(that.dependencies) &&
            that.refinement in setOf(null, refinement)

    override fun intersect(that: PType): GenericPType? {
      val intersect: PClass = pclass.intersect(that.pclass) ?: return null
      return GenericPType(
          intersect,
          dependencies.intersect(that.dependencies),
          combine(this.refinement, that.refinement))
    }

    private fun combine(one: Requirement?, two: Requirement?): Requirement? {
      val x = setOfNotNull(one, two)
      return when (x.size) {
        0 -> null
        1 -> x.first()
        2 -> Requirement.And(x.toList())
        else -> error("imposserous")
      }
    }

    fun specialize(specs: List<PType>): GenericPType {
      return copy(dependencies = dependencies.specialize(specs))
    }

    override fun toTypeExpression(): GenericTypeExpression {
      val specs = dependencies.types.map { it.toTypeExpressionFull() }
      return pclass.name.addArgs(specs).refine(refinement)
    }
    override fun toString() = toTypeExpression().toString()
  }
}
