package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.types.MType
import dev.martianzoo.util.Multiset

/**
 * A multiset of [Component] instances; the "present" state of a game in progress. It is a plain
 * multiset, but called a "graph" because these component instances have references to their
 * dependencies which are also stored in the multiset.
 */
interface ComponentGraph {
  /**
   * Does at least one instance of [component] exist currently? (That is, is [countComponent]
   * nonzero?
   */
  operator fun contains(component: Component): Boolean

  /** How many instances of the exact component [component] currently exist? */
  fun countComponent(component: Component): Int

  /** How many total component instances have the type [parentType] (or any of its subtypes)? */
  fun count(parentType: MType): Int

  /**
   * Returns all component instances having the type [parentType] (or any of its subtypes), as a
   * multiset. The size of the returned collection will be `[count]([parentType])` . If [parentType]
   * is `Component` this will return the entire component multiset.
   */
  fun getAll(parentType: MType): Multiset<Component>
}
