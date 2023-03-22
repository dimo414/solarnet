package dev.martianzoo.tfm.repl

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import dev.martianzoo.tfm.api.GameSetup
import dev.martianzoo.tfm.api.ResourceUtils.lookUpProductionLevels
import dev.martianzoo.tfm.canon.Canon
import dev.martianzoo.tfm.data.Actor.Companion.PLAYER1
import dev.martianzoo.tfm.data.Actor.Companion.PLAYER2
import dev.martianzoo.tfm.data.Task.TaskId
import dev.martianzoo.tfm.engine.Engine
import dev.martianzoo.tfm.pets.Parsing.parseInput
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn
import org.junit.jupiter.api.Test

class EntireGameTest {
  @Test
  fun fourWholeGenerations() {
    val game = Engine.newGame(GameSetup(Canon, "BREPT", 2))
    val p1 = InteractiveSession(game, PLAYER1)
    val p2 = InteractiveSession(game, PLAYER2)
    val engine = InteractiveSession(game)

    fun InteractiveSession.exec(s: String) = initiateAndAutoExec(parseInput(s))
    fun InteractiveSession.task(id: String, s: String? = null) =
        doTaskAndAutoExec(TaskId(id), s?.let { parseInput(it) })

    fun InteractiveSession.dropTask(id: String) = agent.removeTask(TaskId(id))

    p1.exec("CorporationCard, LakefrontResorts, 3 BuyCard")
    p2.exec("CorporationCard, InterplanetaryCinematics, 8 BuyCard")

    p1.exec("2 PreludeCard, MartianIndustries, GalileanMining")
    p2.exec("2 PreludeCard, MiningOperations, UnmiContractor")

    engine.exec("ActionPhase")

    p1.exec("-30 THEN AsteroidMining")

    with(p2) {
      exec("-4 Steel THEN -1 THEN NaturalPreserve")
      dropTask("B")
      exec("Tile044<E37>")
      exec("-13 Steel THEN -1 THEN SpaceElevator")
      exec("UseAction1<SpaceElevator>")
      exec("-2 THEN InventionContest")
      exec("-6 THEN GreatEscarpmentConsortium")
      task("A", "PROD[-Steel<P1>]")
    }

    engine.exec("ProductionPhase")
    p1.task("A", "4 BuyCard")
    p2.task("B", "1 BuyCard")
    engine.exec("ActionPhase")

    with(p2) {
      exec("UseAction1<SpaceElevator>")
      exec("-23 THEN EarthCatapult")
    }

    with(p1) {
      exec("-7 THEN TitaniumMine")
      exec("-9 THEN RoboticWorkforce")
      dropTask("A") // TODO reify?
      exec("@copyProductionBox(MartianIndustries)")
      exec("-6 THEN Sponsors")
    }

    with(p2) {
      exec("-5 Steel THEN IndustrialMicrobes")
      exec("-Titanium THEN TechnologyDemonstration")
      exec("-1 THEN EnergyTapping")
      task("A", "PROD[-Energy<P1>]")
      exec("-2 Steel THEN BuildingIndustries")
    }

    engine.exec("ProductionPhase")
    p1.task("A", "3 BuyCard")
    p2.task("B", "2 BuyCard")
    engine.exec("ActionPhase")

    p1.exec("-2 THEN -1 Steel THEN Mine")

    with(p2) {
      exec("UseAction1<SpaceElevator>")
      exec("-5 THEN -5 Steel THEN ElectroCatapult")
      exec("UseAction1<ElectroCatapult>")
      task("A", "-Steel THEN 7") // TODO just one
      exec("-Titanium THEN -7 THEN SpaceHotels")
      exec("-6 THEN MarsUniversity")
      exec("-10 THEN ArtificialPhotosynthesis")
      task("B", "PROD[2 Energy]")
      exec("-5 THEN BribedCommittee")
    }

    engine.exec("ProductionPhase")
    p1.task("A", "3 BuyCard")
    p2.task("B", "2 BuyCard")
    engine.exec("ActionPhase")

    with(p2) {
      exec("UseAction1<ElectroCatapult>")
      dropTask("A")
      exec("-Steel THEN 7")
      exec("UseAction1<SpaceElevator>")
    }

    with(p1) {
      exec("-2 Steel THEN -14 THEN ResearchOutpost")
      dropTask("A")
      exec("CityTile<E56>") // TODO reif refi
      exec("-13 Titanium THEN -1 THEN IoMiningIndustries")
    }

    with(p2) {
      exec("-Titanium THEN -1 THEN TransNeptuneProbe")
      exec("-1 THEN Hackers")
      task("B", "PROD[-2 Megacredit<P1>]")
    }

    with(p1) {
      exec("UseAction1<SellPatents>")
      task("A", "Megacredit FROM ProjectCard")
    }

    with(p2) {
      exec("-4 Steel THEN -1 THEN SolarPower")
      exec("UseAction1<UseStandardProject>")
      task("A", "UseAction1<CitySP>")
      dropTask("B") // split
      exec("-25 THEN CityTile<E65> THEN PROD[1]")
      exec("PROD[-Plant, Energy]") // CORRECTION TODO WHY
    }

    engine.exec("ProductionPhase")

    val repl = ReplSession(Canon, GameSetup(Canon, "BM", 2))
    repl.session = InteractiveSession(game)

    // Stuff
    assertThat(repl.counts("Generation")).containsExactly(5)
    assertThat(repl.counts("OceanTile, OxygenStep, TemperatureStep")).containsExactly(0, 0, 0)

    // P1

    repl.command("become P1")

    repl.assertCount("TerraformRating", 20)

    val prods = lookUpProductionLevels(repl.session.game.reader, cn("P1").expr)
    assertThat(prods.values).containsExactly(2, 2, 7, 0, 1, 0).inOrder()

    assertThat(repl.counts("M, Steel, Titanium, Plant, Energy, Heat"))
        .containsExactly(34, 2, 8, 3, 1, 3)
        .inOrder()

    assertThat(repl.counts("ProjectCard, CardFront, ActiveCard, AutomatedCard, PlayedEvent"))
        .containsExactly(5, 10, 1, 6, 0)

    // tag abbreviations
    assertThat(repl.counts("BUT, SPT, SCT, POT, EAT, JOT, PLT, MIT, ANT, CIT"))
        .containsExactly(5, 2, 2, 0, 1, 3, 0, 0, 0, 1)
        .inOrder()

    assertThat(repl.counts("CityTile, GreeneryTile, SpecialTile"))
        .containsExactly(1, 0, 0)
        .inOrder()

    // P2

    repl.command("become P2")

    repl.assertCount("TerraformRating", 25)

    val prods2 = lookUpProductionLevels(repl.session.game.reader, cn("P2").expr)
    assertThat(prods2.values).containsExactly(8, 6, 1, 0, 2, 0).inOrder()

    assertThat(repl.counts("M, Steel, Titanium, Plant, Energy, Heat"))
        .containsExactly(47, 6, 1, 1, 2, 3)
        .inOrder()

    assertThat(repl.counts("ProjectCard, CardFront, ActiveCard, AutomatedCard, PlayedEvent"))
        .containsExactly(3, 17, 4, 10, 3)

    // tag abbreviations
    assertThat(repl.counts("BUT, SPT, SCT, POT, EAT, JOT, PLT, MIT, ANT, CIT"))
        .containsExactly(9, 3, 4, 2, 3, 0, 0, 1, 0, 0)
        .inOrder()

    assertThat(repl.counts("CityTile, GreeneryTile, SpecialTile"))
        .containsExactly(1, 0, 1)
        .inOrder()
  }

