package dev.martianzoo.tfm.repl.commands

import dev.martianzoo.tfm.repl.ReplCommand
import dev.martianzoo.tfm.repl.ReplSession
import dev.martianzoo.tfm.repl.ReplSession.ReplMode
import dev.martianzoo.tfm.repl.ReplSession.UsageException

internal class ModeCommand(val repl: ReplSession) : ReplCommand("mode") {
  override val usage = "mode <mode name>"
  override val help =
      """
        Changes modes. Names are red, yellow, green, blue, purple. Just enter a mode and it will
        tell you what it means.
      """

  override fun noArgs() = listOf("Mode ${repl.mode}: ${repl.mode.message}")

  override fun withArgs(args: String): List<String> {
    try {
      repl.mode = ReplMode.valueOf(args.uppercase())
    } catch (e: Exception) {
      throw UsageException(
          "Valid modes are: ${ReplMode.values().joinToString { it.toString().lowercase() }}")
    }
    return noArgs()
  }
}
