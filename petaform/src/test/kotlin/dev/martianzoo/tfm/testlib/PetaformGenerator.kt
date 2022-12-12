package dev.martianzoo.tfm.testlib

import com.google.common.truth.Truth
import dev.martianzoo.tfm.petaform.Action
import dev.martianzoo.tfm.petaform.Action.Cost
import dev.martianzoo.tfm.petaform.Effect
import dev.martianzoo.tfm.petaform.Effect.Trigger
import dev.martianzoo.tfm.petaform.Instruction
import dev.martianzoo.tfm.petaform.Instruction.Custom
import dev.martianzoo.tfm.petaform.Instruction.FromExpression
import dev.martianzoo.tfm.petaform.Instruction.FromIsBelow
import dev.martianzoo.tfm.petaform.Instruction.FromIsNowhere
import dev.martianzoo.tfm.petaform.Instruction.FromIsRightHere
import dev.martianzoo.tfm.petaform.Instruction.Gain
import dev.martianzoo.tfm.petaform.Instruction.Gated
import dev.martianzoo.tfm.petaform.Instruction.Intensity
import dev.martianzoo.tfm.petaform.Instruction.Multi
import dev.martianzoo.tfm.petaform.Instruction.Per
import dev.martianzoo.tfm.petaform.Instruction.Remove
import dev.martianzoo.tfm.petaform.Instruction.Then
import dev.martianzoo.tfm.petaform.Instruction.Transmute
import dev.martianzoo.tfm.petaform.PetaformException
import dev.martianzoo.tfm.petaform.PetaformNode
import dev.martianzoo.tfm.petaform.PetaformParser
import dev.martianzoo.tfm.petaform.Predicate
import dev.martianzoo.tfm.petaform.Predicate.And
import dev.martianzoo.tfm.petaform.Predicate.Exact
import dev.martianzoo.tfm.petaform.Predicate.Max
import dev.martianzoo.tfm.petaform.Predicate.Min
import dev.martianzoo.tfm.petaform.Predicate.Or
import dev.martianzoo.tfm.petaform.QuantifiedExpression
import dev.martianzoo.tfm.petaform.TypeExpression
import dev.martianzoo.util.multiset
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.reflect.KClass

