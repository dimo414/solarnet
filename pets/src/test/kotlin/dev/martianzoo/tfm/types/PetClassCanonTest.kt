package dev.martianzoo.tfm.types

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.pets.SpecialComponent.CLASS
import dev.martianzoo.tfm.pets.SpecialComponent.COMPONENT
import dev.martianzoo.tfm.pets.testRoundTrip
import org.junit.jupiter.api.Test
import java.util.*

class PetClassCanonTest {

  fun component() { // TODO make this pass by not forcing subclasses to get loaded early
    val table = PetClassLoader(Canon.allDefinitions)

    table.load("$COMPONENT").apply {
      assertThat(name).isEqualTo("Component")
      assertThat(abstract).isTrue()
      assertThat(directDependencyKeys).isEmpty()
      assertThat(allDependencyKeys).isEmpty()
      assertThat(directSuperclasses).isEmpty()
    }

    table.load("$CLASS").apply {
      assertThat(name).isEqualTo("Class")
      assertThat(abstract).isTrue()
      assertThat(directDependencyKeys).containsExactly(DependencyKey(this, 0, true))
      assertThat(allDependencyKeys).containsExactly(DependencyKey(this, 0, true))
      assertThat(directSuperclasses).containsExactly(table["$COMPONENT"])
    }
  }

  @Test
  fun slurp() {
    val defns = Canon.allDefinitions
    Truth.assertThat(defns.size).isGreaterThan(700)

    val table = PetClassLoader(defns).loadAll()

    table.all().forEach {
      it.directEffectsRaw.forEach(::testRoundTrip)
    }
    table.all().forEach {
      it.directEffects.forEach(::testRoundTrip)
    }
  }

  @Test fun subConcrete() {
    val table = PetClassLoader(Canon.allDefinitions).loadAll()
    val subConcrete = table.all().flatMap { clazz ->
      clazz.directSuperclasses.filterNot { it.abstract }.map { clazz.name to it.name }
    }

    // currently just 3 cases of subclassing a concrete class in the canon
    Truth.assertThat(subConcrete).containsExactly(
        "Tile008" to "CityTile",
        "Psychrophile" to "Microbe",
        "Dirigible" to "Floater")
  }

  fun findValidTypes() {
    val table = PetClassLoader(Canon.allDefinitions).loadAll()
    val names: List<String> = table.all().map { it.name }.filterNot {
      it.matches(Regex("^Card.{3,4}$")) && it.hashCode() % 12 != 0
    }.filterNot {
      it.matches(Regex("^(Tharsis|Hellas|Elysium)")) && it.hashCode() % 8 != 0
    }.filterNot {
      it in setOf("Component", "Die", "Class")
    }

    val abstracts = TreeSet<String>()
    val concretes = TreeSet<String>()
    val invalids = TreeSet<String>()

    while (abstracts.size < 100 || concretes.size < 100 || invalids.size < 100) {
      val name1 = names.random()
      val name2 = names.random()
      val name3 = names.random()
      val name4 = names.random()
      val tryThese = setOf(
          "$name1<$name2>",
          "$name1<$name2, $name3>",
          "$name1<$name2<$name3>>",
          "$name1<$name2, $name3, $name4>",
          "$name1<$name2<$name3>, $name4>",
          "$name1<$name2, $name3<$name4>>",
          "$name1<$name2<$name3<$name4>>>",
          "$name1<Player1>",
          "$name1<Player1, $name3>",
          "$name1<Player1, $name3, $name4>",
          "$name1<Player1, $name3<$name4>>",
          "$name1<$name2, Player1>",
          "$name1<$name2<Player1>>",
          "$name1<$name2, Player1, $name4>",
          "$name1<$name2<Player1>, $name4>",
          "$name1<$name2, $name3, Player1>",
          "$name1<$name2<$name3>, Player1>",
          "$name1<$name2, $name3<Player1>>",
          "$name1<$name2<$name3<Player1>>>",
      )
      for (thing in tryThese) {
        if (table.isValid(thing)) {
          val type = table.resolve(thing)
          if (type.abstract) {
            if (abstracts.size < 100) abstracts.add(thing)
          } else {
            if (concretes.size < 100) concretes.add(thing)
          }
        } else {
          if (invalids.size < 100) invalids.add(thing)
        }
      }
    }
    println("ABSTRACTS")
    abstracts.forEach(::println)
    println()
    println("CONCRETES")
    concretes.forEach(::println)
    println()
    println("INVALIDS")
    invalids.forEach(::println)
  }

  fun describeEverything() {
    val table = PetClassLoader(Canon.allDefinitions).loadAll()
    table.all().sortedBy { it.name }.forEach { c ->
      println("${c.baseType} : ${c.allSuperclasses.filter { it.name !in setOf("$COMPONENT", c.name) }}")
    }
  }
}
