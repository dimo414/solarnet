package dev.martianzoo.tfm.api

import dev.martianzoo.tfm.data.ChangeRecord.Cause

interface GameStateWriter {
  fun write(
      count: Int = 1,
      gaining: Type? = null,
      removing: Type? = null,
      amap: Boolean = false,
      cause: Cause? = null,
  )
}
