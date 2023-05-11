package dev.martianzoo.tfm.engine

import dev.martianzoo.tfm.api.Exceptions.AbstractException
import dev.martianzoo.tfm.api.Exceptions.ExistingDependentsException
import dev.martianzoo.tfm.api.Exceptions.NotNowException
import dev.martianzoo.tfm.api.Exceptions.TaskException
import dev.martianzoo.tfm.api.GameReader
import dev.martianzoo.tfm.api.TypeInfo
import dev.martianzoo.tfm.data.GameEvent
import dev.martianzoo.tfm.data.GameEvent.ChangeEvent
import dev.martianzoo.tfm.data.GameEvent.ChangeEvent.Cause
import dev.martianzoo.tfm.data.GameEvent.TaskEvent
import dev.martianzoo.tfm.data.GameEvent.TaskRemovedEvent
import dev.martianzoo.tfm.data.Player
import dev.martianzoo.tfm.data.Player.Companion.ENGINE
import dev.martianzoo.tfm.data.Task
import dev.martianzoo.tfm.data.Task.TaskId
import dev.martianzoo.tfm.data.TaskResult
import dev.martianzoo.tfm.engine.ActiveEffect.FiredEffect
import dev.martianzoo.tfm.engine.Component.Companion.toComponent
import dev.martianzoo.tfm.engine.Game.ComponentGraph
import dev.martianzoo.tfm.engine.Game.EventLog
import dev.martianzoo.tfm.engine.Game.EventLog.Checkpoint
import dev.martianzoo.tfm.engine.Game.TaskQueue
import dev.martianzoo.tfm.pets.ast.Expression
import dev.martianzoo.tfm.pets.ast.Instruction
import dev.martianzoo.tfm.types.MClass
import dev.martianzoo.tfm.types.MClassTable
import dev.martianzoo.tfm.types.MType
import dev.martianzoo.util.Multiset

/**
 * The mutable state of a game in progress. This state is the aggregation of three mutable child
 * objects, which callers accesses directly: a [ComponentGraph], a [TaskQueue], and an [EventLog].
 * These types don't expose mutation operations, but the objects are mutable and always represent
 * the most current state.
 *
 * To read game state at a higher level (e.g. via Pets expressions), use [reader]. To change state
 * use [writer].
 */
