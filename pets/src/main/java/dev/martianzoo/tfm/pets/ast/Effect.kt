package dev.martianzoo.tfm.pets.ast

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import dev.martianzoo.tfm.api.SpecialClassNames.END
import dev.martianzoo.tfm.api.SpecialClassNames.THIS
import dev.martianzoo.tfm.api.SpecialClassNames.USE_ACTION
import dev.martianzoo.tfm.pets.BaseTokenizer
import dev.martianzoo.tfm.pets.Parsing
import dev.martianzoo.tfm.pets.PetException
import dev.martianzoo.tfm.pets.ast.ClassName.Parsing.className
import dev.martianzoo.tfm.pets.ast.Effect.Trigger.OnGainOf
import dev.martianzoo.tfm.pets.ast.Effect.Trigger.WhenGain
import dev.martianzoo.tfm.pets.ast.Effect.Trigger.WhenRemove
import dev.martianzoo.tfm.pets.ast.Instruction.Gated
import dev.martianzoo.util.iff

public data class Effect(
    val trigger: Trigger,
    val instruction: Instruction,
    val automatic: Boolean = false,
) : PetNode(), Comparable<Effect> {

  override val kind = Effect::class.simpleName!!

  override fun visitChildren(visitor: Visitor) = visitor.visit(trigger, instruction)

  override fun toString() =
      "$trigger:${":".iff(automatic)} " +
          if (instruction is Gated) "($instruction)" else "$instruction"

  override fun compareTo(other: Effect): Int = effectComparator.compare(this, other)

  sealed class Trigger : PetNode() {
    override val kind = Trigger::class.simpleName!!

    data class ByTrigger(val inner: Trigger, val by: ClassName) : Trigger() {
      init {
        if (inner is ByTrigger) {
          throw PetException("by the by")
        }
      }

      override fun visitChildren(visitor: Visitor) = visitor.visit(inner, by)
      override fun toString() = "$inner BY $by"
    }

    data class IfTrigger(val inner: Trigger, val condition: Requirement) : Trigger() {
      init {
        if (inner is ByTrigger) throw PetException("if the by")
        if (inner is IfTrigger) throw PetException("if the if")
      }
      override fun visitChildren(visitor: Visitor) = visitor.visit(inner, condition)

      override fun toString() = "$inner IF ${groupPartIfNeeded(condition)}"
    }

    object WhenGain : Trigger() {
      override fun visitChildren(visitor: Visitor) = Unit
      override fun toString() = "This"
    }

    object WhenRemove : Trigger() {
      override fun visitChildren(visitor: Visitor) = Unit
      override fun toString() = "-This"
    }

    data class OnGainOf private constructor(val expression: Expression) : Trigger() {
      companion object {
        fun create(expression: Expression): Trigger {
          return if (expression == THIS.expr) {
            WhenGain
          } else {
            OnGainOf(expression)
          }
        }
      }
      init {
        require(expression != THIS.expr)
      }

      override fun visitChildren(visitor: Visitor) = visitor.visit(expression)
      override fun toString() = "$expression"
    }

    data class OnRemoveOf private constructor(val expression: Expression) : Trigger() {
      companion object {
        fun create(expression: Expression): Trigger {
          return if (expression == THIS.expr) {
            WhenRemove
          } else {
            OnRemoveOf(expression)
          }
        }
      }

      init {
        require(expression != THIS.expr)
      }

      override fun visitChildren(visitor: Visitor) = visitor.visit(expression)
      override fun toString() = "-$expression"
    }

    data class XTrigger(val inner: Trigger) : Trigger() {
      init {
        if (!(inner is OnGainOf || inner is WhenGain || inner is OnRemoveOf || inner is WhenRemove))
          throw PetException("Can't have an X trigger of ${inner::class.simpleName}")
      }

      override fun visitChildren(visitor: Visitor) = visitor.visit(inner)
      override fun toString(): String {
        return when (inner) {
          is OnGainOf, is WhenGain -> "X $inner"
          is OnRemoveOf, is WhenRemove -> "-X ${inner.toString().substring(1)}"
          else -> error("")
        }
      }
    }

    data class Transform(val trigger: Trigger, override val transformKind: String) :
        Trigger(), GenericTransform<Trigger> {
      override fun visitChildren(visitor: Visitor) = visitor.visit(trigger)
      override fun toString() = "$transformKind[$trigger]"

      init {
        if (trigger !is OnGainOf && trigger !is OnRemoveOf && trigger !is XTrigger) {
          throw PetException("only gain/remove trigger can go in transform block")
        }
      }

      override fun extract() = trigger
    }

    companion object : BaseTokenizer() {
      fun trigger(text: String): Trigger = Parsing.parse(parser(), text)

      fun parser(): Parser<Trigger> {
        return parser {
          val onGainOf: Parser<Trigger> = Expression.parser() map OnGainOf::create

          val exxedGain: Parser<XTrigger> = skip(_x) and onGainOf map ::XTrigger

          val onRemoveOf: Parser<Trigger> =
              skipChar('-') and Expression.parser() map OnRemoveOf::create

          val exxedRemove: Parser<XTrigger> =
              skipChar('-') and
                  skip(_x) and
                  Expression.parser() map OnRemoveOf::create map ::XTrigger

          val atom: Parser<Trigger> = exxedGain or exxedRemove or onGainOf or onRemoveOf
          val transform = transform(atom) map { (node, name) -> Transform(node, name) }
          val ifClause: Parser<Requirement> = skip(_if) and Requirement.atomParser()
          val byClause: Parser<ClassName> = skip(_by) and className

          (transform or atom) and
              optional(ifClause) and
              optional(byClause) map
              { (inTrigger, `if`, by) ->
                var trig = inTrigger
                if (`if` != null) trig = IfTrigger(trig, `if`)
                if (by != null) trig = ByTrigger(trig, by)
                trig
              }
        }
      }
    }
  }

  companion object : BaseTokenizer() {
    fun effect(text: String): Effect = Parsing.parse(parser(), text)

    fun parser(): Parser<Effect> {
      val colons = _doubleColon or char(':') map { it.text == "::" }

      return Trigger.parser() and
          colons and
          maybeGroup(Instruction.parser()) map
          { (trig, immed, instr) ->
            Effect(trigger = trig, automatic = immed, instruction = instr)
          }
    }

    // TODO really a trigger comparator
    private val effectComparator: Comparator<Effect> =
        compareBy(
            {
              val t = it.trigger
              when {
                t == WhenGain -> if (it.automatic) -1 else 0
                t == WhenRemove -> if (it.automatic) 1 else 2
                t is OnGainOf && "${t.expression.className}".startsWith("$USE_ACTION") -> 4
                t == OnGainOf.create(END.expr) -> 5
                else -> 3
              }
            },
            { "${it.trigger}" },
        )
  }
}
