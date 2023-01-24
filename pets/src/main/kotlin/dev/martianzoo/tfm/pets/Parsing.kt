package dev.martianzoo.tfm.pets

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.MismatchedToken
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.UnexpectedEof
import com.github.h0tk3y.betterParse.parser.parseToEnd
import dev.martianzoo.tfm.data.ClassDeclaration
import dev.martianzoo.tfm.pets.ClassDeclarationParsers.oneLineDeclaration
import dev.martianzoo.tfm.pets.ClassDeclarationParsers.tokenize
import dev.martianzoo.tfm.pets.ClassDeclarationParsers.topLevelDeclarationGroup
import dev.martianzoo.tfm.pets.ast.Action
import dev.martianzoo.tfm.pets.ast.Action.Cost
import dev.martianzoo.tfm.pets.ast.Effect
import dev.martianzoo.tfm.pets.ast.Effect.Trigger
import dev.martianzoo.tfm.pets.ast.Instruction
import dev.martianzoo.tfm.pets.ast.PetNode
import dev.martianzoo.tfm.pets.ast.Requirement
import dev.martianzoo.tfm.pets.ast.ScalarAndType
import dev.martianzoo.tfm.pets.ast.TypeExpr
import dev.martianzoo.tfm.pets.ast.TypeExpr.TypeParsers
import dev.martianzoo.util.Debug
import dev.martianzoo.util.ParserGroup
import kotlin.reflect.KClass
import kotlin.reflect.cast

object Parsing {

  private val parserGroup by lazy {
    val pgb = ParserGroup.Builder<PetNode>()
    pgb.publish(TypeParsers.genericTypeExpr)
    pgb.publish(TypeParsers.typeExpr)
    pgb.publish(ScalarAndType.parser())
    pgb.publish(Requirement.parser())
    pgb.publish(Instruction.parser())
    pgb.publish(Cost.parser())
    pgb.publish(Action.parser())
    pgb.publish(Trigger.parser())
    pgb.publish(Effect.parser())
    pgb.finish()
  }

  /**
   * Parses the PETS element in [elementSource], expecting a construct of type [P], and returning
   * the parsed [P]. [P] can only be one of the major elemental types like [Effect], [Action],
   * [Instruction], [TypeExpr], etc.
   */
  inline fun <reified P : PetNode> parsePets(elementSource: String): P =
      parsePets(P::class, elementSource)

  /** Non-reified version of `parse(source)`. */
  fun <P : PetNode> parsePets(expectedType: KClass<P>, source: String): P {
    val pet =
        try {
          parserGroup.parse(expectedType, tokenize(source))
        } catch (e: ParseException) {
          throw IllegalArgumentException(
              """
          Expecting ${expectedType.simpleName} ...
          Input was:
          $source
      """
                  .trimIndent(),
              e)
        }
    return expectedType.cast(pet)
  }

  // For java
  fun <P : PetNode> parsePets(expectedType: Class<P>, source: String) =
      parsePets(expectedType.kotlin, source)

  fun parseOneLineClassDeclaration(declarationSource: String): ClassDeclaration {
    return parse(oneLineDeclaration, declarationSource)
  }
  /** Parses an entire PETS class declarations source file. */
  fun parseClassDeclarations(declarationsSource: String): List<ClassDeclaration> {
    val tokens = tokenize(stripLineComments(declarationsSource))
    return parseRepeated(topLevelDeclarationGroup, tokens)
  }

  fun <T> parse(parser: Parser<T>, source: String): T {
    val tokens = tokenize(source)
    Debug.d(
        tokens
            .filterNot { it.type.ignored }
            .joinToString(" ") { it.type.name?.replace("\n", "\\n") ?: "NULL" })
    return parser.parseToEnd(tokens)
  }

  private val lineCommentRegex = Regex(""" *(//[^\n]*)*\n""")

  private fun stripLineComments(text: String) = lineCommentRegex.replace(text, "\n")
}

fun <T> parseRepeated(listParser: Parser<List<T>>, tokens: TokenMatchesSequence): List<T> {
  Debug.d(
      tokens
          .filterNot { it.type.ignored }
          .joinToString(" ") { it.type.name?.replace("\n", "\\n") ?: "NULL" })
  var index = 0
  val parsed = mutableListOf<T>()
  while (true) {
    val result = listParser.tryParse(tokens, index)
    when {
      result is Parsed -> {
        parsed += result.value
        require(result.nextPosition != index) { index }
        index = result.nextPosition
      }
      result is UnexpectedEof -> break
      result is AlternativesFailure && result.errors.any(::isEOF) -> break
      result is ErrorResult -> myThrow(result)
      else -> error("huh?")
    }
  }
  return parsed
}

fun myThrow(result: ErrorResult) {
  var message = StringBuilder()
  var ctr = 0
  val locations = mutableMapOf<Pair<Int, Int>, Int>()
  var input: String? = null
  fun visit(result: ErrorResult) {
    when (result) {
      is AlternativesFailure -> result.errors.forEach(::visit)
      is MismatchedToken -> {
        val match: TokenMatch = result.found
        val loc = match.row to match.column
        val thisLoc =
            if (loc in locations) {
              locations[loc]
            } else {
              locations[loc] = ctr
              ctr++
            }
        input = match.input.toString()
        val found = match.text.replace("\n", "\\n")
        val expec = result.expected.name?.replace("\n", "\\n")
        message.append(
            "$thisLoc: at ${match.row}:${match.column}, looking for $expec, but found $found\n")
      }
      else -> message.append(result.toString())
    }
  }

  visit(result)

  message.append("\nNow, here is the input:\n")
  input!!.split("\n").forEachIndexed { lineNum, line ->
    message.append("$line\n")
    (1..100).forEach { columnNum ->
      val loc = (lineNum + 1) to columnNum
      message.append(if (loc in locations) "${locations[loc]}" else " ")
    }
    message.append("\n")
  }

  throw RuntimeException(message.toString())
}

private fun isEOF(result: ParseResult<*>?): Boolean =
    when (result) {
      is UnexpectedEof -> true
      is AlternativesFailure -> result.errors.any(::isEOF)
      else -> false
    }
