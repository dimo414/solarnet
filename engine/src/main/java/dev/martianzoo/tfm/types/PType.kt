package dev.martianzoo.tfm.types

import dev.martianzoo.tfm.api.Type
import dev.martianzoo.tfm.pets.SpecialClassNames.CLASS
import dev.martianzoo.tfm.pets.ast.Requirement
import dev.martianzoo.tfm.pets.ast.Requirement.And
import dev.martianzoo.tfm.pets.ast.TypeExpr
import dev.martianzoo.tfm.types.Dependency.ClassDependency
import dev.martianzoo.tfm.types.Dependency.TypeDependency

/**
 * The translation of a [TypeExpr] into a "live" type, referencing actual [PClass]es loaded by a
 * [PClassLoader]. These are usually obtained by [PClassLoader.resolveType]. These can be abstract.
 * Usages of this type should be fairly unrelated to questions of whether instances exist in a game
 * state.
 */
public data class PType
private constructor(
    public val pclass: PClass,
    internal val allDependencies: DependencyMap = DependencyMap(),
    override val refinement: Requirement? = null,
) : Type {
  internal companion object {
    fun create(pclass: PClass, allDependenciesUnordered: DependencyMap = DependencyMap()) =
        PType(pclass, allDependenciesUnordered.extract(pclass.allDependencyKeys))
  }
  init {
    require(allDependencies.keys.toList() == pclass.allDependencyKeys.toList()) {
      "expected keys ${pclass.allDependencyKeys}, got $allDependencies"
    }
    if (pclass.className == CLASS) {
      require(allDependencies.types.single() is ClassDependency)
    } else {
      require(allDependencies.types.all { it is TypeDependency })
    }
    // if (refinement != null) pclass.loader.checkAllTypes(refinement) // TODO SOE
  }
  override val abstract = pclass.abstract || allDependencies.abstract || refinement != null

  override fun isSubtypeOf(that: Type) =
      pclass.isSubclassOf((that as PType).pclass) &&
      allDependencies.specializes(that.allDependencies) &&
      that.refinement in setOf(null, refinement)

  fun intersect(that: PType): PType? {
    val intersect: PClass = pclass.intersect(that.pclass) ?: return null
    return create(intersect, allDependencies.intersect(that.allDependencies))
        .refine(combine(this.refinement, that.refinement))
  }

  private fun combine(one: Requirement?, two: Requirement?): Requirement? {
    val x = setOfNotNull(one, two)
    return when (x.size) {
      0 -> null
      1 -> x.first()
      2 -> And(x.toList())
      else -> error("imposserous")
    }
  }

  fun refine(newRef: Requirement?): PType = copy(refinement = combine(refinement, newRef))

  override val typeExpr: TypeExpr by lazy {
    toTypeExprUsingSpecs(narrowedDependencies.types.map { it.typeExpr })
  }

  override val typeExprFull: TypeExpr by lazy {
    toTypeExprUsingSpecs(allDependencies.types.map { it.typeExprFull })
  }

  internal val narrowedDependencies: DependencyMap by lazy {
    allDependencies - pclass.baseType.allDependencies
  }

  private fun toTypeExprUsingSpecs(specs: List<TypeExpr>): TypeExpr {
    val typeExpr = pclass.className.addArgs(specs).refine(refinement)
    val roundTrip = pclass.loader.resolveType(typeExpr)
    require(roundTrip == this) { "$this -> $typeExpr -> $roundTrip" }
    return typeExpr
  }

  fun supertypes(): List<PType> {
    val supers = pclass.allSuperclasses - pclass.loader.componentClass - pclass
    return supers.map { PType(it, allDependencies.extract(it.allDependencyKeys)) }
  }

  override fun toString() = typeExprFull.toString()
}
