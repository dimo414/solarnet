package dev.martianzoo.tfm.data

import com.squareup.moshi.Json
import dev.martianzoo.tfm.petaform.Component
import dev.martianzoo.tfm.petaform.classNamePattern

/**
 * The declaration of a component class, such as GreeneryTile. Models the declaration textually as
 * it was provided.
 */
data class ComponentDefinition(
    /** Unique name for this component class. */
    val name: String,

    /** If `true`, types are always abstract, even when all dependencies are concrete. Not inherited. */
    val abstract: Boolean = false,

    /**
     * Zero or more direct supertypes, including specializations, as Petaform TypeExpressions. Don't
     * include `Component` or any types that are already indirect supertypes (unless specializing them).
     */
    @Json(name = "supertypes")
    val supertypesPetaform: Set<String> = setOf(),

    /**
     * Dependencies declared for this component class; it will also inherit the dependencies of its
     * supertypes but those are not listed here.
     */
    @Json(name = "dependencies")
    val dependenciesPetaform: List<String> = listOf(),

    @Json(name = "immediate")
    val immediatePetaform: String? = null,

    @Json(name = "actions")
    val actionsPetaform: Set<String> = setOf(),

    /**
     * Zero or more unordered effects that belong to each *instance* of this component class,
     * expressed in Petaform. If the exact name of a dependency type is used in an effect, and a
     * subtype of this type specializes that dependency, then its inherited copy of this effect will
     * have that type specialized in the same way. These petaform expressions can (and should) make
     * use of `This` and `Me` and can rely on type defaults.
     */
    @Json(name = "effects")
    val effectsPetaform: Set<String> = setOf(),
) : Definition {

  init {
    require(name.matches(classNamePattern()))
  }

  override val asComponentDefinition = this

  companion object {
    fun from(component: Component): ComponentDefinition {
      return ComponentDefinition(
          component.expression.className,
          component.abstract,
          component.supertypes.map(Any::toString).toSet(),
          component.expression.specializations.map(Any::toString),
          null,
          component.actions.map(Any::toString).toSet(),
          component.effects.map(Any::toString).toSet(),
      )
    }
  }
}
