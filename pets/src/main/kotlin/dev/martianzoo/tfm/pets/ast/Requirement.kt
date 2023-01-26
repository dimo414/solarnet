package dev.martianzoo.tfm.pets.ast

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import dev.martianzoo.tfm.pets.Parsing
import dev.martianzoo.tfm.pets.PetException
import dev.martianzoo.tfm.pets.PetParser
import dev.martianzoo.tfm.pets.PetVisitor
import dev.martianzoo.tfm.pets.SpecialClassNames.THIS

sealed class Requirement : PetNode() {
  open fun requiresThis() = false

  data class Min(val sat: ScalarAndType) : Requirement() {
    override fun visitChildren(v: PetVisitor) = v.visit(sat)
    override fun toString() = "$sat"

    init {
      if (sat.scalar == 0) {
        throw PetException("Minimum of 0 would always be true")
      }
    }

    override fun requiresThis() = this.sat == ScalarAndType.sat(1, THIS.type)
  }

  data class Max(val sat: ScalarAndType) : Requirement() {
    override fun visitChildren(v: PetVisitor) = v.visit(sat)
    override fun toString() = "MAX ${sat.toString(true, true)}" // no "MAX 5" or "MAX Heat"
  }

  data class Exact(val sat: ScalarAndType) : Requirement() {
    override fun visitChildren(v: PetVisitor) = v.visit(sat)
    override fun toString() = "=${sat.toString(true, true)}" // no "=5" or "=Heat"

    override fun requiresThis() = this.sat == ScalarAndType.sat(1, THIS.type)
  }

  data class Or(val requirements: Set<Requirement>) : Requirement() {
    constructor(
        req1: Requirement,
        req2: Requirement,
        vararg rest: Requirement
    ) : this(setOf(req1) + req2 + rest)

    init {
      require(requirements.size >= 2)
    }

    override fun visitChildren(v: PetVisitor) = v.visit(requirements)
    override fun toString() = requirements.joinToString(" OR ") { groupPartIfNeeded(it) }
    override fun precedence() = 3
  }

  data class And(val requirements: List<Requirement>) : Requirement() {
    constructor(
        req1: Requirement,
        req2: Requirement,
        vararg rest: Requirement
    ) : this(listOf(req1) + req2 + rest)

    init {
      require(requirements.size >= 2)
    }

    override fun visitChildren(v: PetVisitor) = v.visit(requirements)
    override fun toString() = requirements.joinToString { groupPartIfNeeded(it) }
    override fun precedence() = 1

    override fun requiresThis() = requirements.any { it.requiresThis() }
  }

  data class Transform(val requirement: Requirement, override val transform: String) :
      Requirement(), GenericTransform<Requirement> {
    override fun visitChildren(v: PetVisitor) = v.visit(requirement)
    override fun toString() = "$transform[$requirement]"
    override fun extract() = requirement
  }

  override val kind = "Requirement"

  companion object : PetParser() {
    fun requirement(text: String) = Parsing.parse(parser(), text)

    internal fun parser(): Parser<Requirement> {
      return parser {
        val orReq =
            separatedTerms(atomParser(), _or) map
                {
                  val set = it.toSet()
                  if (set.size == 1) set.first() else Or(set)
                }
        commaSeparated(orReq) map { if (it.size == 1) it.first() else And(it) }
      }
    }

    /** A requirement suitable for being nested directly in something else. */
    internal fun atomParser(): Parser<Requirement> {
      return parser {
        val sat = ScalarAndType.parser()
        val min = sat map ::Min
        val max = skip(_max) and sat map ::Max
        val exact = skipChar('=') and sat map ::Exact
        val transform =
            transform(parser()) map { (node, transformName) -> Transform(node, transformName) }
        min or max or exact or transform or group(parser())
      }
    }
  }
}
