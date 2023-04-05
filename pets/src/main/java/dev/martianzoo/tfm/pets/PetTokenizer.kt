@file:Suppress("PropertyName")

package dev.martianzoo.tfm.pets

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple2
import dev.martianzoo.tfm.pets.ast.Instruction.Intensity

/** A base class for parsing objects. */
public open class PetTokenizer {
  internal val _arrow = literal("->", "arrow")
  internal val _doubleColon = literal("::", "doubleColon")
  internal val _questionColon = literal("?:", "questionColon")

  // I simply don't want to name all of these and would rather look them up by the char itself
  private val characters = "!@^+,-./:;=?()[]{}<>\n".map { it to literal("$it") }.toMap()

  internal val _by = literal("BY")
  internal val _from = literal("FROM")
  internal val _has = literal("HAS")
  internal val _if = literal("IF")
  internal val _max = literal("MAX")
  internal val _or = literal("OR")
  internal val _then = literal("THEN")
  internal val _x = regex(Regex("X\\b"), "X")

  // class declarations
  internal val _abstract = literal("ABSTRACT")
  internal val _class = literal("CLASS")
  internal val _default = literal("DEFAULT")

  // regexes - could leave the `Regex()` out, but it loses IDEA syntax highlighting!
  internal val _upperCamelRE = regex(Regex("""\b[A-Z][a-z][A-Za-z0-9_]*\b"""), "UpperCamel")
  internal val _lowerCamelRE = regex(Regex("""\b[a-z][a-zA-Z0-9]*\b"""), "lowerCamel")
  // can't match just X!
  internal val _allCapsWordRE = regex(Regex("""\b([A-WYZ]|[A-Z][A-Z0-9]{1,3})\b"""), "ALLCAPS")
  private val _scalarRE = regex(Regex("""\b(0|[1-9][0-9]*)"""), "scalar")

  internal val rawScalar: Parser<Int> = _scalarRE map { it.text.toInt() }

  internal val intensity =
      optional(char('!') or char('.') or char('?') map { it.text } map Intensity::from)

  internal inline fun <reified T> optionalList(parser: Parser<List<T>>) =
      optional(parser) map { it ?: listOf() }

  internal inline fun <reified T> transform(interior: Parser<T>) =
      _allCapsWordRE and
      skipChar('[') and
      interior and
      skipChar(']') map { (trans, inter) ->
        Tuple2(inter, trans.text.removeSuffix("["))
      }

  internal inline fun <reified P> commaSeparated(p: Parser<P>) = separatedTerms(p, char(','))

  internal inline fun <reified T> group(contents: Parser<T>) =
      skipChar('(') and contents and skipChar(')')

  internal inline fun <reified T> maybeGroup(contents: Parser<T>) = contents or group(contents)

  internal fun char(c: Char): Token = characters[c] ?: error("add $c to `characters`")

  internal fun skipChar(c: Char) = skip(char(c))

  internal object TokenCache {
    private val ignoreList =
        listOf<Token>(
            regexToken("backslash-newline", "\\\\\n", true), // ignore these
            regexToken("spaces", " +", true))

    private val map = mutableMapOf<Pair<String, Boolean>, Token>()

    fun cacheLiteral(text: String, name: String) =
        map.computeIfAbsent(name to false) { literalToken(name, text) }

    fun cacheRegex(regex: Regex, name: String) =
        map.computeIfAbsent(name to true) { regexToken(name, regex) }

    private val toke by lazy { DefaultTokenizer(ignoreList + map.values) }

    fun tokenize(input: String): TokenMatchesSequence = toke.tokenize(input)
  }

  private fun literal(text: String, name: String = text) = TokenCache.cacheLiteral(text, name)
  private fun regex(regex: Regex, name: String = "$regex") = TokenCache.cacheRegex(regex, name)
}
