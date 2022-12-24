package dev.martianzoo.tfm.pets.ast

import dev.martianzoo.tfm.pets.PetsException
import dev.martianzoo.tfm.pets.classNamePattern
import dev.martianzoo.util.joinOrEmpty

sealed class Instruction : PetsNode() {
  data class Gain(val qe: QuantifiedExpression, val intensity: Intensity? = null) : Instruction() {
    constructor(expr: TypeExpression?, scalar: Int? = null, intensity: Intensity? = null) : this(
        QuantifiedExpression(expr, scalar), intensity)

    init {
      if ((qe.scalar ?: 1) <= 0) throw PetsException("Can't gain a non-positive amount")
    }

    override fun toString() = "${qe}${intensity?.pets ?: ""}"
    override val children = setOf(qe)
  }

  data class Remove(val qe: QuantifiedExpression, val intensity: Intensity? = null) : Instruction() {
    constructor(expr: TypeExpression?, scalar: Int? = null, intensity: Intensity? = null) : this(
        QuantifiedExpression(expr, scalar), intensity)

    init {
      if ((qe.scalar ?: 1) <= 0) throw PetsException("Can't remove a non-positive amount")
    }

    override fun toString() = "-${qe}${intensity?.pets ?: ""}"
    override val children = setOf(qe)
  }

  sealed class FromExpression : PetsNode()

  data class SimpleFrom(val to: TypeExpression, val from: TypeExpression) : FromExpression() {
    override fun toString() = "$to FROM $from"
    override val children = setOf(to, from)
  }

  data class ComplexFrom(
      val className: String,
      val specializations: List<FromExpression> = listOf(),
      val requirement: Requirement? = null
  ) : FromExpression() {
    init {
      require(className.matches(classNamePattern())) { className }
      if (specializations.count { it is ComplexFrom || it is SimpleFrom } != 1) {
        throw PetsException("Can only have one FROM in an expression")
      }
    }

    override fun toString() =
        className +
            specializations.joinOrEmpty(surround = "<>") +
            (requirement?.let { "(HAS $it)" } ?: "")
    override val children = specializations + setOfNotNull(requirement)
  }

  data class TypeInFrom(val type: TypeExpression) : FromExpression() {
    override fun toString() = "$type"
    override val children = setOf(type)
  }

  data class Transmute(
      val trans: FromExpression,
      val scalar: Int? = null,
      val intensity: Intensity? = null) : Instruction() {
    init {
      if ((scalar ?: 1) < 1) throw PetsException("Can't do a non-positive number of transmutes")
      if (trans is TypeInFrom) throw PetsException("Should be a regular gain instruction")
    }
    override fun toString(): String {
      val intens = intensity?.pets ?: ""
      val scal = if (scalar != null) "$scalar " else ""
      return "$scal$trans$intens"
    }
    override val children = setOf(trans)
    override fun parenthesizeThisWhenInside(container: PetsNode): Boolean {
      if (trans is SimpleFrom && container is Or) {
        return true // not technically necessary, but helpful
      }
      return super.parenthesizeThisWhenInside(container)
    }

    override fun precedence() = if (trans is SimpleFrom) 7 else 10
  }

  data class Per(val instruction: Instruction, val qe: QuantifiedExpression): Instruction() {
    init {
      if ((qe.scalar ?: 1) <= 0) throw PetsException("Can't do something 'per' a nonpositive amount")
      when (instruction) {
        is Gain, is Remove, is Transmute -> {}
        else -> throw PetsException("Per can only contain gain/remove/transmute")
      }
    }
    override fun toString() = "$instruction / $qe"
    override fun precedence() = 8
    override val children = setOf(instruction, qe)
  }

  data class Gated(val requirement: Requirement, val instruction: Instruction): Instruction() {
    init {
      if (instruction is Gated) {
        throw PetsException("You don't gate a gater") // TODO keep??
      }
    }
    override fun toString(): String {
      return "${groupIfNeeded(requirement)}: ${groupIfNeeded(instruction)}"
    }

    // let's over-group for clarity
    override fun parenthesizeThisWhenInside(container: PetsNode) =
        container is Or || super.parenthesizeThisWhenInside(container)

    override fun precedence() = 6
    override val children = setOf(requirement, instruction)
  }

  data class Or(val instructions: Set<Instruction>) : Instruction() {
    constructor(vararg instructions: Instruction) : this(instructions.toSet())
    override fun toString() = instructions.map(::groupIfNeeded).joinToString(" OR ")
    override fun parenthesizeThisWhenInside(container: PetsNode): Boolean {
      return container is Then || super.parenthesizeThisWhenInside(container)
    }
    override fun precedence() = 4
    override val children = instructions
  }

  data class Then(val instructions: List<Instruction>) : Instruction() {
    constructor(vararg instructions: Instruction) : this(instructions.toList())
    override fun toString() = instructions.map(::groupIfNeeded).joinToString (" THEN ")
    override fun precedence() = 2
    override val children = instructions
  }

  data class Multi(val instructions: List<Instruction>) : Instruction() {
    constructor(vararg instructions: Instruction) : this(instructions.toList())
    override fun toString() = instructions.map(::groupIfNeeded).joinToString()
    override fun precedence() = 0
    override val children = instructions
  }

  data class Prod(val instruction: Instruction) : Instruction(), ProductionBox<Instruction> {
    override fun toString() = "PROD[$instruction]"
    override val children = setOf(instruction)
    override fun extract() = instruction
  }

  data class Custom(val name: String, val arguments: List<TypeExpression>) : Instruction() {
    override fun toString() = "$$name(${arguments.joinToString()})"
    override val children = arguments
  }

  enum class Intensity(val symbol: String) {
    MANDATORY("!"),
    AMAP("."),
    OPTIONAL("?"),
    ;

    val pets: String = symbol

    companion object {
      fun intensity(symbol: String?) =
          symbol?.let { s -> values().first { it.symbol == s } }
    }
  }
}