class PetaformGenerator(scaling: (Int) -> Double)
    : RandomGenerator<PetaformNode>(Registry, scaling) {

  constructor(greed: Double = 0.8, backoff: Double = 0.15) : this(scaling(greed, backoff))

  private object Registry : RandomGenerator.Registry<PetaformNode>() {
    init {
      val specSizes = (multiset(8 to 0, 4 to 1, 2 to 2, 1 to 3)) // weight to value
      register {
        TypeExpression(
            randomName(),
            listOfSize(choose(specSizes)),
            refinement()
        )
      }
      register { QuantifiedExpression(choose(1 to null, 3 to recurse()), choose(null, null, 0, 1, 5, 11)) }

      val predicateTypes = (multiset(
          9 to Min::class,
          4 to Max::class,
          2 to Exact::class,
          3 to And::class,
          5 to Or::class,
      ))
      register<Predicate> { recurse(choose(predicateTypes)) }
      register { Min(qe = recurse()) }
      register { Max(recurse()) }
      register { Exact(recurse()) }
      register { Predicate.and(listOfSize(choose(2, 2, 2, 2, 3))) as And }
      register { Predicate.or(listOfSize(choose(2, 2, 2, 2, 2, 3, 4))) as Or }

      fun RandomGenerator<*>.intensity() = choose(3 to null, 1 to randomEnum<Intensity>())

      val instructionTypes = (multiset(
          9 to Gain::class,
          4 to Remove::class,
          2 to Transmute::class,
          2 to Gated::class,
          3 to Per::class,
          1 to Then::class,
          3 to Instruction.Or::class,
          5 to Multi::class,
          1 to Custom::class,
      ))
      register<Instruction> { recurse(choose(instructionTypes)) }
      register { Gain(recurse(), intensity()) }
      register { Remove(recurse(), intensity()) }
      register { Transmute(recurse(), recurse<QuantifiedExpression>().scalar, intensity()) }
      register { Gated(recurse(), recurse()) }
      register { Per(recurse(), recurse()) }
      register { Instruction.then(listOfSize(choose(2, 2, 2, 3))) as Then }
      register { Instruction.multi(listOfSize(choose(2, 2, 2, 2, 2, 3, 4))) as Multi }
      register { Instruction.or(listOfSize(choose(2, 2, 2, 2, 3))) as Instruction.Or }
      register { Custom("name", listOfSize(choose(1, 1, 1, 2))) }

      register<FromExpression> {
        val one: TypeExpression = recurse()
        val two: TypeExpression = recurse()

        fun getTypes(type: TypeExpression): List<TypeExpression> = type.specializations.flatMap { getTypes(it) } + type

        val oneTypes = getTypes(one)
        val twoTypes = getTypes(two)

        val inject: TypeExpression
        val into: TypeExpression
        val target: TypeExpression

        if (oneTypes.size <= twoTypes.size) {
          inject = one
          into = two
          target = twoTypes.random()
        } else {
          inject = two
          into = one
          target = oneTypes.random()
        }

        val b = Random.Default.nextBoolean()

        fun convert(type: TypeExpression): FromExpression {
          if (type == target) {
            return FromIsRightHere(if (b) inject else target, if (b) target else inject)
          }
          val specs = type.specializations.map { convert(it) }
          return if (specs.all { it is FromIsNowhere }) {
            FromIsNowhere(type)
          } else {
            FromIsBelow(type.className, specs, type.predicate)
          }
        }
        convert(into)
      }

      val triggerTypes = (multiset(
          9 to Trigger.OnGain::class,
          5 to Trigger.OnRemove::class,
          2 to Trigger.Conditional::class,
          2 to Trigger.Now::class,
          1 to Trigger.Prod::class,
      ))
      register<Trigger> { recurse(choose(triggerTypes)) }
      register { Trigger.OnGain(recurse()) }
      register { Trigger.OnRemove(recurse()) }
      register { Trigger.Conditional(recurse(), recurse()) }
      register { Trigger.Now(recurse()) }
      register { Trigger.Prod(recurse()) }

      register { Effect(recurse(), recurse()) }

      val costTypes = (multiset(
          9 to Cost.Spend::class,
          3 to Cost.Per::class,
          3 to Cost.Or::class,
          2 to Cost.Multi::class,
          2 to Cost.Prod::class,
      ))
      register<Cost> { recurse(choose(costTypes)) }
      register { Cost.Spend(qe = recurse()) }
      register { Cost.Per(recurse(), recurse()) }
      register { Cost.or(listOfSize(choose(2, 2, 2, 2, 3, 4))) as Cost.Or }
      register { Cost.and(listOfSize(choose(2, 2, 2, 3))) as Cost.Multi }
      register { Cost.Prod(recurse()) }

      register { Action(choose(1 to null, 3 to recurse<Cost>()), recurse<Instruction>()) }
    }

    override fun <T : PetaformNode> invoke(type: KClass<T>, gen: RandomGenerator<PetaformNode>): T? {
      try {
        val result = super.invoke(type, gen)
        if (result!!.countProds() > 1) return null
        return result
      } catch (e: PetaformException) {
        return null // TODO this better
      }
    }

    fun RandomGenerator<PetaformNode>.refinement() = chooseS(9 to { null }, 1 to { recurse<Predicate>() })
    fun RandomGenerator<PetaformNode>.randomName() = choose("Foo", "Bar", "Qux", "Abc", "Xyz", "Ooh", "Ahh", "Eep", "Wau")
  }

  inline fun <reified T : PetaformNode> goNuts(count: Int = 10_000) {
    for (i in 1..count) {
      val node = makeRandomNode<T>()
      val str = node.toString()
      val trip = PetaformParser.parse<T>(str)
      Truth.assertThat(trip.toString()).isEqualTo(str)
      if (trip != node) {
        println(ToKotlin.pp(trip))
        println()
        println(ToKotlin.pp(node))
        Truth.assertThat(trip).isEqualTo(node)
      }
    }
  }

  inline fun <reified T : PetaformNode> findAverageTextLength(): Int {
    val samples = 1000
    val sum = (1..samples).map { makeRandomNode<T>().toString().length }.sum()
    return (sum.toDouble() / samples).roundToInt()
  }

  inline fun <reified T : PetaformNode> printTestStrings(count: Int) {
    for (i in 1..count) {
      println(makeRandomNode<T>())
    }
  }

  inline fun <reified T : PetaformNode> printTestStringOfEachLength(maxLength: Int) {
    getTestStringOfEachLength<T>(maxLength).forEach(::println)
  }

  inline fun <reified T : PetaformNode> getTestStringOfEachLength(maxLength: Int) : List<String> {
    require(maxLength >= 20) // just cause

    val set = sortedSetOf<String>(Comparator.comparing { it.length })

    // Don't track the short strings because some lengths might be impossible
    // If we didn't get to length 9 yet we probably weren't going to.
    var need = maxLength - 10
    var tried = 0
    while (need > 0) {
      if (tried++ == 100_000) error("whoops")
      val s = makeRandomNode<T>().toString()
      if (s.length <= maxLength) {
        if (set.add(s) && s.length > 10) need--
      }
    }
    return set.toList()
  }

  inline fun <reified T : PetaformNode> generateTestApiConstructions(count: Int = 10) {
    for (i in 1..count) {
      val node = makeRandomNode<T>()
      println("assertThat(${ToKotlin.pp(node)}.toString()).isEqualTo($node)")
    }
  }

  inline fun <reified T : PetaformNode> uniqueNodes(
      count: Int = 100, depthLimit: Int = 10, stopAtDrySpell: Int = 200): Set<T> {
    val set = mutableSetOf<T>()
    var drySpell = 0
    while (set.size < count && drySpell < stopAtDrySpell) {
      val node = makeRandomNode<T>()
      if (node.descendants().size <= depthLimit && set.add(node)) {
        drySpell = 0
      } else {
        drySpell++
      }
    }
    return set
  }
}

fun scaling(greed: Double, backoff: Double): (Int) -> Double {
  require(backoff >= 0)
  require(greed > -1.0)
  require(greed < 1.0)
  // must be in range -1..1
  return { (greed + 1) / (backoff + 1).pow(it) - 1 }
}
