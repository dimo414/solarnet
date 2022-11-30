package dev.martianzoo.tfm.data

import com.google.common.truth.Truth.assertThat
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.petaform.api.PetaformNode
import dev.martianzoo.tfm.types.CTypeTable
import org.junit.jupiter.api.Test

// Not testing much, just a bit of the canon data
class CTypeDefinitionTest {
  @Test fun foo() {
    val data = Canon.cTypeDefinitions
    val tr = data["TerraformRating"]!!
    assertThat(tr.name).isEqualTo("TerraformRating")
    assertThat(tr.abstract).isFalse()
    assertThat(tr.supertypesPetaform).containsExactly("Owned<Player>", "Plural").inOrder()
    assertThat(tr.dependenciesPetaform).isEmpty()
    assertThat(tr.effectsPetaform).containsExactly("ProductionPhase: 1", "End: VictoryPoint")
  }

  @Test fun slurp() {
    val table = CTypeTable()
    table.loadAll(Canon.cTypeDefinitions.values)
    table.loadAll(Canon.mapAreaDefinitions.values.flatMap { it })
    table.loadAll(Canon.cardDefinitions.values)
    table.all().forEach { rc ->
      val cc = rc.definition
      if (cc.supertypesPetaform.isNotEmpty()) {
        // checkRoundTrip(cc.supertypesPetaform, rc.superclasses)
      }
      checkRoundTrip(listOfNotNull(cc.immediatePetaform), listOfNotNull(rc.immediate))
      checkRoundTrip(cc.actionsPetaform, rc.actions)
      checkRoundTrip(cc.effectsPetaform, rc.effects)

      // deps??
    }
    assertThat(table.all().size).isGreaterThan(580)
  }

  fun checkRoundTrip(source: Collection<String>, cooked: Collection<PetaformNode>) {
    assertThat(source.size).isEqualTo(cooked.size)
    source.zip(cooked).forEach {
      assertThat("${it.second}").isEqualTo(it.first)
    }
  }
}
