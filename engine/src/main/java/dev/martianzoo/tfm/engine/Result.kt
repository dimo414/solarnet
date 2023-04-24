package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.data.GameEvent.ChangeEvent
import dev.martianzoo.tfm.data.Task.TaskId

/**
 * Returned by a successful execution to indicate what changes were performed and what new tasks
 * were added.
 */
public data class Result(
    public val changes: List<ChangeEvent>,
    public val tasksSpawned: Set<TaskId>,
)
