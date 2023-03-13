package dev.martianzoo.tfm.repl

import com.google.common.truth.Truth.assertThat
import dev.martianzoo.tfm.api.GameSetup
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.engine.Exceptions.DependencyException
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn
import dev.martianzoo.tfm.pets.ast.Expression.Companion.expression
import dev.martianzoo.tfm.pets.ast.Instruction.Companion.instruction
import dev.martianzoo.tfm.pets.ast.Metric.Companion.metric
import dev.martianzoo.tfm.pets.ast.Requirement.Companion.requirement
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private class InteractiveSessionTest {
  @Test
  fun test() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "MB", 2))
    session.becomePlayer(cn("Player2"))

    session.initiateAndQueue(instruction("PROD[5, 4 Energy]"))
    session.initiateAndQueue(instruction("StripMine"))
    session.initiateAndQueue(instruction("PROD[-2 Energy, 2 Steel, Titanium]"))

    assertThat(session.has(requirement("PROD[=2 Energy, =2 Steel]"))).isTrue()

    session.becomePlayer(cn("Player1"))
    assertThat(session.has(requirement("PROD[=0 Energy, =0 Steel]"))).isTrue()
  }

  @Test
  fun shortNames() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "MB", 2))
    session.becomePlayer(cn("P2"))

    session.initiateAndQueue(instruction("PROD[5, 4 E]"))
    session.initiateAndQueue(instruction("C138"))
    session.initiateAndQueue(instruction("PROD[-2 E, 2 S, T]"))

    assertThat(session.has(requirement("PROD[=2 E, =2 S]"))).isTrue()

    session.becomePlayer(cn("P1"))
    assertThat(session.has(requirement("PROD[=0 E, =0 S]"))).isTrue()
  }

  @Test
  fun removeAmap() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "MB", 2))
    session.becomePlayer(cn("Player1"))

    session.initiateAndQueue(instruction("3 Heat!"))
    session.initiateAndQueue(instruction("4 Heat."))
    session.initiateAndQueue(instruction("-9 Heat."))
    assertThat(session.count(metric("Heat"))).isEqualTo(0)
  }

  @Test
  fun krash() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "BRHVPX", 3))
    session.list(expression("System"))
  }

  @Test
  fun rollback() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "MB", 2))
    session.becomePlayer(cn("Player1"))

    session.initiateAndQueue(instruction("3 Heat"))
    session.initiateAndQueue(instruction("4 Heat"))
    assertThat(session.count(metric("Heat"))).isEqualTo(7)

    val checkpoint = session.game!!.eventLog.checkpoint()
    session.initiateAndQueue(instruction("-6 Heat"))
    assertThat(session.count(metric("Heat"))).isEqualTo(1)

    session.rollBack(checkpoint.ordinal)
    assertThat(session.count(metric("Heat"))).isEqualTo(7)
  }

  @Test
  fun dependencies() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "MB", 2))
    session.becomePlayer(cn("Player2"))

    assertThat(session.game!!.taskQueue.taskMap).isEmpty()
    assertThat(session.count(metric("Microbe"))).isEqualTo(0)

    session.initiateAndAutoExec(instruction("4 OxygenStep"))
    assertThat(session.count(metric("OxygenStep"))).isEqualTo(4)
    session.initiateAndAutoExec(instruction("Ants"))
    assertThat(session.game!!.taskQueue.taskMap.values).isEmpty()
    assertThat(session.count(metric("Ants"))).isEqualTo(1)
    session.initiateAndAutoExec(instruction("3 Microbe<Ants>"))
    assertThat(session.count(metric("Microbe"))).isEqualTo(3)
    assertThrows<DependencyException> { session.initiateAndAutoExec(instruction("-Ants")) }
  }

  @Test
  fun counting() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "MB", 2))
    session.becomePlayer(cn("Player2"))
    session.initiateAndQueue(instruction("42 Heat"))
    assertThat(session.count(metric("Heat"))).isEqualTo(42)
    assertThat(session.count(metric("4 Heat"))).isEqualTo(10)
    assertThat(session.count(metric("42 Heat"))).isEqualTo(1)
    assertThat(session.count(metric("43 Heat"))).isEqualTo(0)
    assertThat(session.count(metric("Heat MAX 50"))).isEqualTo(42)
    assertThat(session.count(metric("Heat MAX 42"))).isEqualTo(42)
    assertThat(session.count(metric("Heat MAX 41"))).isEqualTo(41)
    assertThat(session.count(metric("Heat MAX 1"))).isEqualTo(1)
    assertThat(session.count(metric("Heat MAX 0"))).isEqualTo(0)
    assertThat(session.count(metric("4 Heat MAX 10"))).isEqualTo(10)
    assertThat(session.count(metric("4 Heat MAX 9"))).isEqualTo(9)
  }

  @Test
  fun tempTrigger() {
    val session = InteractiveSession()
    session.newGame(GameSetup(Canon, "MB", 2))

    session.becomePlayer(cn("Player1"))
    assertThat(session.count(metric("TerraformRating"))).isEqualTo(20)

    session.initiateAndAutoExec(instruction("2 TemperatureStep"))
    assertThat(session.count(metric("TemperatureStep"))).isEqualTo(2)
    assertThat(session.count(metric("TerraformRating"))).isEqualTo(22)
    assertThat(session.count(metric("Production<Class<Heat>>"))).isEqualTo(0)

    session.initiateAndAutoExec(instruction("2 TemperatureStep"))
    assertThat(session.count(metric("TerraformRating"))).isEqualTo(24)
    assertThat(session.count(metric("Production<Class<Heat>>"))).isEqualTo(1)

    session.initiateAndAutoExec(instruction("8 OxygenStep"))
    assertThat(session.count(metric("TerraformRating"))).isEqualTo(33)
    assertThat(session.count(metric("Production<Class<Heat>>"))).isEqualTo(2)
  }
}
