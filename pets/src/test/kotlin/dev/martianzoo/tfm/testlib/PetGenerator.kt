package dev.martianzoo.tfm.testlib

import com.google.common.collect.ImmutableMultiset
import com.google.common.truth.Truth.assertWithMessage
import dev.martianzoo.tfm.pets.Parsing.parsePets
import dev.martianzoo.tfm.pets.PetException
import dev.martianzoo.tfm.pets.SpecialClassNames.MEGACREDIT
import dev.martianzoo.tfm.pets.ast.Action
import dev.martianzoo.tfm.pets.ast.Action.Cost
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn
import dev.martianzoo.tfm.pets.ast.Effect
import dev.martianzoo.tfm.pets.ast.Effect.Trigger
import dev.martianzoo.tfm.pets.ast.FromExpression
import dev.martianzoo.tfm.pets.ast.FromExpression.ComplexFrom
import dev.martianzoo.tfm.pets.ast.FromExpression.SimpleFrom
import dev.martianzoo.tfm.pets.ast.FromExpression.TypeInFrom
import dev.martianzoo.tfm.pets.ast.Instruction
import dev.martianzoo.tfm.pets.ast.Instruction.Custom
import dev.martianzoo.tfm.pets.ast.Instruction.Gain
import dev.martianzoo.tfm.pets.ast.Instruction.Gated
import dev.martianzoo.tfm.pets.ast.Instruction.Intensity
import dev.martianzoo.tfm.pets.ast.Instruction.Per
import dev.martianzoo.tfm.pets.ast.Instruction.Remove
import dev.martianzoo.tfm.pets.ast.Instruction.Then
import dev.martianzoo.tfm.pets.ast.Instruction.Transmute
import dev.martianzoo.tfm.pets.ast.PetNode
import dev.martianzoo.tfm.pets.ast.Requirement
import dev.martianzoo.tfm.pets.ast.Requirement.Exact
import dev.martianzoo.tfm.pets.ast.Requirement.Max
import dev.martianzoo.tfm.pets.ast.Requirement.Min
import dev.martianzoo.tfm.pets.ast.ScalarAndType
import dev.martianzoo.tfm.pets.ast.ScalarAndType.Companion.sat
import dev.martianzoo.tfm.pets.ast.TypeExpression
import dev.martianzoo.tfm.pets.ast.TypeExpression.ClassLiteral
import dev.martianzoo.tfm.pets.ast.TypeExpression.GenericTypeExpression
import dev.martianzoo.tfm.testlib.PetToKotlin.p2k
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.reflect.KClass
import org.junit.jupiter.api.Assertions.fail

