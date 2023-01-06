package dev.martianzoo.tfm.canon

import dev.martianzoo.tfm.data.Authority
import dev.martianzoo.tfm.data.CardDefinition
import dev.martianzoo.tfm.data.ClassDeclaration
import dev.martianzoo.tfm.data.JsonReader
import dev.martianzoo.tfm.data.MarsAreaDefinition
import dev.martianzoo.tfm.data.MilestoneDefinition
import dev.martianzoo.tfm.pets.PetsParser.parseComponents
import dev.martianzoo.tfm.pets.ast.Instruction.CustomInstruction
import dev.martianzoo.util.Grid

object Canon : Authority() {

  override val explicitClassDeclarations: Collection<ClassDeclaration> by lazy {
    EXPLICIT_CLASS_FILENAMES.flatMap {
      parseComponents(readResource(it))
    }
  }

  override val mapAreaDefinitions: Map<String, Grid<MarsAreaDefinition>> by lazy {
    JsonReader.readMaps(readResource("maps.json5"))
  }

  override val cardDefinitions: Collection<CardDefinition> by lazy {
    JsonReader.readCards(readResource("cards.json5"))
  }

  override val milestoneDefinitions: Collection<MilestoneDefinition> by lazy {
    JsonReader.readMilestones(readResource("milestones.json5"))
  }

  override val customInstructions: Map<String, CustomInstruction> by lazy {
      //when (name) {
      //"createMarsAreas" -> {
      //  object : CustomInstruction {
      //    override val name = "createMarsAreas"
      //    override fun translate(game: GameApi, types: List<TypeExpression>): Instruction {
      //      return Instruction.Multi(
      //          mapAreaDefinitions.keys.filter {
      //            it.startsWith("Tharsis")
      //          }.map { Gain(te(it)) })
      //    }
      //  }
      //}
    mapOf()
  }

  private val EXPLICIT_CLASS_FILENAMES = setOf("system.pets", "components.pets", "player.pets")

  private fun readResource(filename: String): String {
    val dir = javaClass.packageName.replace('.', '/')
    return javaClass.getResource("/$dir/$filename")!!.readText()
  }

  enum class Bundle(val id: Char) {
    BASE('B'),
    CORPORATE_ERA('R'),  // well the letter R appears 3 times so...
    THARSIS('M'),        // for "map", ooh
    HELLAS('H'),
    ELYSIUM('E'),
    VENUS_NEXT('V'),
    PRELUDE('P'),
    COLONIES('C'),
    TURMOIL('T'),
    PROMOS('X'),
  }
}
