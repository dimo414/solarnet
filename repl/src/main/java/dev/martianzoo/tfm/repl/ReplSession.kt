package dev.martianzoo.tfm.repl

import dev.martianzoo.tfm.api.GameSetup
import dev.martianzoo.tfm.api.SpecialClassNames.COMPONENT
import dev.martianzoo.tfm.api.UserException
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.data.GameEvent.ChangeEvent
import dev.martianzoo.tfm.data.Player
import dev.martianzoo.tfm.data.Task
import dev.martianzoo.tfm.data.Task.TaskId
import dev.martianzoo.tfm.engine.Engine
import dev.martianzoo.tfm.engine.Exceptions.InteractiveException
import dev.martianzoo.tfm.engine.Game.EventLog.Checkpoint
import dev.martianzoo.tfm.engine.PlayerSession
import dev.martianzoo.tfm.engine.Result
import dev.martianzoo.tfm.pets.Parsing.parseAsIs
import dev.martianzoo.tfm.pets.Parsing.parseInput
import dev.martianzoo.tfm.pets.ast.ClassName
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn
import dev.martianzoo.tfm.pets.ast.Expression
import dev.martianzoo.tfm.pets.ast.Instruction
import dev.martianzoo.tfm.pets.ast.Instruction.Gain
import dev.martianzoo.tfm.pets.ast.Metric
import dev.martianzoo.tfm.pets.ast.Requirement
import dev.martianzoo.tfm.repl.ReplSession.ReplMode.BLUE
import dev.martianzoo.tfm.repl.ReplSession.ReplMode.GREEN
import dev.martianzoo.tfm.repl.ReplSession.ReplMode.RED
import dev.martianzoo.tfm.repl.ReplSession.ReplMode.YELLOW
import dev.martianzoo.tfm.types.MType
import dev.martianzoo.util.Multiset
import dev.martianzoo.util.toStrings
import java.io.File
import org.jline.reader.History

internal fun main() {
  val jline = JlineRepl()
  val repl = ReplSession(Canon.SIMPLE_GAME, jline)

  fun prompt(): String {
    val bundles: String = repl.setup.bundles.joinToString("")

    val phases = repl.session.list(cn("Phase").expression) // should only be one
    val phase: String = phases.singleOrNull()?.toString() ?: "NoPhase"

    val player: Player = repl.session.agent.player
    val count: Int = repl.setup.players
    val logPosition: Int = repl.session.game.events.size
    return repl.mode.color.foreground("$bundles $phase $player/$count @$logPosition> ")
  }

  // We don't actually have to start another game.....
  val welcome =
      """
        Welcome to REgo PLastics. Type `help` for help.
        Warning: this is a bare-bones tool that is not trying to be easy to use... at all
        Most things are case-sensitive. Sorry

      """
          .trimIndent()

  jline.loop(::prompt, repl::command, welcome)
  println("Bye")
}

/** A programmatic entry point to a REPL session that is more textual than [ReplSession]. */
public class ReplSession(var setup: GameSetup, private val jline: JlineRepl? = null) {
  // TODO all we use `jline` for is history (and just checking whether it's there or not)
  public var session = PlayerSession(Engine.newGame(setup))
    internal set

  internal var mode: ReplMode = GREEN
  internal val authority by setup::authority

  public enum class ReplMode(val message: String, val color: TfmColor) {
    RED("Arbitrary state changes with few restrictions", TfmColor.HEAT),
    YELLOW("Arbitrary state changes, within limits", TfmColor.MEGACREDIT),
    GREEN("Arbitrary state changes, triggering effects", TfmColor.PLANT),
    BLUE("Can only perform valid game actions", TfmColor.OCEAN_TILE),
  }

  private val inputRegex = Regex("""^\s*(\S+)(.*)$""")

  private class UsageException(message: String? = null) : InteractiveException(message ?: "")

  internal abstract inner class ReplCommand(val name: String) {
    open val isReadOnly: Boolean = false // TODO I think I intended to use this for something
    abstract val usage: String
    abstract val help: String
    open fun noArgs(): List<String> = throw UsageException()

    open fun withArgs(args: String): List<String> = throw UsageException()
  }

