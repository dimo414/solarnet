package dev.martianzoo.tfm.types

data class DependencyMap(val keyToType: Map<DependencyKey, DependencyTarget>) {

  constructor() : this(mapOf<DependencyKey, DependencyTarget>())

  init {
    keyToType.forEach {
      if (it.key.classDep) {
        require(it.value is PetClass) { it.key }
      } else {
        require(it.value is PetType) { it.key }
      }
    }
  }
  val abstract = keyToType.values.any { type -> type.abstract }

  fun sub(that: DependencyMap) =
      // For each of *its* keys, my type must be a subtype of its type
      that.keyToType.all { (thatKey, thatType) -> keyToType[thatKey]!!.isSubtypeOf(thatType) }

  // Combines all entries, using the glb when both maps have the same key
  fun merge(that: DependencyMap) = DependencyMap(
      dev.martianzoo.util.merge(this.keyToType, that.keyToType) { type1, type2 -> type1.glb(type2) })

  // determines the map that could be merged with this one to specialize, by inferring which
  // keys the provided specs go with
  fun findMatchups(specs: List<PetType>): DependencyMap {
    val unhandled = specs.toMutableList()
    val newMap: Map<DependencyKey, DependencyTarget> = keyToType.mapNotNull {
      (key, originalValue) ->
        if (key.classDep) {
          val matchType: PetType? = unhandled.firstOrNull {
            require(it.dependencies.keyToType.isEmpty())
            it.petClass.isSubtypeOf(originalValue)
          }
          matchType?.let { key to it.also(unhandled::remove).petClass }
        } else {
          val matchType = unhandled.firstOrNull { it.isSubtypeOf(originalValue) }
          matchType?.let { key to it.also(unhandled::remove) }
        }
    }.toMap()
    require (unhandled.isEmpty()) { "Unrecognized specializations: $unhandled"}
    return DependencyMap(newMap)
  }

  fun specialize(specs: List<PetType>) = merge(findMatchups(specs))
  override fun toString() = "$keyToType"
}
