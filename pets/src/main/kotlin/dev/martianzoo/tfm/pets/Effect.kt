package dev.martianzoo.tfm.pets

import dev.martianzoo.tfm.pets.Instruction.Gated

data class Effect(
    val trigger: Trigger,
    val instruction: Instruction,
    val immediate: Boolean = false,
) : PetsNode() {
  override fun toString(): String {
    val instext = when (instruction) {
      is Gated -> "($instruction)"
      else -> "$instruction"
    }
    return "${trigger}${if (immediate) "::" else ":"} $instext"
  }
  override val children = setOf(trigger, instruction)

  sealed class Trigger : PetsNode() {
    data class OnGain(val expression: TypeExpression) : Trigger() {
      override fun toString() = "$expression"
      override val children = setOf(expression)
    }

    data class OnRemove(val expression: TypeExpression) : Trigger() {
      override fun toString() = "-${expression}"
      override val children = setOf(expression)
    }

    data class Conditional(val trigger: Trigger, val predicate: Predicate) : Trigger() {
      init { if (trigger is Conditional) throw PetsException("And the conditions together instead") }
      override fun toString() = "$trigger IF $predicate"
      override val children = setOf(trigger, predicate)
    }

    data class Now(val predicate: Predicate) : Trigger() {
      override fun toString() = "NOW $predicate"
      override val children = setOf(predicate)
    }

    data class Prod(val trigger: Trigger) : Trigger(), ProductionBox<Trigger> {
      init {
        if (trigger !is OnGain && trigger !is OnRemove) {
          throw PetsException("only gain/remove trigger can go in prod block")
        }
      }
      override fun toString() = "PROD[${trigger}]"
      override val children = setOf(trigger)
      override fun countProds() = super.countProds() + 1
      override fun extract() = trigger
    }
  }
}
