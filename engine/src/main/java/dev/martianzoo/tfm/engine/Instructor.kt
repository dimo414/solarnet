package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.api.CustomClass
import dev.martianzoo.tfm.api.Exceptions.DeadEndException
import dev.martianzoo.tfm.api.Exceptions.DependencyException
import dev.martianzoo.tfm.api.Exceptions.LimitsException
import dev.martianzoo.tfm.api.Exceptions.NotNowException
import dev.martianzoo.tfm.api.Exceptions.abstractInstruction
import dev.martianzoo.tfm.api.Exceptions.orWithoutChoice
import dev.martianzoo.tfm.api.Exceptions.requirementNotMet
import dev.martianzoo.tfm.api.SpecialClassNames.DIE
import dev.martianzoo.tfm.data.GameEvent.ChangeEvent.Cause
import dev.martianzoo.tfm.data.Task
import dev.martianzoo.tfm.engine.Component.Companion.toComponent
import dev.martianzoo.tfm.engine.Game.GameWriterImpl
import dev.martianzoo.tfm.engine.Game.SnReader
import dev.martianzoo.tfm.pets.PetTransformer.Companion.chain
import dev.martianzoo.tfm.pets.Transforming.replaceOwnerWith
import dev.martianzoo.tfm.pets.ast.Expression
import dev.martianzoo.tfm.pets.ast.Instruction
import dev.martianzoo.tfm.pets.ast.Instruction.Change
import dev.martianzoo.tfm.pets.ast.Instruction.Change.Companion.change
import dev.martianzoo.tfm.pets.ast.Instruction.Companion.split
import dev.martianzoo.tfm.pets.ast.Instruction.Gated
import dev.martianzoo.tfm.pets.ast.Instruction.Intensity.AMAP
import dev.martianzoo.tfm.pets.ast.Instruction.Intensity.MANDATORY
import dev.martianzoo.tfm.pets.ast.Instruction.Multi
import dev.martianzoo.tfm.pets.ast.Instruction.NoOp
import dev.martianzoo.tfm.pets.ast.Instruction.Or
import dev.martianzoo.tfm.pets.ast.Instruction.Per
import dev.martianzoo.tfm.pets.ast.Instruction.Then
import dev.martianzoo.tfm.pets.ast.Instruction.Transform
import dev.martianzoo.tfm.pets.ast.ScaledExpression.Scalar.ActualScalar
import dev.martianzoo.tfm.types.Dependency.TypeDependency
import dev.martianzoo.tfm.types.MType
import dev.martianzoo.util.Hierarchical.Companion.lub
import kotlin.math.min

