package dev.martianzoo.tfm.repl.commands

import dev.martianzoo.tfm.repl.ReplCommand
import dev.martianzoo.tfm.repl.ReplSession

internal class TasksCommand(val repl: ReplSession) : ReplCommand("tasks") {
  override val usage = "tasks"
  override val help =
      """
        List all currently pending tasks. You can then execute or drop them using `task`. The
        tasks of all players plus the engine are currently mixed together (but labeled).
      """
  override val isReadOnly = true
  override fun noArgs() = repl.tfm.game.tasks.extract { it.toStringWithoutCause() }
}