  internal val commands =
      listOf(
              AsCommand(),
              AutoCommand(),
              BecomeCommand(),
              BoardCommand(),
              CountCommand(),
              DescCommand(),
              ExecCommand(),
              HasCommand(),
              HelpCommand(),
              HistoryCommand(),
              ListCommand(),
              LogCommand(),
              MapCommand(),
              ModeCommand(),
              NewGameCommand(),
              RollbackCommand(),
              ScriptCommand(),
              TaskCommand(),
              TasksCommand(),
          )
          .associateBy { it.name }

  internal inner class HelpCommand : ReplCommand("help") {
    override val usage = "help [command]"
    override val help =
        """
          Help will give you help, if you want help, but this help on help doesn't help, does it?
        """
    override val isReadOnly = true
    override fun noArgs() = listOf(helpText)
    override fun withArgs(args: String): List<String> {
      return when (args.trim()) {
        "exit" -> listOf("I mean it exits.")
        "rebuild" -> listOf("Exits, recompiles the code, and restarts. Your game is lost.")
        else -> {
          val helpCommand = commands[args.trim().lowercase()]
          if (helpCommand == null) {
            listOf("¯\\_(ツ)_/¯ Type `help` for help")
          } else {
            helpCommand.help.trimIndent().split("\n")
          }
        }
      }
    }
  }

  internal inner class AsCommand : ReplCommand("as") {
    override val usage = "as <PlayerN> <full command>"
    override val help =
        """
          For any command you could type normally, put `as Player2` etc. or `as Engine` before it.
          It's handled as if you had first `become` that player, then restored.
        """

    override fun noArgs() = throw UsageException()
    override fun withArgs(args: String): List<String> {
      val (player, rest) = args.trim().split(Regex("\\s+"), 2)
      val saved = session
      return try {
        session = session.asPlayer(cn(player))
        command(rest)
      } finally {
        session = saved
      }
    }
  }

  internal inner class NewGameCommand : ReplCommand("newgame") {
    override val usage = "newgame <bundles> <player count>"
    override val help =
        """
          Erases your current game and starts a new one. You can't undo that (but you can get your
          command history out of ~/.rego_session and replay it.) For <bundles>, jam some letters
          together: B=Base, R=coRpoRate eRa, M=Tharsis, H=Hellas, X=Promos, and the rest are what
          you'd think. The player count can be from 1 to 5, but if you choose 1, you are NOT getting
          any of the actual solo rules!
        """

    override fun withArgs(args: String): List<String> {
      try {
        val (bundleString, players) = args.trim().split(Regex("\\s+"), 2)

        setup = GameSetup(authority, bundleString, players.toInt())
        session = PlayerSession(Engine.newGame(setup))

        return listOf("New $players-player game created with bundles: $bundleString") +
            if (players.toInt() == 1) {
              listOf("NOTE: No solo mode rules are implemented.")
            } else {
              listOf()
            }
      } catch (e: RuntimeException) {
        throw UsageException(e.message)
      }
    }
  }

  internal inner class BecomeCommand : ReplCommand("become") {
    override val usage = "become [PlayerN]"
    override val help =
        """
          Type `become Player2` or whatever and your prompt will change accordingly; everything you
          do now will be done as if it's player 2 doing it. You can also `become Engine` to do
          engine things.
        """

    override fun noArgs(): List<String> {
      session = session.asPlayer(Player.ENGINE)
      return listOf("Okay, you are the game engine now")
    }

    override fun withArgs(args: String): List<String> {
      session = session.asPlayer(cn(args))
      return listOf("Hi, ${session.agent.player}")
    }
  }

  internal inner class HasCommand : ReplCommand("has") {
    override val usage = "has <Requirement>"
    override val help =
        """
          Evaluates the requirement and tells you true or false. Go see syntax.md on the github page
          for syntax.
        """
    override val isReadOnly = true

    override fun withArgs(args: String): List<String> {
      val reqt: Requirement = parseInput(args)
      val result = session.has(reqt)
      return listOf("$result: ${session.prep(reqt)}")
    }
  }

  internal inner class CountCommand : ReplCommand("count") {
    override val usage = "count <Metric>"
    override val help =
        """
          Evaluates the metric and tells you the count. Usually just a type, but can include `MAX`,
          `+`, etc.
        """
    override val isReadOnly = true

    override fun withArgs(args: String): List<String> {
      val metric: Metric = parseInput(args)
      val count = session.count(metric)
      return listOf("$count ${session.prep(metric)}")
    }
  }