  @Test
  fun startEllieGameWithoutPrelude() {
    val repl = ReplSession(Canon, GameSetup(Canon, "BRHX", 2))
    repl.test("mode blue")

    repl.test("become P1")
    repl.test("exec Turn", "task B InterplanetaryCinematics", "task C 7 BuyCard")

    repl.test("become P2")
    repl.test("exec Turn", "task B PharmacyUnion", "task C 5 BuyCard")

    repl.test("become Engine")
    repl.test("exec ActionPhase")

    repl.test("become P1")
    repl.playCard(6, "MediaGroup")
  }

  @Test
  fun ellieGame() {
    val repl = ReplSession(Canon, GameSetup(Canon, "BRHXP", 2))

    repl.test("mode blue")

    repl.test("become P1")
    repl.test("exec Turn", "task B InterplanetaryCinematics", "task C 7 BuyCard")

    repl.test("become P2")
    repl.test("exec Turn", "task B PharmacyUnion", "task C 5 BuyCard")

    repl.test("become Engine")
    repl.test("exec PreludePhase")

    repl.test("become P1")
    repl.test("exec Turn", "task B UnmiContractor")
    repl.test("exec Turn", "task B CorporateArchives")

    repl.test("become P2")
    repl.test("exec Turn", "task B BiosphereSupport")
    repl.test("exec Turn", "task B SocietySupport")

    repl.test("become Engine")
    repl.test("exec ActionPhase")

    repl.test("become P1")

    repl.playCard(6, "MediaGroup")

    repl.playCard(1, "Sabotage", "task E -7 Megacredit<P2>")

    repl.test("become P2")
    repl.playCard(11, "Research")
    repl.playCard(9, "MartianSurvey", "task D Ok")

    repl.playCard(
        3,
        "SearchForLife",
        "task D PlayedEvent<Class<PharmacyUnion>> FROM PharmacyUnion THEN 3 TerraformRating")

    repl.useAction1("SearchForLife", "task C -1 THEN Ok")

    repl.test("become Engine")
    repl.test("exec ProductionPhase", "as P1 task A BuyCard", "as P2 task B 3 BuyCard")
    repl.test("exec ActionPhase")

    repl.test("become P2")

    repl.test("exec Turn", 1)
    repl.test("task A UseAction1<SellPatents>", 1)
    repl.test("task B Megacredit FROM ProjectCard")

    repl.test("exec Turn", 1)
    repl.test("task A UseAction1<PlayCardFromHand>", 1)
    repl.test("task B PlayCard<Class<VestaShipyard>>", 2)
    repl.test("task C 15 Pay<Class<M>> FROM M", 1)
    repl.test("task D Ok")

    repl.test("become P1")
    repl.playCard(23, "EarthCatapult")
    // TODO recognize one is impossible

    repl.test("exec Turn", 1)
    repl.test("task A UseAction1<PlayCardFromHand>", 1)
    repl.test("task B PlayCard<Class<OlympusConference>>", 2)
    repl.test("task C Ok", 1)
    repl.test("task D 4 Pay<Class<S>> FROM S", 1)
    repl.test("task E Science<OlympusConference>")

    repl.test("mode green")

    repl.test(
        "exec -4 Steel THEN -1 THEN DevelopmentCenter",
        "task A ProjectCard FROM Science<OlympusConference>")
    repl.test("exec -4 Steel THEN -1 THEN GeothermalPower")
    repl.test("exec -10 THEN MirandaResort")
    repl.test("exec -1 THEN Hackers", "task B PROD[-2 M<P2>]")
    repl.test("exec -1 THEN MicroMills")

    repl.test("become Engine")
    repl.test("exec ProductionPhase", "as P1 task A 3 BuyCard", "as P2 task B BuyCard")
    repl.test("exec ActionPhase")

    repl.test("become P1")
    repl.test(
        "exec UseAction1<UseActionFromCard>",
        "task A UseAction1<DevelopmentCenter> THEN ActionUsedMarker<DevelopmentCenter>",
    )
    repl.test("exec -5 Steel THEN -1 THEN ImmigrantCity", 1)
    // repl.testExec("task C CityTile<Hellas_9_7>")

    // Shared stuff

    assertThat(repl.counts("Generation")).containsExactly(3)
    assertThat(repl.counts("OceanTile, OxygenStep, TemperatureStep")).containsExactly(0, 0, 0)

    // P1

    repl.command("become P1")

    repl.assertCount("TerraformRating", 23)

    val prods = lookUpProductionLevels(repl.session.game.reader, cn("P1").expr)
    assertThat(prods.values).containsExactly(4, 0, 0, 0, 0, 1).inOrder()

    assertThat(repl.counts("M, Steel, Titanium, Plant, Energy, Heat"))
        .containsExactly(22, 3, 0, 0, 0, 1)
        .inOrder()

    assertThat(repl.counts("ProjectCard, CardFront, ActiveCard, AutomatedCard, PlayedEvent"))
        .containsExactly(6, 12, 5, 4, 1)

    // tag abbreviations
    assertThat(repl.counts("BUT, SPT, SCT, POT, EAT, JOT, PLT, MIT, ANT, CIT"))
        .containsExactly(5, 1, 3, 1, 4, 1, 0, 0, 0, 1)
        .inOrder()

    assertThat(repl.counts("CityTile, GreeneryTile, SpecialTile"))
        .containsExactly(0, 0, 0)
        .inOrder()

    // P2

    repl.command("become P2")

    repl.assertCount("TerraformRating", 25)

    val prods2 = lookUpProductionLevels(repl.session.game.reader, cn("P2").expr)
    assertThat(prods2.values).containsExactly(-4, 0, 1, 3, 1, 1).inOrder()

    assertThat(repl.counts("M, Steel, Titanium, Plant, Energy, Heat"))
        .containsExactly(18, 0, 1, 6, 1, 3)
        .inOrder()

    assertThat(repl.counts("ProjectCard, CardFront, ActiveCard, AutomatedCard, PlayedEvent"))
        .containsExactly(9, 5, 1, 2, 2)

    // tag abbreviations
    assertThat(repl.counts("BUT, SPT, SCT, POT, EAT, JOT, PLT, MIT, ANT, CIT"))
        .containsExactly(0, 1, 3, 0, 0, 1, 1, 0, 0, 0)
        .inOrder()

    assertThat(repl.counts("CityTile, GreeneryTile, SpecialTile"))
        .containsExactly(0, 0, 0)
        .inOrder()
  }
}