internal class PetGenerator(scaling: (Int) -> Double) :
    RandomGenerator<PetNode>(Registry, scaling) {

  constructor(greed: Double = 0.8, backoff: Double = 0.15) : this(scaling(greed, backoff))

  private object Registry : RandomGenerator.Registry<PetNode>() {
    init {
      val specSizes = multiset(8 to 0, 4 to 1, 2 to 2, 1 to 3) // weight to value
      register {
        cn(randomName())
      }
      register { ClassLiteral(recurse()) }
      register {
        GenericTypeExpression(recurse(), listOfSize(choose(specSizes)), refinement())
      }
      register {
        choose(5 to recurse<GenericTypeExpression>(), 0 to recurse<ClassLiteral>()) // TODO
      }
      register {
        sat(choose(0, 1, 1, 1, 5, 11), choose(1 to MEGACREDIT.type, 3 to recurse()))
      }

      val requirementTypes = multiset(
          9 to Min::class,
          4 to Max::class,
          2 to Exact::class,
          5 to Requirement.Or::class,
          3 to Requirement.And::class,
          1 to Requirement.Transform::class,
      )
      register<Requirement> { recurse(choose(requirementTypes)) }
      register { Min(sat = recurse()) }
      register { Max(sat = recurse()) }
      register { Exact(sat = recurse()) }
      register { Requirement.Or(setOfSize(choose(2, 2, 2, 2, 2, 3, 4))) }
      register { Requirement.And(listOfSize(choose(2, 2, 2, 2, 3))) }
      register { Requirement.Transform(recurse(), "PROD") }

      fun RandomGenerator<*>.intensity() = choose(3 to null, 1 to randomEnum<Intensity>())

      val instructionTypes = multiset(
          9 to Gain::class,
          4 to Remove::class,
          3 to Per::class,
          2 to Gated::class,
          2 to Transmute::class,
          1 to Custom::class,
          1 to Then::class,
          3 to Instruction.Or::class,
          5 to Instruction.Multi::class,
          1 to Instruction.Transform::class,
      )
      register<Instruction> { recurse(choose(instructionTypes)) }
      register { Gain(recurse(), intensity()) }
      register { Remove(recurse(), intensity()) }
      register { Per(recurse(), recurse()) }
      register { Gated(recurse(), recurse()) }
      register { Transmute(recurse(), recurse<ScalarAndType>().scalar, intensity()) }
      register { Custom("name", listOfSize(choose(1, 1, 1, 2))) }
      register { Then(listOfSize(choose(2, 2, 2, 3))) }
      register { Instruction.Or(setOfSize(choose(2, 2, 2, 2, 3))) }
      register { Instruction.Multi(listOfSize(choose(2, 2, 2, 2, 2, 3, 4))) }
      register { Instruction.Transform(recurse(), "PROD") }

      register<FromExpression> {
        val one: GenericTypeExpression = recurse()
        val two: GenericTypeExpression = recurse()

        fun getTypes(type: GenericTypeExpression): List<GenericTypeExpression> =
            type.args.flatMap { getTypes(it.asGeneric()) } + type

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

        fun convert(type: GenericTypeExpression): FromExpression {
          if (type == target) {
            return SimpleFrom(if (b) inject else target, if (b) target else inject)
          }
          val args = type.args.map { convert(it.asGeneric()) }
          return if (args.all { it is TypeInFrom }) {
            TypeInFrom(type)
          } else {
            ComplexFrom(type.root, args, type.refinement)
          }
        }
        convert(into)
      }

      val triggerTypes = multiset(
          9 to Trigger.OnGain::class,
          5 to Trigger.OnRemove::class,
          1 to Trigger.Transform::class,
      )
      register<Trigger> { recurse(choose(triggerTypes)) }
      register { Trigger.OnGain(recurse()) }
      register { Trigger.OnRemove(recurse()) }
      register { Trigger.Transform(recurse(), "PROD") }

      register { Effect(recurse(), recurse(), choose(true, false)) }

      val costTypes = multiset(
          9 to Cost.Spend::class,
          3 to Cost.Per::class,
          3 to Cost.Or::class,
          2 to Cost.Multi::class,
          2 to Cost.Transform::class,
      )
      register<Cost> { recurse(choose(costTypes)) }
      register { Cost.Spend(sat = recurse()) }
      register { Cost.Per(recurse(), recurse()) }
      register { Cost.Or(setOfSize(choose(2, 2, 2, 2, 3, 4))) }
      register { Cost.Multi(listOfSize(choose(2, 2, 2, 3))) }
      register { Cost.Transform(recurse(), "PROD") }

      register { Action(choose(1 to null, 3 to recurse()), recurse()) }
    }

    override fun <T : PetNode> invoke(type: KClass<T>, gen: RandomGenerator<PetNode>): T? {
      return try {
        super.invoke(type, gen)
      } catch (e: PetException) {
        null // TODO this better
      }
    }

    fun RandomGenerator<PetNode>.refinement() =
        chooseS(9 to { null }, 1 to { recurse<Requirement>() })

    fun RandomGenerator<PetNode>.randomName() =
        choose("Foo", "Bar", "Qux", "Abc", "Xyz", "Ooh", "Ahh", "Eep", "Wau")
  }

  inline fun <reified T : PetNode> goNuts(count: Int = 10_000) {
    return goNuts(T::class, count)
  }

  fun <T : PetNode> goNuts(type: KClass<T>, count: Int = 10_000) {
    for (i in 1..count) {
      val randomNode = makeRandomNode(type)

      val originalStringOut = randomNode.toString()

      val reparsedNode = try {
        parsePets(type, originalStringOut)
      } catch (e: Exception) {
        fail("node was ${p2k(randomNode)}", e)
      }

      assertWithMessage("intermediate string form was $originalStringOut")
          .that(reparsedNode)
          .isEqualTo(randomNode)

      val regurgitated = reparsedNode.toString()
      assertWithMessage("intermediate parsed form was:\n${p2k(reparsedNode)}")
          .that(regurgitated)
          .isEqualTo(originalStringOut)
    }
  }

  inline fun <reified T : PetNode> findAverageTextLength(): Int {
    val samples = 1000
    val sum = (1..samples).sumOf { makeRandomNode<T>().toString().length }
    return (sum.toDouble() / samples).roundToInt()
  }

  inline fun <reified T : PetNode> printTestStrings(count: Int) {
    for (i in 1..count) {
      println(makeRandomNode<T>())
    }
  }

  inline fun <reified T : PetNode> printTestStringOfEachLength(maxLength: Int) {
    getTestStringOfEachLength<T>(maxLength).forEach(::println)
  }

  inline fun <reified T : PetNode> getTestStringOfEachLength(maxLength: Int): List<String> {
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

  inline fun <reified T : PetNode> generateTestApiConstructions(count: Int = 10) {
    for (i in 1..count) {
      val node = makeRandomNode<T>()
      println("assertThat(${p2k(node)}.toString()).isEqualTo($node)")
    }
  }

  inline fun <reified T : PetNode> uniqueNodes(
      count: Int = 100, depthLimit: Int = 10, stopAtDrySpell: Int = 200,
  ): Set<T> {
    val set = mutableSetOf<T>()
    var drySpell = 0
    while (set.size < count && drySpell < stopAtDrySpell) {
      val node = makeRandomNode<T>()
      if (node.nodeCount() <= depthLimit && set.add(node)) {
        drySpell = 0
      } else {
        drySpell++
      }
    }
    return set
  }
}

private fun <T : Any> multiset(vararg pairs: Pair<Int, T>): ImmutableMultiset<T> {
  val builder = ImmutableMultiset.builder<T>()
  pairs.forEach { (count, element) -> builder.addCopies(element, count) }
  return builder.build()
}

fun scaling(greed: Double, backoff: Double): (Int) -> Double {
  require(backoff >= 0)
  require(greed > -1.0)
  require(greed < 1.0)
  // must be in range -1..1
  return { (greed + 1) / (backoff + 1).pow(it) - 1 }
}
