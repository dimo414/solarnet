package dev.martianzoo.tfm.data

import dev.martianzoo.tfm.pets.ast.ClassName
import dev.martianzoo.tfm.pets.ast.ClassName.Companion.cn

object EnglishHack {
  // I don't want English to be privileged; this is just for my convenience for the time being
  fun englishHack(id: ClassName): ClassName {
    ENGLISH_HACK[id]?.let {
      return it
    }
    val idStr = id.toString()
    if (idStr.endsWith("F")) {
      return englishHack(cn(idStr.substring(0, idStr.length - 1)))
    }
    error(id)
  }

  private fun classNameMap(vararg pairs: Pair<String, String>) =
      pairs.associate { (a, b) -> cn(a) to cn(b) }

  @Suppress("SpellCheckingInspection")
  val ENGLISH_HACK =
      classNameMap(
          "SAA" to "PlayCardFromHand",
          "SAB" to "UseStandardProject",
          "SAC" to "ClaimMilestone",
          "SAD" to "FundAward",
          "SAE" to "UseActionFromCard",
          "SAF" to "ConvertPlants",
          "SAG" to "ConvertHeat",
          "SELL" to "SellPatents",
          "SP11" to "PowerPlantSP",
          "SP14" to "AsteroidSP",
          "SP15" to "AirScrappingSP",
          "SP18" to "AquiferSP",
          "SP23" to "GreenerySP",
          "SP25" to "CitySP",
          "MM1" to "Terraformer",
          "MM2" to "Mayor",
          "MM3" to "Gardener",
          "MM4" to "Builder",
          "MM5" to "Planner",
          "HM1" to "Diversifier",
          "HM2" to "Tactician",
          "HM3" to "PolarExplorer",
          "HM4" to "Energizer",
          "HM5" to "RimSettler",
          "EM1" to "Generalist",
          "EM2" to "Specialist",
          "EM3" to "Ecologist",
          "EM4" to "Tycoon",
          "EM5" to "Legend",
          "EM1R" to "GeneralistR",
          "VM1" to "Hoverlord",
          "C001" to "ColonizerTrainingCamp",
          "C002" to "AsteroidMiningConsortium",
          "C003" to "DeepWellHeating",
          "C004" to "CloudSeeding",
          "C005" to "SearchForLife",
          "C006" to "InventorsGuild",
          "C007" to "MartianRails",
          "C008" to "Capital",
          "C009" to "AsteroidCard",
          "C010" to "Comet",
          "C011" to "BigAsteroid",
          "C012" to "WaterImportFromEuropa",
          "C013" to "SpaceElevator",
          "C014" to "DevelopmentCenter",
          "C015" to "EquatorialMagnetizer",
          "C016" to "DomedCrater",
          "C017" to "NoctisCity",
          "C018" to "MethaneFromTitan",
          "C019" to "ImportedHydrogen",
          "C020" to "ResearchOutpost",
          "C021" to "PhobosSpaceHaven",
          "C022" to "BlackPolarDust",
          "C023" to "ArcticAlgae",
          "C024" to "Predators",
          "C025" to "SpaceStation",
          "C026" to "EosChasmaNationalPark",
          "C027" to "InterstellarColonyShip",
          "C028" to "SecurityFleet",
          "C029" to "CupolaCity",
          "C030" to "LunarBeam",
          "C031" to "OptimalAerobraking",
          "C032" to "UndergroundCity",
          "C033" to "RegolithEaters",
          "C034" to "GhgProducingBacteria",
          "C035" to "Ants",
          "C036" to "ReleaseOfInertGases",
          "C037" to "NitrogenRichAsteroid",
          "C038" to "RoverConstruction",
          "C039" to "DeimosDown",
          "C040" to "AsteroidMining",
          "C041" to "FoodFactory",
          "C042" to "Archaebacteria",
          "C043" to "CarbonateProcessing",
          "C044" to "NaturalPreserve",
          "C045" to "NuclearPower",
          "C046" to "LightningHarvest",
          "C047" to "Algae",
          "C048" to "AdaptedLichen",
          "C049" to "Tardigrades",
          "C050" to "Virus",
          "C051" to "MirandaResort",
          "C052" to "Fish",
          "C053" to "LakeMarineris",
          "C054" to "SmallAnimals",
          "C055" to "KelpFarming",
          "C056" to "Mine",
          "C057" to "VestaShipyard",
          "C058" to "BeamFromAThoriumAsteroid",
          "C059" to "Mangrove",
          "C060" to "Trees",
          "C061" to "GreatEscarpmentConsortium",
          "C062" to "MineralDeposit",
          "C063" to "MiningExpedition",
          "C064" to "MiningArea",
          "C065" to "BuildingIndustries",
          "C066" to "LandClaim",
          "C067" to "MiningRights",
          "C068" to "Sponsors",
          "C069" to "ElectroCatapult",
          "C070" to "EarthCatapult",
          "C071" to "AdvancedAlloys",
          "C072" to "Birds",
          "C073" to "MarsUniversity",
          "C074" to "ViralEnhancers",
          "C075" to "TowingAComet",
          "C076" to "SpaceMirrors",
          "C077" to "SolarWindPower",
          "C078" to "IceAsteroid",
          "C079" to "QuantumExtractor",
          "C080" to "GiantIceAsteroid",
          "C081" to "GanymedeColony",
          "C082" to "CallistoPenalMines",
          "C083" to "GiantSpaceMirror",
          "C084" to "TransNeptuneProbe",
          "C085" to "CommercialDistrict",
          "C086" to "RoboticWorkforce",
          "C087" to "Grass",
          "C088" to "Heather",
          "C089" to "PeroxidePower",
          "C090" to "Research",
          "C091" to "GeneRepair",
          "C092" to "IoMiningIndustries",
          "C093" to "Bushes",
          "C094" to "MassConverter",
          "C095" to "PhysicsComplex",
          "C096" to "Greenhouses",
          "C097" to "NuclearZone",
          "C098" to "TropicalResort",
          "C099" to "TollStation",
          "C100" to "FueledGenerators",
          "C101" to "Ironworks",
          "C102" to "PowerGrid",
          "C103" to "Steelworks",
          "C104" to "OreProcessor",
          "C105" to "EarthOffice",
          "C106" to "AcquiredCompany",
          "C107" to "MediaArchives",
          "C108" to "OpenCity",
          "C109" to "MediaGroup",
          "C110" to "BusinessNetwork",
          "C111" to "BusinessContacts",
          "C112" to "BribedCommittee",
          "C113" to "SolarPower",
          "C114" to "BreathingFilters",
          "C115" to "ArtificialPhotosynthesis",
          "C116" to "ArtificialLake",
          "C117" to "GeothermalPower",
          "C118" to "Farming",
          "C119" to "DustSeals",
          "C120" to "UrbanizedArea",
          "C121" to "Sabotage",
          "C122" to "Moss",
          "C123" to "IndustrialCenter",
          "C124" to "HiredRaiders",
          "C125" to "Hackers",
          "C126" to "GhgFactories",
          "C127" to "SubterraneanReservoir",
          "C128" to "EcologicalZone",
          "C129" to "Zeppelins",
          "C130" to "Worms",
          "C131" to "Decomposers",
          "C132" to "FusionPower",
          "C133" to "SymbioticFungus",
          "C134" to "ExtremeColdFungus",
          "C135" to "AdvancedEcosystems",
          "C136" to "GreatDam",
          "C137" to "Cartel",
          "C138" to "StripMine",
          "C139" to "WavePower",
          "C140" to "LavaFlows",
          "C141" to "PowerPlantCard",
          "C142" to "MoholeArea",
          "C143" to "LargeConvoy",
          "C144" to "TitaniumMine",
          "C145" to "TectonicStressPower",
          "C146" to "NitrophilicMoss",
          "C147" to "Herbivores",
          "C148" to "Insects",
          "C149" to "CeosFavoriteProject",
          "C150" to "AntiGravityTechnology",
          "C151" to "InvestmentLoan",
          "C152" to "Insulation",
          "C153" to "AdaptationTechnology",
          "C154" to "CaretakerContract",
          "C155" to "DesignedMicroorganisms",
          "C156" to "StandardTechnology",
          "C157" to "NitriteReducingBacteria",
          "C158" to "IndustrialMicrobes",
          "C159" to "Lichen",
          "C160" to "PowerSupplyConsortium",
          "C161" to "ConvoyFromEuropa",
          "C162" to "ImportedGhg",
          "C163" to "ImportedNitrogen",
          "C164" to "MicroMills",
          "C165" to "MagneticFieldGenerators",
          "C166" to "Shuttles",
          "C167" to "ImportOfAdvancedGhg",
          "C168" to "Windmills",
          "C169" to "TundraFarming",
          "C170" to "AerobrakedAmmoniaAsteroid",
          "C171" to "MagneticFieldDome",
          "C172" to "Pets",
          "C173" to "ProtectedHabitats",
          "C174" to "ProtectedValley",
          "C175" to "Satellites",
          "C176" to "NoctisFarming",
          "C177" to "WaterSplittingPlant",
          "C178" to "HeatTrappers",
          "C179" to "SoilFactory",
          "C180" to "FuelFactory",
          "C181" to "IceCapMelting",
          "C182" to "CorporateStronghold",
          "C183" to "BiomassCombustors",
          "C184" to "Livestock",
          "C185" to "OlympusConference",
          "C186" to "RadSuits",
          "C187" to "AquiferPumping",
          "C188" to "Flooding",
          "C189" to "EnergySaving",
          "C190" to "LocalHeatTrapping",
          "C191" to "PermafrostExtraction",
          "C192" to "InventionContest",
          "C193" to "Plantation",
          "C194" to "PowerInfrastructure",
          "C195" to "IndenturedWorkers",
          "C196" to "LagrangeObservatory",
          "C197" to "TerraformingGanymede",
          "C198" to "ImmigrationShuttles",
          "C199" to "RestrictedArea",
          "C200" to "ImmigrantCity",
          "C201" to "EnergyTapping",
          "C202" to "UndergroundDetonations",
          "C203" to "Soletta",
          "C204" to "TechnologyDemonstration",
          "C205" to "RadChemFactory",
          "C206" to "SpecialDesign",
          "C207" to "MedicalLab",
          "C208" to "AiCentral",
          "C209" to "SmallAsteroid",
          "C210" to "SelfReplicatingRobots",
          "C211" to "SnowAlgae",
          "C212" to "Penguins",
          "C213" to "AerialMappers",
          "C214" to "AerosportTournament",
          "C215" to "AirScrappingExpedition",
          "C216" to "AtalantaPlanitiaLab",
          "C217" to "Atmoscoop",
          "C218" to "CometForVenus",
          "C219" to "CorroderSuits",
          "C220" to "DawnCity",
          "C221" to "DeuteriumExport",
          "C222" to "Dirigibles",
          "C223" to "ExtractorBalloons",
          "C224" to "Extremophiles",
          "C225" to "FloatingHabs",
          "C226" to "ForcedPrecipitation",
          "C227" to "FreyjaBiodomes",
          "C228" to "GhgImportFromVenus",
          "C229" to "GiantSolarShade",
          "C230" to "Gyropolis",
          "C231" to "HydrogenToVenus",
          "C232" to "IoSulphurResearch",
          "C233" to "IshtarMining",
          "C234" to "JetStreamMicroscrappers",
          "C235" to "LocalShading",
          "C236" to "LunaMetropolis",
          "C237" to "LuxuryFoods",
          "C238" to "MaxwellBase",
          "C239" to "MiningQuota",
          "C240" to "NeutralizerFactory",
          "C241" to "Omnicourt",
          "C242" to "OrbitalReflectors",
          "C243" to "RotatorImpacts",
          "C244" to "SisterPlanetSupport",
          "C245" to "Solarnet",
          "C246" to "SpinInducingAsteroid",
          "C247" to "SponsoredAcademies",
          "C248" to "Stratopolis",
          "C249" to "StratosphericBirds",
          "C250" to "SulphurExports",
          "C251" to "SulphurEatingBacteria",
          "C252" to "TerraformingContract",
          "C253" to "Thermophiles",
          "C254" to "WaterToVenus",
          "C255" to "VenusGovernor",
          "C256" to "VenusMagnetizer",
          "C257" to "VenusSoils",
          "C258" to "VenusWaystation",
          "C259" to "VenusianAnimals",
          "C260" to "VenusianInsects",
          "C261" to "VenusianPlants",
          "CB00A" to "BeginnerCorporationA",
          "CB00B" to "BeginnerCorporationB",
          "CB00C" to "BeginnerCorporationC",
          "CB00D" to "BeginnerCorporationD",
          "CB00E" to "BeginnerCorporationE",
          "CB01" to "Credicor",
          "CB02" to "Ecoline",
          "CB03" to "Helion",
          "CB04" to "InterplanetaryCinematics",
          "CB05" to "Inventrix",
          "CB06" to "MiningGuild",
          "CB07" to "Phobolog",
          "CB08" to "TharsisRepublic",
          "CB09" to "Thorgate",
          "CB10" to "UnitedNationsMarsInitiative",
          "CB11" to "SaturnSystems",
          "CB12" to "Teractor",
          "CC01" to "Airliners",
          "CC02" to "AirRaid",
          "CC03" to "AtmoCollectors",
          "CC04" to "CommunityServices",
          "CC05" to "Conscription",
          "CC06" to "CoronaExtractor",
          "CC07" to "CryoSleep",
          "CC08" to "EarthElevator",
          "CC09" to "EcologyResearch",
          "CC10" to "FloaterLeasing",
          "CC11" to "FloaterPrototypes",
          "CC12" to "FloaterTechnology",
          "CC13" to "GalileanWaystation",
          "CC14" to "HeavyTaxation",
          "CC15" to "IceMoonColony",
          "CC16" to "ImpactorSwarm",
          "CC17" to "InterplanetaryColonyShip",
          "CC18" to "JovianLanterns",
          "CC19" to "JupiterFloatingStation",
          "CC20" to "LunaGovernor",
          "CC21" to "LunarExports",
          "CC22" to "LunarMining",
          "CC23" to "MarketManipulation",
          "CC24" to "MartianZoo",
          "CC25" to "MiningColony",
          "CC26" to "MinorityRefuge",
          "CC27" to "MolecularPrinting",
          "CC28" to "NitrogenFromTitan",
          "CC29" to "PioneerSettlement",
          "CC30" to "ProductiveOutpost",
          "CC31" to "QuantumCommunications",
          "CC32" to "RedSpotObservatory",
          "CC33" to "RefugeeCamps",
          "CC34" to "ResearchColony",
          "CC35" to "RimFreighters",
          "CC36" to "SkyDocks",
          "CC37" to "SolarProbe",
          "CC38" to "SolarReflectors",
          "CC39" to "SpacePort",
          "CC40" to "SpacePortColony",
          "CC41" to "SpinOffDepartment",
          "CC42" to "SubZeroSaltFish",
          "CC43" to "TitanAirScrapping",
          "CC44" to "TitanFloatingLaunchPad",
          "CC45" to "TitanShuttles",
          "CC46" to "TradeEnvoys",
          "CC47" to "TradingColony",
          "CC48" to "UrbanDecomposers",
          "CC49" to "WarpDrive",
          "CCC1" to "Aridor",
          "CCC2" to "Arklight",
          "CCC3" to "Polyphemos",
          "CCC4" to "Poseidon",
          "CCC5" to "StormcraftIncorporated",
          "CP01" to "AlliedBank",
          "CP02" to "AquiferTurbines",
          "CP03" to "Biofuels",
          "CP04" to "Biolab",
          "CP05" to "BiosphereSupport",
          "CP06" to "BusinessEmpire",
          "CP07" to "DomeFarming",
          "CP08" to "Donation",
          "CP09" to "EarlySettlement",
          "CP10" to "EcologyExperts",
          "CP11" to "ExcentricSponsor",
          "CP12" to "ExperimentalForest",
          "CP13" to "GalileanMining",
          "CP14" to "GreatAquifer",
          "CP15" to "HugeAsteroid",
          "CP16" to "IoResearchOutpost",
          "CP17" to "Loan",
          "CP18" to "MartianIndustries",
          "CP19" to "MetalRichAsteroid",
          "CP20" to "MetalsCompany",
          "CP21" to "MiningOperations",
          "CP22" to "Mohole",
          "CP23" to "MoholeExcavation",
          "CP24" to "NitrogenShipment",
          "CP25" to "OrbitalConstructionYard",
          "CP26" to "PolarIndustries",
          "CP27" to "PowerGeneration",
          "CP28" to "ResearchNetwork",
          "CP29" to "SelfSufficientSettlement",
          "CP30" to "SmeltingPlant",
          "CP31" to "SocietySupport",
          "CP32" to "Supplier",
          "CP33" to "SupplyDrop",
          "CP34" to "UnmiContractor",
          "CP35" to "AcquiredSpaceAgency",
          "CP36" to "HousePrinting",
          "CP37" to "LavaTubeSettlement",
          "CP38" to "MartianSurvey",
          "CP39" to "Psychrophiles",
          "CP40" to "ResearchCoordination",
          "CP41" to "SfMemorial",
          "CP42" to "SpaceHotels",
          "CPC1" to "CheungShingMars",
          "CPC2" to "PointLuna",
          "CPC3" to "RobinsonIndustries",
          "CPC4" to "ValleyTrust",
          "CPC5" to "Vitor",
          "CT01" to "AerialLenses",
          "CT02" to "BannedDelegate",
          "CT03" to "CulturalMetropolis",
          "CT04" to "DiasporaMovement",
          "CT05" to "EventAnalysts",
          "CT06" to "GmoContract",
          "CT07" to "MartianMediaCenter",
          "CT08" to "ParliamentHall",
          "CT09" to "PrOffice",
          "CT10" to "PublicCelebrations",
          "CT11" to "Recruitment",
          "CT12" to "RedTourismWave",
          "CT13" to "SponsoredMohole",
          "CT14" to "SupportedResearch",
          "CT15" to "WildlifeDome",
          "CT16" to "VoteOfNoConfidence",
          "CTC1" to "LakefrontResorts",
          "CTC2" to "Pristar",
          "CTC3" to "SeptemTribus",
          "CTC4" to "TerralabsResearch",
          "CTC5" to "UtopiaInvest",
          "CV01" to "Aphrodite",
          "CV02" to "Celestic",
          "CV03" to "Manutech",
          "CV04" to "MorningStarInc",
          "CV05" to "Viron",
          "CX01" to "DuskLaserMining",
          "CX02" to "ProjectInspection",
          "CX03" to "EnergyMarket",
          "CX04" to "HiTechLab",
          "CX05" to "InterplanetaryTrade",
          "CX06" to "LawSuit",
          "CX07" to "MercurianAlloys",
          "CX08" to "OrbitalCleanup",
          "CX09" to "PoliticalAlliance",
          "CX10" to "RegoPlastics",
          "CX11" to "SaturnSurfing",
          "CX12" to "StanfordTorus",
          "CX13" to "Advertising",
          "CX14" to "AsteroidDeflectionSystem",
          "CX15" to "AsteroidHollowing",
          "CX16" to "CometAiming",
          "CX17" to "CrashSiteCleanup",
          "CX18" to "CuttingEdgeTechnology",
          "CX19" to "DirectedImpactors",
          "CX20" to "DiversitySupport",
          "CX21" to "FieldCappedCity",
          "CX22" to "ImportedNutrients",
          "CX23" to "JovianEmbassy",
          "CX24" to "MagneticShield",
          "CX25" to "MeatIndustry",
          "CX26" to "Meltworks",
          "CX27" to "MoholeLake",
          "CX28" to "Potatoes",
          "CX29" to "SubCrustMeasurements",
          "CX30" to "TopsoilContract",
          "CX31" to "DeimosDownPromo", // TODO
          "CX32" to "GreatDamPromo",
          "CX33" to "MagneticFieldGeneratorsPromo",
          "CX34" to "AsteroidRights",
          "CX35" to "BactoviralResearch",
          "CX36" to "BioPrintingFacility",
          "CX37" to "Harvest",
          "CX38" to "OutdoorSports",
          "CX39" to "CorporateArchives",
          "CX40" to "DoubleDown",
          "CX41" to "Merger",
          "CX42" to "NewPartner",
          "CX43" to "HeadStart",
          "CXC1" to "ArcadianCommunities",
          "CXC2" to "Recyclon",
          "CXC3" to "SpliceTacticalGenomics",
          "CXC4" to "Factorum",
          "CXC5" to "MonsInsurance",
          "CXC6" to "Philares",
          "CXC7" to "Astrodrill",
          "CXC8" to "PharmacyUnion",
      )