fun ReplSession.playCard(mega: Int, cardName: String, vararg tasks: String) {
  test("exec Turn", 1)
  test("task A UseAction1<PlayCardFromHand>", 1)
  test("task B PlayCard<Class<$cardName>>", 1)
  if (mega > 0) {
    test("task C $mega Pay<Class<M>> FROM M", tasks.size)
  } else {
    test("task C Ok", tasks.size)
  }
  var left = tasks.size
  for (task in tasks) test(task, --left)
}

fun ReplSession.useAction1(cardName: String, vararg tasks: String) {
  test("exec Turn", 1)
  test("task A UseAction1<UseActionFromCard>", 1)
  test("task B UseAction1<$cardName> THEN ActionUsedMarker<$cardName>", 1)
  var left = tasks.size
  for (task in tasks) test(task, --left)
}

fun ReplSession.test(s: String, tasksExpected: Int = 0) {
  val (cmd, args) = s.split(" ", limit = 2)
  commands[cmd]!!.withArgs(args)
  assertWithMessage("${session.game.taskQueue}")
      .that(session.game.taskQueue.size)
      .isEqualTo(tasksExpected)
}

fun ReplSession.test(vararg s: String) {
  var x = s.size
  for (it in s) test(it, --x)
}

fun ReplSession.assertCount(text: String, i: Int) {
  assertThat(session.count(parseInput(text))).isEqualTo(i)
}

fun ReplSession.counts(text: String): List<Int> =
    text.split(",").map { session.count(parseInput(it)) }