  internal inner class ListCommand : ReplCommand("list") {
    override val usage = "list <Expression>"
    override val help = """
          This command is super broken right now.
        """
    override val isReadOnly = true
    override fun noArgs() = withArgs(COMPONENT.toString())

    override fun withArgs(args: String): List<String> {
      val expr: Expression = parseAsIs(args)
      val counts: Multiset<Expression> = session.list(expr)
      return listOf("${counts.size} ${session.prep(expr)}") +
          counts.entries.sortedByDescending { (_, ct) -> ct }.map { (e, ct) -> "  $ct $e" }
    }
  }

  internal inner class BoardCommand : ReplCommand("board") {
    override val usage = "board [PlayerN]"
    override val help =
        """
          Shows a crappy player board for the named player, or the current player by default.
        """
    override val isReadOnly = true

    override fun noArgs(): List<String> = PlayerBoardToText(session, jline != null).board()

    override fun withArgs(args: String) =
        PlayerBoardToText(session.asPlayer(cn(args)), jline != null).board()
  }

  internal inner class MapCommand : ReplCommand("map") {
    override val usage = "map"
    override val help = """
          I mean it shows a map.
        """
    override val isReadOnly = true
    override fun noArgs() = MapToText(session.agent.reader, jline != null).map()
  }

  internal inner class ModeCommand : ReplCommand("mode") {
    override val usage = "mode <mode name>"
    override val help =
        """
          Changes modes. Names are red, yellow, green, blue, purple. Just enter a mode and it will
          tell you what it means.
        """

    override fun noArgs() = listOf("Mode $mode: ${mode.message}")

    override fun withArgs(args: String): List<String> {
      try {
        val thing = ReplMode.valueOf(args.uppercase())
        mode = thing
      } catch (e: Exception) {
        throw UsageException(
            "Valid modes are: ${ReplMode.values().joinToString { it.toString().lowercase() }}")
      }
      return noArgs()
    }
  }

  var auto: Boolean = true

  internal inner class AutoCommand : ReplCommand("auto") {
    override val usage = "auto [ on | off ]"
    override val help =
        """
          Turns auto-execute mode on or off, or just `auto` tells you what mode you're in. When you
          initiate an instruction with `exec` or `task`, per the game rules you always get to decide
          what order to do all the resulting tasks in. But that's a pain, so when `auto` is `on` (as
          it is by default) the REPL tries to execute each task (in the order they appear on the
          cards), and leaves it on the queue only if it can't run correctly. This setting is sticky
          until you `exit` or `rebuild`, even across games.
        """

    override fun noArgs() = listOf("Autoexecute is " + if (auto) "on" else "off")

    override fun withArgs(args: String): List<String> {
      auto =
          when (args) {
            "on" -> true
            "off" -> false
            else -> throw UsageException()
          }
      return noArgs()
    }
  }

  internal inner class ExecCommand : ReplCommand("exec") {
    override val usage = "exec <Instruction>"
    override val help =
        """
          Initiates the specified instruction; see syntax.md on github for details on syntax. If
          `auto` mode is on, it will also try to execute any tasks that result from this. Otherwise
           use `tasks` to see which tasks are waiting for you.
        """

    override fun withArgs(args: String): List<String> {
      val instruction = args
      val instr: Instruction = parseInput(instruction)
      val changes: Result =
          when (mode) {
            RED,
            YELLOW -> session.sneakyChange(instr)
            GREEN -> initiate(instr)
            BLUE -> {
              when {
                instr.isGainOf(cn("Turn")) -> initiate(instr)
                session.agent.player != Player.ENGINE -> {
                  throw UsageException("In blue mode you must be Engine to do this")
                }
                instr.isGainOf(cn("Phase")) -> initiate(instr)
                else -> throw UsageException("Eep, can't do that in ${mode.name.lowercase()} mode")
              }
            }
          }

      return describeExecutionResults(changes)
    }

    private fun Instruction.isGainOf(supertype: ClassName): Boolean =
        when (this) {
          is Gain -> {
            val t: MType = session.game.resolve(gaining)
            t.isSubtypeOf(session.game.resolve(supertype.expression))
          }
          is Instruction.Transform -> instruction.isGainOf(supertype)
          else -> false
        }

    private fun initiate(instruction: Instruction): Result {
      if (mode == BLUE && !session.game.tasks.isEmpty()) {
        throw InteractiveException.mustClearTasks()
      }
      return session.execute(instruction, auto)
    }
  }

