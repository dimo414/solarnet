package dev.martianzoo.tfm.repl

import dev.martianzoo.tfm.api.Authority
import dev.martianzoo.tfm.api.GameSetup
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn
import dev.martianzoo.tfm.pets.ast.Instruction.Companion.instruction
import dev.martianzoo.tfm.pets.ast.Requirement.Companion.requirement
import dev.martianzoo.tfm.pets.ast.TypeExpr.Companion.typeExpr
import dev.martianzoo.tfm.types.PClass
import dev.martianzoo.util.toStrings

internal fun main() {
  val session = ReplSession(Canon)
  session.command("newgame BM 2").forEach(::println)

  val repl = JlineRepl()
  repl.loop(session::command)
}

/** A programmatic entry point to a REPL session that is more textual than [ReplSession]. */
public class ReplSession(private val authority: Authority) {
  private val session = InteractiveSession()

  public fun command(wholeCommand: String): List<String> {
    val (_, command, args) = INPUT_REGEX.matchEntire(wholeCommand)?.groupValues ?: return listOf()
    return command(command, args.ifBlank { null })
  }

  internal fun command(command: String, args: String?): List<String> {
    if (command !in setOf("help", "newgame") && session.game == null) {
      return listOf("no game active")
    }
    return commands[command]?.invoke(args) ?: commands["exec"]!!(command + (args ?: ""))
  }

  private val commands =
      mapOf<String, (String?) -> List<String>>(
          "help" to { listOf(HELP) },
          "newgame" to
              {
                it?.let { args ->
                  val (bundleString, players) = args.trim().split(Regex("\\s+"), 2)
                  session.newGame(GameSetup(authority, bundleString, players.toInt()))
                  listOf("New $players-player game created with bundles: $bundleString")
                } ?: listOf("Usage: newgame <bundles> <player count>")
              },
          "become" to
              { args ->
                val message =
                    if (args == null) {
                      session.becomeNoOne()
                      "Okay you are no one"
                    } else {
                      val trimmed = args.trim()
                      require(trimmed.length == 7 && trimmed.startsWith("Player"))
                      val p = trimmed.substring(6).toInt()
                      session.becomePlayer(p)
                      "Hi, $trimmed"
                    }
                listOf(message)
              },
          "count" to
              {
                it?.let { args ->
                  val typeExpr = session.fixTypes(typeExpr(args))
                  val count = session.count(typeExpr)
                  listOf("$count $typeExpr")
                } ?: listOf("Usage: count <TypeExpr>")
              },
          "has" to
              {
                it?.let { args ->
                  val fixed = session.fixTypes(requirement(args))
                  val result = session.has(fixed)
                  listOf("$result: $fixed")
                } ?: listOf("Usage: has <Requirement>")
              },
          "map" to
              {
                if (it == null) {
                  MapToText(session.game!!.asGameState).map()
                } else {
                  listOf("Arguments unexpected: $it")
                }
              },
          "board" to
              {
                val player = if (it == null) session.defaultPlayer!! else cn(it.trim())
                BoardToText(session.game!!.asGameState).board(player.type)
              },
          "changes" to
              { args ->
                args?.let { listOf("Arguments unexpected: $it") }
                    ?: session.game!!.changeLog().toStrings().mapIndexed { i, s -> "$i: $s" }
              },
          "exec" to
              {
                it?.let { args ->
                  val instr = session.execute(instruction(args))
                  listOf("Ok: $instr")
                }
                    ?: listOf("Usage: exec <Instruction>")
              },
          "desc" to
              {
                it?.let { args ->
                  val className = cn(args.trim())
                  val pclass: PClass = session.game!!.loader.getClass(className)
                  listOf(pclass.describe())
                }
                    ?: listOf("Usage: desc <ClassName>")
              },
      )
}

internal val INPUT_REGEX = Regex("""^\s*(\S+)(.*)$""")

private val HELP =
    """
      newgame BMP 3        ->  ERASE CURRENT GAME and start a new 3p game with Base/Tharsis/Prelude
      become Player1       ->  make Player1 the default player for future commands
      count Plant          ->  counts how many Plants the default player has
      count Plant<Anyone>  ->  counts how many Plants anyone has
      list Tile            ->  lists all Tiles you have
      has MAX 3 OceanTile  ->  evaluates a requirement in the current game state
      exec PROD[3 Heat]    ->  gives the default player 3 heat production
      PROD[3 Heat]         ->  that too
      changes              ->  see the changelog for the current game
      history              ->  see your *command* history
      board                ->  displays an extremely bad looking player board
      map                  ->  displays an extremely bad looking map
      desc Microbe         ->  describes the Microbe class in detail (given this game setup)
      help                 ->  see this message
    """.trimIndent()