  fun shortName(a: ClassName) = SHORT_NAMES[a] ?: a

  // TODO this is so stupid, I just can't figure out to put these into the grammar without hosing
  // everything
  val SHORT_NAMES =
      classNameMap(
          "Adjacency" to "ADJ",
          "Animal" to "ANI",
          "AnimalTag" to "ANT",
          "Anyone" to "ANY",
          "Asteroid" to "AST",
          "BuildingTag" to "BUT",
          "CardFront" to "CARD",
          "CardResource" to "CR",
          "CityTag" to "CIT",
          "CityTile" to "CT",
          "EarthTag" to "EAT",
          "Energy" to "E",
          "Floater" to "FLO",
          "Generation" to "GEN",
          "GlobalParameter" to "GP",
          "GreeneryTile" to "GT",
          "Heat" to "H",
          "JovianTag" to "JOT",
          "LandArea" to "LA",
          "MarsArea" to "MA",
          "Megacredit" to "M",
          "Microbe" to "MIC",
          "MicrobeTag" to "MIT",
          "Milestone" to "MIL",
          "Neighbor" to "NBR",
          "NoctisArea" to "NA",
          "OceanTile" to "OT",
          "OxygenStep" to "O2",
          "Plant" to "P",
          "PlantTag" to "PLT",
          "PlayedEvent" to "EVT",
          "Player1" to "P1",
          "Player2" to "P2",
          "Player3" to "P3",
          "Player4" to "P4",
          "Player5" to "P5",
          "PowerTag" to "POT",
          "ProjectCard" to "PC",
          "RemoteArea" to "RA",
          "Resource" to "RES",
          "ResourcefulCard" to "RC",
          "Science" to "SCI",
          "ScienceTag" to "SCT",
          "SpaceTag" to "SPT",
          "SpecialTile" to "ST",
          "StandardResource" to "SR",
          "Steel" to "S",
          "TemperatureStep" to "TEMP",
          "TerraformRating" to "TR",
          "Titanium" to "T",
          "UseAction" to "UA",
          "UseAction1" to "UA1",
          "UseAction2" to "UA2",
          "VenusTag" to "VET",
          "VictoryPoint" to "VP",
          "VolcanicArea" to "VA",
          "WaterArea" to "WA",
      )
}