  internal inner class TasksCommand : ReplCommand("tasks") {
    override val usage = "tasks"
    override val help =
        """
          List all currently pending tasks. You can then execute or drop them using `task`. The
          tasks of all players plus the engine are currently mixed together (but labeled).
        """
    override val isReadOnly = true
    override fun noArgs() = session.game.tasks.toStrings()
  }

  internal inner class TaskCommand : ReplCommand("task") {
    override val usage = "task <id> [<Instruction> | drop]"
    override val help =
        """
          To carry out a task exactly as it is, just type `task A` where `A` is the id of that task
          in your `tasks` list. But usually a task gets put on that list because its instruction
          was not fully specified. So, after `task A` you can write a revised version of that
          instruction, as long as your revision is a more specific form of the instruction. For
          example, if the queued task is `-3 StandardResource<Anyone>?` you can revise it to
          `-2 Plant<Player1>`.
        """

    override fun withArgs(args: String): List<String> {
      val q = session.game.tasks

      val split = Regex("\\s+").split(args, 2)
      val idString = split.firstOrNull() ?: throw UsageException()
      val id = TaskId(idString.uppercase())
      if (id !in q) throw UsageException("valid ids are ${q.ids}")
      val rest: String? =
          if (split.size > 1 && split[1].isNotEmpty()) {
            split[1]
          } else {
            null
          }
      if (rest == "drop") {
        session.agent.removeTask(id)
        return listOf("Task $id deleted")
      }
      val instruction: Instruction? = rest?.let(::parseInput)
      val result: Result =
          when (mode) {
            RED,
            YELLOW -> throw UsageException("Can't execute tasks in this mode")
            GREEN,
            BLUE ->
                if (auto) {
                  session.doTaskAndAutoExec(id, instruction)
                } else {
                  session.agent.doTask(id, instruction?.let(session::prep))
                }
          }
      return describeExecutionResults(result)
    }
  }

  private fun describeExecutionResults(changes: Result): List<String> {
    val oops: List<Task> = changes.tasksSpawned.map { session.game.getTask(it) }

    val interesting: List<ChangeEvent> = changes.changes.filterNot(session.game::isSystem)
    val changeLines = interesting.toStrings().ifEmpty { listOf("No state changes") }
    val taskLines =
        if (oops.any()) {
          listOf("", "There are new pending tasks:") + oops.toStrings()
        } else {
          listOf()
        }
    return changeLines + taskLines
  }

  internal inner class LogCommand : ReplCommand("log") {
    override val usage = "log [full]"
    override val help =
        """
          Shows everything that has happened in the current game (`log full`) or just the more
          interesting bits (i.e., filtering out Task changes, and filtering out changes to System
          components -- just like the default output after `exec` or `task` does).
        """
    override val isReadOnly = true

    override fun noArgs() =
        session.game.events.changesSinceSetup().filterNot(session.game::isSystem).toStrings()

    override fun withArgs(args: String): List<String> {
      if (args == "full") {
        return session.game.events.entriesSince(Checkpoint(0)).toStrings()
      } else {
        throw UsageException()
      }
    }
  }

  internal inner class RollbackCommand : ReplCommand("rollback") {
    override val usage = "rollback <logid>"
    override val help =
        """
          Undoes the event with the id given and every event after it. If you undo too far,
          you can't go forward again (you can only try to reconstruct the game from your
          ~/.rego_history). If you want to undo your command `exec 5 Plant`, look for the number in
          the command prompt on that line; that's the number to use here. Or check `log`. Be careful
          though, as you it will let you undo to a position when the engine was in the middle of
          doing stuff, which would put you in an invalid game state.
        """

    override fun withArgs(args: String): List<String> {
      session.game.rollBack(Checkpoint(args.toInt()))
      return listOf("Rollback done")
    }
  }

