package dev.martianzoo.tfm.repl.commands

import dev.martianzoo.api.SystemClasses
import dev.martianzoo.pets.Parsing
import dev.martianzoo.pets.ast.ClassName
import dev.martianzoo.pets.ast.FromExpression.SimpleFrom
import dev.martianzoo.pets.ast.Instruction
import dev.martianzoo.pets.ast.Instruction.Gain
import dev.martianzoo.pets.ast.Instruction.Multi
import dev.martianzoo.pets.ast.Instruction.Transmute
import dev.martianzoo.tfm.repl.ReplCommand
import dev.martianzoo.tfm.repl.ReplSession

internal class TfmPayCommand(val repl: ReplSession) : ReplCommand("tfm_pay") {
  override val usage: String = "tfm_pay <amount resource>"
  override val help: String = ""
  override fun withArgs(args: String): List<String> {
    val gains: List<Instruction> = Instruction.split(Parsing.parse(args)).instructions

    val ins =
        Multi.create(
            gains.map {
              val sex = (it as Gain).scaledEx
              val currency = sex.expression
              val pay = ClassName.cn("Pay").of(SystemClasses.CLASS.of(currency))
              Transmute(SimpleFrom(pay, currency), sex.scalar)
            })
    return TaskCommand(repl).withArgs(ins.toString())
  }
}
