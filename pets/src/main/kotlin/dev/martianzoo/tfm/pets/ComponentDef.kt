package dev.martianzoo.tfm.pets

/**
 * The declaration of a component class, such as GreeneryTile. Models the declaration textually as
 * it was provided. DIRECT INFO ONLY; stuff is inherited among *loaded* classes (PetClasses).
 */
data class ComponentDef( // TODO not sure abt data class after complete is gone
    val name: String,
    val abstract: Boolean = false,
    val supertypes: Set<TypeExpression> = setOf(),
    val dependencies: List<Dependency> = listOf(),
    val effects: Set<Effect> = setOf(),
    val defaults: Set<Instruction> = setOf(),
) : PetsNode() {
  init {
    if (name == rootName) {
      require(supertypes.isEmpty())
      require(dependencies.isEmpty())
    }
  }

  val superclassNames = supertypes.map { it.className }

  // TODO: this should really enforce rules
  override val children = supertypes + dependencies + effects + defaults

  data class Dependency(val type: TypeExpression, val classDep: Boolean = false) : PetsNode() {
    override val children = listOf(type)
  }
}
