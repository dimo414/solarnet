package dev.martianzoo.tfm.types

import dev.martianzoo.tfm.data.ClassDeclaration.DefaultsDeclaration
import dev.martianzoo.tfm.pets.ast.Instruction.Intensity
import dev.martianzoo.tfm.pets.ast.TypeExpr
import dev.martianzoo.tfm.types.Dependency.TypeDependency

internal class Defaults(
    // These DMs will always contain TypeDependencies
    val allCasesDependencies: DependencyMap = DependencyMap(),
    val gainOnlyDependencies: DependencyMap = DependencyMap(),
    val gainIntensity: Intensity? = null,
) {

  override fun toString() =
      "{ALL $allCasesDependencies GAIN $gainOnlyDependencies INTENS $gainIntensity}"

  companion object {
    fun from(d: DefaultsDeclaration, pclass: PClass, loader: PClassLoader): Defaults {
      fun PClass.toDependencyMap(specs: List<TypeExpr>?): DependencyMap =
          specs?.let { loader.resolve(name.addArgs(it)).dependencies } ?: DependencyMap()

      return Defaults(
          allCasesDependencies = pclass.toDependencyMap(d.universalSpecs),
          gainOnlyDependencies = pclass.toDependencyMap(d.gainOnlySpecs),
          gainIntensity = d.gainIntensity,
      )
    }
  }

  // Return a DefaultsDeclaration that uses the information from *this* one if present,
  // but otherwise attempt to find agreement among all of `defaultses`.
  fun overlayOn(defaultses: List<Defaults>): Defaults {
    return Defaults(
        overlayDMs(defaultses) { it.allCasesDependencies },
        overlayDMs(defaultses) { it.gainOnlyDependencies },
        overlayIntensities(defaultses) { it.gainIntensity })
  }

  private fun overlayIntensities(
      defaultses: List<Defaults>,
      extract: (Defaults) -> Intensity?,
  ): Intensity? {
    val override = extract(this)
    if (override != null) return override

    val intensities = defaultses.mapNotNull(extract)
    return when (intensities.size) {
      0 -> null
      1 -> intensities.first()
      else -> error("conflicting intensities: $intensities")
    }
  }

  private fun overlayDMs(
      defaultses: List<Defaults>,
      extract: (Defaults) -> DependencyMap,
  ): DependencyMap {
    val defMap = mutableMapOf<Dependency.Key, TypeDependency>()
    extract(this).keys.associateWithTo(defMap) { extract(this)[it]!! as TypeDependency }
    for (key in defaultses.flatMap { extract(it).keys }.toSet()) {
      if (key !in defMap) {
        // TODO some orders might work when others don't
        val depMapsWithThisKey = defaultses.map(extract).filter { key in it }
        defMap[key] =
            depMapsWithThisKey
                .map { it[key]!! as TypeDependency }
                .reduce { a, b -> a.intersect(b)!! }
      }
    }
    return DependencyMap(defMap)
  }
}
