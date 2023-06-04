package dev.martianzoo.tfm.engine.cards

import com.google.common.truth.Truth.assertThat
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.data.GameSetup
import dev.martianzoo.tfm.data.Player.Companion.PLAYER1
import dev.martianzoo.tfm.engine.Engine
import dev.martianzoo.tfm.engine.TerraformingMarsApi.Companion.tfm
import dev.martianzoo.tfm.engine.TestHelpers.assertCounts
import org.junit.jupiter.api.Test

class ManutechTest {

  @Test
  fun manutech() {
    val game = Engine.newGame(GameSetup(Canon, "BMV", 2))
    with(game.tfm(PLAYER1)) {
      playCorp("Manutech", 5)
      assertCounts(1 to "PROD[Steel]", 1 to "Steel")

      operations.initiate("PROD[8, 6T, 7P, 5E, 3H]")
      assertThat(production().values).containsExactly(8, 1, 6, 7, 5, 3).inOrder()
      assertCounts(28 to "M", 1 to "S", 6 to "T", 7 to "P", 5 to "E", 3 to "H")

      operations.initiate("-7 Plant")
      assertCounts(0 to "Plant")

      operations.initiate("Moss")
      assertThat(production().values).containsExactly(8, 1, 6, 8, 5, 3).inOrder()
      assertCounts(28 to "M", 1 to "S", 6 to "T", 0 to "P", 5 to "E", 3 to "H")
    }
  }
}
