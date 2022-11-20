package dev.martianzoo.tfm.petaform.parser

import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import dev.martianzoo.tfm.petaform.api.Expression
import dev.martianzoo.tfm.petaform.api.Predicate
import dev.martianzoo.tfm.petaform.parser.ExpressionGrammar.expression
import dev.martianzoo.tfm.petaform.parser.PredicateGrammar.predicate

object BetterParser : PetaformParser {
  override fun parseExpression(petaform: String): Expression {
    return parseWith(expression, petaform)
  }

  override fun parsePredicate(petaform: String): Predicate {
    return parseWith(predicate, petaform)
  }

  private fun <T> parseWith(parser: Parser<T>, input: String) =
      parser.parseToEnd(Tokens.tokenizer.tokenize(input))
}
