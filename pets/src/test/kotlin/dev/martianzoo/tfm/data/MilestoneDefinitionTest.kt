package dev.martianzoo.tfm.data

import com.google.common.truth.Truth.assertThat
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.pets.Requirement
import dev.martianzoo.tfm.pets.Requirement.Min
import dev.martianzoo.tfm.pets.TypeExpression
import org.junit.jupiter.api.Test

class MilestoneDefinitionTest {

  @Test
  fun slurp() {
    val generalist = Canon.milestoneDefinitions["EM1R"]!!
    assertThat(generalist.id).isEqualTo("EM1R")
    assertThat(generalist.replaces).isEqualTo("EM1")

    assertThat(generalist.requirement).isEqualTo(
        Requirement.Prod(
            Requirement.and(listOf(
                Min(scalar = 6),
                Min(TypeExpression("Steel")),
                Min(TypeExpression("Titanium")),
                Min(TypeExpression("Plant")),
                Min(TypeExpression("Energy")),
                Min(TypeExpression("Heat")),
            ))
        )
    )
  }
}