  internal inner class HistoryCommand : ReplCommand("history") {
    override val usage = "history <count>"
    override val help =
        """
          This shows the history of the commands you've typed into the REPL. It should contain
          history from your previous sessions too (hopefully). `history 20` would show you only
          the last 20. These are numbered, and if one command is numbered 123 you can type `!123`
          to repeat it. You can also write `!` plus the first few letters of the command and you'll
          get the most recent match. There's other stuff you can do; look for info on the `jline`
          library if curious.
        """
    override val isReadOnly = true
    val history = jline?.history

    override fun noArgs() = fmt(history!!)

    override fun withArgs(args: String): List<String> {
      val max = args.toIntOrNull() ?: throw UsageException()
      val drop = (history!!.size() - max).coerceIn(0, null)
      return fmt(history.drop(drop))
    }

    private fun fmt(entries: Iterable<History.Entry>) =
        entries.map { "${it.index() + 1}: ${it.line()}" }
  }

  internal inner class DescCommand : ReplCommand("desc") {
    override val usage = "desc <Expression>"
    override val help =
        """
          Put any type expression after `desc` and it will tell you everything it knows about that
          type. A page on github somewhere will explain what all the output means, but it doesn't
          exist yet.
        """
    override val isReadOnly = true

    override fun withArgs(args: String): List<String> {
      val expression: Expression =
          // if (args == "random") {
          //   val randomBaseType = session.table.allClasses.random().baseType
          //   val randomType = randomBaseType.concreteSubtypesSameClass().toList().random()
          //   randomType.expression
          // } else {
            parseAsIs(args)
          // }
      return listOf(MTypeToText.describe(expression, session))
    }
  }

  internal inner class ScriptCommand : ReplCommand("script") {
    override val usage = "script <filename>"
    override val help =
        """
          Reads from the given filename (expressed relative to the solarnet/ directory) and executes
          every command in it, as if you had typed it directly at the prompt, until reaching the
          line "stop" or the end of file. You probably don't want to put "exit" in that file.
        """

    override fun withArgs(args: String) =
        File(args)
            .readLines()
            .takeWhile { it.trim() != "stop" }
            .filter { it.isNotEmpty() }
            .flatMap { listOf(">>> $it") + command(it) + "" }
  }

  public fun command(wholeCommand: String): List<String> {
    val stripped = wholeCommand.replace(Regex("//.*"), "")
    val groups = inputRegex.matchEntire(stripped)?.groupValues
    return if (groups == null) {
      listOf()
    } else {
      val (_, command, args) = groups
      command(command.lowercase(), args.trim().ifEmpty { null })
    }
  }

  internal fun command(command: ReplCommand, args: String? = null): List<String> {
    return try {
      if (args == null) command.noArgs() else command.withArgs(args.trim())
    } catch (e: UserException) {
      val usage = if (e is UsageException) "Usage: ${command.usage}" else ""
      listOf(e.message, usage).filter { it.isNotEmpty() }
    }
  }

  internal fun command(commandName: String, args: String?): List<String> {
    val command = commands[commandName] ?: return listOf("¯\\_(ツ)_/¯ Type `help` for help")
    return command(command, args)
  }
}

private val helpText: String =
    """
      Commands can be separated with semicolons, or saved in a file and run with `script`.
      Type `help <command name>` to learn more.,

      CONTROL
        help                -> shows this message
        newgame BHV 3       -> erases current game and starts 3p game with Base/Hellas/Venus
        exit                -> go waste time differently
        rebuild             -> restart after code changes (game is forgotten)
        become Player1      -> makes Player1 the default player for queries & executions
        as Player1 <cmd>    -> does <cmd> as if you'd typed just that, but as Player1
        script mygame       -> reads file `mygame` and performs REPL commands as if typed
      QUERYING
        has MAX 3 OceanTile -> evaluates a requirement (true/false) in the current game state
        count Plant         -> counts how many Plants the default player has
        list Tile           -> list all Tiles (categorized)
        board               -> displays an extremely bad looking player board
        map                 -> displays an extremely bad looking Mars board
      EXECUTION
        exec PROD[3 Heat]   -> gives the default player 3 heat production
        tasks               -> shows your current to-do list
        task F              -> do task F on your to-do list, as-is
        task F Plant        -> do task F, substituting `Plant` for an abstract instruction
        task F drop         -> bye task F
        auto off            -> turns off autoexec mode (you'll have to run tasks 1-by-1)
        mode yellow         -> switches to Yellow Mode (also try red, green, blue, purple)
      HISTORY
        log                 -> shows events that have happened in the current game
        rollback 123        -> undoes recent events up to and *including* event 123
        history             -> shows your *command* history (as you typed it)
      METADATA
        desc Microbe<Ants>  -> describes the Microbe<Ants> type in detail
    """
        .trimIndent()
