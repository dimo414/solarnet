package dev.martianzoo.tfm.canon

import com.google.common.truth.Truth.assertThat
import dev.martianzoo.tfm.repl.ReplSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private class CanonCustomInstructionsTest {

  @Test
  fun robinson() {
    val repl = ReplSession(Canon)
    repl.command("newgame BM 3")
    repl.command("become Player1")
    repl.command("exec PROD[Steel, Titanium, Plant, Energy, Heat]")
    repl.command("exec @gainLowestProduction(Player1)")
    // TODO PROD metrics
    assertThat(repl.command("count Production<Class<Megacredit>>").first()).startsWith("6")
  }

  @Test
  fun robinsonCant() {
    val repl = ReplSession(Canon)
    repl.command("newgame BM 3")
    repl.command("become Player1")
    repl.command("exec PROD[Steel, Titanium, Plant, Heat]")
    repl.command("exec @gainLowestProduction(Player1)")

    assertThat(repl.command("has PROD[=5 M, =1 S, =1 T, =1 P, =0 E, =1 H]").first())
        .startsWith("true")

    // TODO make better
    assertThat(repl.command("tasks")).containsExactly("A: [Player1]" +
        " @gainLowestProduction(Player1)" +
        " (OR instructions are abstract:" +
        " Production<Player1, Class<Megacredit>>! OR" +
        " Production<Player1, Class<Energy>>!)")
  }

  @Test
  fun robinson2() {
    val repl = ReplSession(Canon)
    repl.command("newgame BM 3")
    repl.command("become Player1")
    repl.command("exec PROD[-1]")
    repl.command("exec @gainLowestProduction(Player1)")
    assertThat(repl.command("has PROD[=5 Megacredit]").first()).startsWith("true")
  }

  // Robo work test
  // exec PROD[5 Megacredit<Player1>]
  //
  // exec PROD[4 Energy<Player1>]
  //
  // exec StripMine<Player1>
  // // we don't have effects working yet so...
  // exec PROD[-2 Energy<Player1>, 2 Steel<Player1>, Titanium<Player1>]
  //
  // REQUIRE PROD[=2 Energy<Player1>, 2 Steel<Player1>]
  // exec @copyProductionBox(StripMine<Player1>)
  //
  // REQUIRE PROD[=0 Energy<Player1>, 4 Steel<Player1>]
}

// TODO share
private fun assertFails(message: String, shouldFail: () -> Unit) =
    assertThrows<RuntimeException>(message, shouldFail)