/** Just a cute name for "instruction handler". It prepares and executes instructions. */
internal data class Instructor(
  private val writer: GameWriterImpl, // makes sense as inner class but file would be so long
  private val reader: SnReader,
  private val effector: Effector,
  private val limiter: (Component?, Component?) -> Int,
) {

  fun execute(instruction: Instruction, cause: Cause?): List<Task> =
      mutableListOf<Task>().also { doExecute(instruction, cause, it) } // TODO prepare?

  private fun doExecute(instruction: Instruction, cause: Cause?, deferred: MutableList<Task>) {
    val prepped = prepare(instruction) // idempotent?
    when (prepped) {
      is Change -> executeChange(prepped, cause, deferred)
      is Then -> prepped.instructions.forEach { doExecute(it, cause, deferred) }
      is Or -> throw orWithoutChoice(prepped)
      is NoOp -> {}
      else -> error("somehow a ${prepped.kind.simpleName!!} was enqueued: $prepped")
    }
  }

  private fun executeChange(change: Change, cause: Cause?, deferred: MutableList<Task>) {
    val ct = change.count as? ActualScalar ?: throw abstractInstruction(change)
    if (change.intensity != MANDATORY) throw abstractInstruction(change)
    val gaining = change.gaining?.toComponent(reader)
    val removing = change.removing?.toComponent(reader)
    if (gaining?.mtype?.root?.custom != null) error("custom")

    val changes = writer.change(ct.value, gaining, removing, cause).changes
    changes.forEach {
      val (now, later) = effector.fire(it, reader).partition { it.next }
      for (task in now) {
        split(task.instruction).forEach { doExecute(it, task.cause, deferred) }
      }
      deferred += later
    }
  }

  /**
   * Returns a narrowed form of [unprepared] based on the current game state (but changes no game
   * state itself). The returned instruction *must* be executed against this very same game state
   * (i.e., must be the next one executed. The returned instruction might still be abstract.
   *
   * Preparing iterates to a fixed point. Examples of preparing:
   * * Replaces inert instructions with `Ok`
   * * Auto-narrows gained and removed types to the extent possible
   * * Modifies a `?` or `.` change based on limits (upgrading `.` to `!`)
   * * Validates and removes "gates"
   * * Evaluates a metric in a [Per] instruction, multiplying the inner instruction appropriately
   * * Prepares each option of an [Or] or [Then] (TODO what if gets separated?)
   * * If gaining a *concrete* custom type, rewrites to the result of [CustomClass.translate] *
   */
  fun prepare(unprepared: Instruction) = doPrepare(unprepared)

  private fun doPrepare(unprepared: Instruction): Instruction {
    return when (unprepared) {
      is NoOp -> NoOp
      is Change -> prepareChange(unprepared)
      is Per -> doPrepare(unprepared.inner * reader.count(unprepared.metric))
      is Gated -> {
        if (reader.evaluate(unprepared.gate)) {
          doPrepare(unprepared.inner)
        } else if (unprepared.mandatory) {
          throw requirementNotMet(unprepared.gate)
        } else {
          NoOp
        }
      }
      is Or -> {
        val options = unprepared.instructions.nonThrowing(::doPrepare)
        if (options.none()) throw NotNowException("all OR options are impossible at this time")
        Or.create(options.map(::doPrepare))
      }
      is Then -> Then.create(unprepared.instructions.map(::doPrepare).filter { it != NoOp })
      is Multi -> error("")
      is Transform -> error("should have been transformed already: $unprepared")
    }
  }

  private fun <T : Any> Iterable<T>.nonThrowing(block: (T) -> Unit) = filter {
    try {
      block(it)
      true
    } catch (e: Exception) {
      false
    }
  }

  private fun prepareChange(change: Change): Instruction {
    // can't prepare at all if we still have an X?
    val count = (change.count as? ActualScalar)?.value ?: return change

    val (g: MType?, r: MType?) = autoNarrowTypes(change.gaining, change.removing)
    if (g?.className == DIE) throw DeadEndException("a Die instruction was reached")

    if (g != null && !g.abstract && g.root.custom != null) {
      require(r == null) { "custom class instructions can only be pure gains" }
      return prepareCustom(g)
    }

    val intens = change.intensity ?: error("$change")

    if (listOfNotNull(g, r).any { it.abstract }) {
      // Still abstract, don't check limits yet
      return change(count, g?.expression, r?.expression, intens)
    }

    val limit = limiter(g?.toComponent(reader), r?.toComponent(reader))
    val adjusted: Int = min(count, limit)

    if (intens == MANDATORY && adjusted != count) {
      val mesg =
          if (g != null) {
            if (r == null) {
              "gain $count ${g.expression}"
            } else {
              "transmute $count ${r.expression} into ${g.expression}"
            }
          } else {
            "remove $count ${r!!.expression}"
          }
      throw LimitsException("Can't $mesg: max possible is $adjusted")
    }

    return change(adjusted, g?.expression, r?.expression, if (intens == AMAP) MANDATORY else intens)
  }

  private fun prepareCustom(type: MType): Instruction {
    val translated = type.root.custom!!.prepare(reader, type)

    val prepped =
        chain(
                reader.transformers.standardPreprocess(),
                reader.transformers.substituter(type.root.baseType, type),
                type.owner?.let { replaceOwnerWith(it) },
            )
            .transform(translated)

    return if (prepped is Multi) prepped else doPrepare(prepped) // TODO hmm?
  }

  private fun autoNarrowTypes(gaining: Expression?, removing: Expression?): Pair<MType?, MType?> {
    var g: MType? = gaining?.let(reader::resolve)
    var r: MType? = removing?.let(reader::resolve)

    if (g?.abstract == true) {
      val lubs: List<Pair<MType, TypeDependency?>> =
          g.dependencies.typeDependencies.map { x ->
            x.boundType to
                lub(reader.getComponents(x.boundType).elements)?.let { x.copy(boundType = it) }
          }
      val missing = lubs.filter { it.second == null }.map { it.first }
      if (missing.any()) throw DependencyException(missing)

      // g = g.root.withAllDependencies(DependencySet.of(lubs.map { it.second!! }))
      g = g.allConcreteSubtypes().singleOrNull() ?: g

    }
    if (r?.abstract == true) {
      // Infer a type if there IS only one kind of component that has it
      // TODO could be smarter, like if the instr is mandatory and only one cpt type can satisfy
      r = reader.getComponents(r).singleOrNull() ?: r
    }
    return g to r
  }
}
