package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.data.StateChange
import dev.martianzoo.tfm.data.StateChange.Cause
import dev.martianzoo.tfm.types.PType
import dev.martianzoo.util.HashMultiset
import dev.martianzoo.util.Multiset
import dev.martianzoo.util.MutableMultiset
import dev.martianzoo.util.filter

public class ComponentGraph(startingWith: Collection<Component> = listOf()) {
  private val multiset: MutableMultiset<Component> = HashMultiset()
  private val changeLog: MutableList<StateChange> = mutableListOf()

  init {
    multiset.addAll(startingWith)
  }

  public fun changeLog() = changeLog.toList()

  public fun applyChange(
      count: Int,
      gaining: Component? = null,
      removing: Component? = null,
      cause: Cause? = null,
      amap: Boolean = false,
  ) {
    require(gaining != removing)

    // TODO deal with limits

    val correctedCount: Int =
        if (amap) {
          removing?.let { multiset.tryRemove(it, count) } ?: count
        } else {
          removing?.let { multiset.mustRemove(it, count) }
          count
        }
    gaining?.let { multiset.add(it, correctedCount) }

    val change =
        StateChange(
            count = correctedCount,
            gaining = gaining?.asTypeExpr?.asGeneric(),
            removing = removing?.asTypeExpr?.asGeneric(),
            cause = cause,
        )
    changeLog.add(change)
  }

  public fun count(ptype: PType) = getAll(ptype).size

  // Aww yeah full table scans rule. One day I'll do something more clever, but only after being
  // able to review usage patterns so I'll actually know what helps most.
  public fun getAll(ptype: PType): Multiset<Component> = multiset.filter { it.hasType(ptype) }
}
