package dev.martianzoo.tfm.data

import com.squareup.moshi.Json

/**
 * The declaration of a component class, such as GreeneryTile. Models the declaration textually as
 * it was provided.
 */
data class CTypeData(
    /** Unique name for this component class. */
    val name: String,

    /** If `true`, types are always abstract, even when all dependencies are concrete. Not inherited. */
    val abstract: Boolean = false,

    /**
     * Zero or more direct supertypes, including specializations, as Petaform Expressions. Don't
     * include `Component` or any types that are already indirect supertypes (unless specializing them).
     */
    @Json(name = "supertypes")
    val supertypesPetaform: Set<String> = setOf(),

    /**
     * Zero or more direct dependencies; a dependency is a Petaform Expression with the requirement
     * that every instance of this class must relate to *exactly one* instance of the dependency
     * type. This relationship may be many-to-one. Supertype dependencies are inherited so should
     * never be restated here.
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
) : TfmData {

  init {
    require(name.matches(CTYPE_PATTERN))
  }

  override val asRawComponentType = this
}