public class Game
internal constructor(
    private val table: MClassTable,
    private val writableEvents: WritableEventLog = WritableEventLog(),
    private val writableComponents: WritableComponentGraph = WritableComponentGraph(),
    private val writableTasks: WritableTaskQueue = WritableTaskQueue(),
) {
  /** The components that make up the game's current state ("present"). */
  public val components: ComponentGraph = writableComponents

  /** The tasks the game is currently waiting on ("future"). */
  public val tasks: TaskQueue = writableTasks

  /** Everything that has happened in this game so far ("past"). */
  public val events: EventLog = writableEvents

  /**
   * A multiset of [Component] instances; the "present" state of a game in progress. It is a plain
   * multiset, but called a "graph" because these component instances have references to their
   * dependencies which are also stored in the multiset.
   */
  public interface ComponentGraph {
    /**
     * Does at least one instance of [component] exist currently? (That is, is [countComponent]
     * nonzero?
     */
    operator fun contains(component: Component): Boolean

    /** How many instances of the exact component [component] currently exist? */
    fun countComponent(component: Component): Int

    /** How many total component instances have the type [parentType] (or any of its subtypes)? */
    fun count(parentType: MType, info: TypeInfo): Int

    /**
     * Returns all component instances having the type [parentType] (or any of its subtypes), as a
     * multiset. The size of the returned collection will be `[count]([parentType])` . If
     * [parentType] is `Component` this will return the entire component multiset.
     */
    fun getAll(parentType: MType, info: TypeInfo): Multiset<Component>

    fun findLimit(gaining: Component?, removing: Component?): Int
  }

  /**
   * A complete record of everything that happened in a particular game (in progress or finished). A
   * complete game state could be reconstructed by replaying these events.
   */
  public interface EventLog {
    val size: Int

    public data class Checkpoint(internal val ordinal: Int) {
      init {
        require(ordinal >= 0)
      }
    }

    /**
     * Returns a [Checkpoint] that can be passed to [Game.rollBack] to return the game to its
     * present state, or to any of the `-Since` methods.
     */
    fun checkpoint(): Checkpoint

    /** Returns all change events since game setup was concluded. */
    fun changesSinceSetup(): List<ChangeEvent>

    /** Returns all change events since [checkpoint]. */
    fun changesSince(checkpoint: Checkpoint): List<ChangeEvent>

    /** Returns the ids of all tasks created since [checkpoint] that still exist. */
    fun newTasksSince(checkpoint: Checkpoint): Set<TaskId>

    fun entriesSince(checkpoint: Checkpoint): List<GameEvent>

    fun activitySince(checkpoint: Checkpoint): TaskResult
  }

  /**
   * Contains tasks: what the game is waiting on someone to do. Each task is owned by some [Player]
   * (which could be the engine itself). Normally, a state should never been observed in which
   * engine tasks remain, as the engine should always be able to take care of them itself before
   * returning.
   *
   * This interface speaks entirely in terms of [TaskId]s.
   */
  public interface TaskQueue : Set<Task> {
    fun ids(): Set<TaskId>

    operator fun contains(id: TaskId): Boolean
    operator fun get(id: TaskId): Task

    fun nextAvailableId(): TaskId

    fun preparedTask(): TaskId?
  }

  // Don't allow actual game logic to depend on the event log
  public val reader: GameReader = GameReaderImpl(table, components)

  internal val transformers by table::transformers

  public fun writer(player: Player): GameWriter = GameWriterImpl(this, player)

  public fun resolve(expression: Expression): MType = table.resolve(expression)

  public fun checkpoint() = events.checkpoint()

  public fun rollBack(checkpoint: Checkpoint) {
    writableEvents.rollBack(checkpoint) {
      when (it) {
        is TaskEvent -> writableTasks.reverse(it)
        is ChangeEvent -> {
          writableComponents.reverse(
              it.change.count,
              removeWhatWasGained = it.change.gaining?.toComponent(reader),
              gainWhatWasRemoved = it.change.removing?.toComponent(reader),
          )
        }
      }
    }
  }

  public fun doAtomic(block: () -> Unit): TaskResult {
    val checkpoint = checkpoint()
    return try {
      block()
      events.activitySince(checkpoint)
    } catch (e: Exception) {
      rollBack(checkpoint)
      throw e
    }
  }

  internal fun activeEffects(classes: Collection<MClass>): List<ActiveEffect> =
      writableComponents.activeEffects(classes)

  internal fun setupFinished() = writableEvents.setStartPoint()

  internal fun addTriggeredTasks(fired: List<FiredEffect>) =
      fired.forEach { writableTasks.addTasksFrom(it, writableEvents) }

  internal fun getComponents(type: Expression): Multiset<Component> =
      components.getAll(resolve(type), reader)

  /*
   * Implementation of GameWriter - would be nice to have in a separate file but we'd have to
   * make some things in Game non-private.
   */
  internal class GameWriterImpl(val game: Game, private val player: Player) :
      GameWriter(), UnsafeGameWriter {
    override fun session() = PlayerSession(game, this, player)

    override fun prepareTask(taskId: TaskId): Boolean {
      val already = game.tasks.preparedTask()
      if (already == taskId) return true
      if (already != null) {
        throw NotNowException("already-prepared task hasn't executed yet: $already")
      }

      val task: Task = game.tasks[taskId]
      checkOwner(task) // TODO use myTasks() instead?

      val prepared = Instructor(this, player).prepare(task.instruction)
      if (prepared == null) {
        game.writableTasks.removeTask(taskId, game.writableEvents)
        return false
      }
      val replacement = task.copy(instruction = prepared, next = true, whyPending = null) // TODO
      game.writableTasks.replaceTask(replacement, game.writableEvents)
      return true
    }

    override fun tryTask(taskId: TaskId, narrowed: Instruction?): TaskResult {
      var message: String? = null
      val result =
          try {
            game.doAtomic {
              if (prepareTask(taskId)) {
                doTask(taskId, narrowed)
              }
              return@doAtomic
            }
          } catch (e: ExistingDependentsException) {
            error("this should not have happened: $e")
          } catch (e: TaskException) {
            return TaskResult(listOf(), setOf())
          } catch (e: NotNowException) {
            message = e.message
            TaskResult(listOf(), setOf())
          } catch (e: AbstractException) {
            message = e.message
            TaskResult(listOf(), setOf())
          }
      if (message == null) return result
      val newTask = game.tasks[taskId]
      val explainedTask = newTask.copy(whyPending = message)
      game.writableTasks.replaceTask(explainedTask, game.writableEvents)
      return result
    }

    override fun doTask(taskId: TaskId, narrowed: Instruction?): TaskResult {
      prepareTask(taskId)
      val nrwd: Instruction? = narrowed?.let { session().preprocess(it) }
      val task = game.tasks[taskId]
      checkOwner(task)
      nrwd?.ensureNarrows(task.instruction, game.reader)
      return game.doAtomic {
        val instructor = Instructor(this, player, task.cause)
        val prepared = instructor.prepare(nrwd ?: task.instruction)
        prepared?.let(instructor::execute)
        task.then?.let { addTasks(it, task.owner, task.cause) }
        removeTask(taskId)
      }
    }

    // Danger

    override fun addTask(instruction: Instruction, initialCause: Cause?): TaskId {
      // require(game.tasks.none()) { game.tasks.joinToString("\n")} TODO enable??
      val events = addTasks(instruction, player, initialCause)
      return events.single().task.id
    }

    override fun removeTask(taskId: TaskId): TaskRemovedEvent {
      checkOwner(game.tasks[taskId])
      return game.writableTasks.removeTask(taskId, game.writableEvents)
    }

    /**
     * Updates the component graph and event log, but does not fire triggers. This exists as a
     * public method so that a broken game state can be fixed, or a game state broken on purpose, or
     * specific game scenario set up very explicitly.
     */
    fun updateAndLog(
        count: Int,
        gaining: Component?,
        removing: Component?,
        cause: Cause?,
    ): ChangeEvent? {
      removing?.let { (game.components as WritableComponentGraph).checkDependents(count, it) }
      val change = game.writableComponents.reallyUpdate(count, gaining, removing)
      return game.writableEvents.addChangeEvent(change, player, cause)
    }

    internal fun addTasks(instruction: Instruction, owner: Player, cause: Cause?) =
        game.writableTasks.addTasksFrom(instruction, owner, cause, game.writableEvents)

    internal fun fixDependentsUpdateAndLog(
        count: Int = 1,
        gaining: Component? = null,
        removing: Component? = null,
        cause: Cause? = null,
    ): TaskResult {
      val cp = game.checkpoint()
      fun tryIt() = updateAndLog(count, gaining, removing, cause)
      try {
        tryIt()
      } catch (e: ExistingDependentsException) {
        // TODO better way to remove dependents?
        e.dependents.forEach {
          val cpt = it.toComponent(game.reader)
          val ct = game.reader.countComponent(cpt.mtype)
          fixDependentsUpdateAndLog(ct, removing = cpt, cause = cause)
        }
        tryIt()
      }
      return game.events.activitySince(cp)
    }

    private fun checkOwner(task: Task) {
      if (player != task.owner && player != ENGINE) {
        throw TaskException("$player can't access task owned by ${task.owner}")
      }
    }
  }
}
