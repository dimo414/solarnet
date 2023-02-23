package dev.martianzoo.tfm.types

import dev.martianzoo.tfm.data.ClassDeclaration.DefaultsDeclaration
import dev.martianzoo.tfm.pets.ast.Instruction.Intensity
import dev.martianzoo.tfm.pets.ast.TypeExpr

internal class Defaults(
    // These DMs will always contain TypeDependencies
    val allCasesDependencies: DependencyMap = DependencyMap(),
    val gainOnlyDependencies: DependencyMap = DependencyMap(),
    val gainIntensity: Intensity? = null,
    val removeOnlyDependencies: DependencyMap = DependencyMap(),
    val removeIntensity: Intensity? = null,
) {
  companion object {
    fun from(d: DefaultsDeclaration, pclass: PClass, loader: PClassLoader): Defaults {
      // TypeExpr/Type? TODO
      fun PClass.toDependencyMap(specs: List<TypeExpr>): DependencyMap =
          loader.resolveType(className.addArgs(specs)).narrowedDependencies

      return Defaults(
          allCasesDependencies = pclass.toDependencyMap(d.universalSpecs),
          gainOnlyDependencies = pclass.toDependencyMap(d.gainOnlySpecs),
          gainIntensity = d.gainIntensity,
          removeOnlyDependencies = pclass.toDependencyMap(d.removeOnlySpecs),
          removeIntensity = d.removeIntensity,
      )
    }
  }

  // Return a DefaultsDeclaration that uses the information from *this* one if present,
  // but otherwise attempt to find agreement among all of `defaultses`.
  fun overlayOn(defaultses: List<Defaults>): Defaults {
    return Defaults(
        allCasesDependencies,
        overlayDMs(gainOnlyDependencies, defaultses.map { it.gainOnlyDependencies }),
        overlayIntensities(defaultses) { it.gainIntensity },
        overlayDMs(removeOnlyDependencies, defaultses.map { it.removeOnlyDependencies }),
        overlayIntensities(defaultses) { it.removeIntensity },
    )
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
      thisUn: DependencyMap,
      otherUns: List<DependencyMap>,
  ): DependencyMap {
    val defMap = thisUn.keys.associateWith { thisUn[it]!! }.toMutableMap()
    for (key in otherUns.flatMap { it.keys }.toSet()) {
      if (key !in defMap) {
        // TODO some orders might work when others don't
        val depMapsWithThisKey = otherUns.filter { key in it }
        defMap[key] = depMapsWithThisKey.map { it[key]!! }.reduce { a, b -> a.intersect(b)!! }
      }
    }
    return DependencyMap(defMap)
  }
}
