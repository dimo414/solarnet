package dev.martianzoo.tfm.repl

import dev.martianzoo.tfm.api.GameSetup
import dev.martianzoo.tfm.canon.Canon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SpecificCardsTest {
  @Test
  fun sulphurEating() {
    val repl = ReplSession(Canon, GameSetup(Canon, "BRMV", 2))
    repl.test("become P1")
    repl.test("exec ProjectCard")
    repl.test("exec SulphurEatingBacteria")
    repl.test("exec UseAction1<SulphurEatingBacteria>")
    repl.assertCount("Microbe", 1)

    repl.test(
        "exec UseAction2<SulphurEatingBacteria>",
        "task A -Microbe<SulphurEatingBacteria> THEN 3")
    repl.assertCount("Microbe", 0)

    repl.test("exec 4 UseAction1<SulphurEatingBacteria>")
    repl.assertCount("Microbe", 4)

    repl.test("exec UseAction2<SulphurEatingBacteria>", 1)

    assertThrows<Exception>("greed") { repl.test("task A -Microbe<C251> THEN 4" ) }
    assertThrows<Exception>("shortchanged") { repl.test("task A -Microbe<C251> THEN 2" ) }
    assertThrows<Exception>("no get paid") { repl.test("task A -Microbe<C251>" ) }
    assertThrows<Exception>("which microbe") { repl.test("task A -3 Microbe THEN 9" ) }
    assertThrows<Exception>("more than have") { repl.test("task A -5 Microbe<C251> THEN 15" ) }
    assertThrows<Exception>("x can't be zero") { repl.test("task A -0 Microbe<C251> THEN 0" ) }
    assertThrows<Exception>("what kind") { repl.test("task A -3 Resource<C251> THEN 9" ) }
    assertThrows<Exception>("out of order") { repl.test("task A 9 THEN -3 Microbe<C251>" ) }
    assertThrows<Exception>("inverse") { repl.test("task A 2 Microbe<C251> THEN -6" ) }
    repl.assertCount("Microbe", 4)

    repl.test("task A -3 Microbe<C251> THEN 9")
    repl.assertCount("Microbe", 1)
  }
}
