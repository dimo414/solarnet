package dev.martianzoo.tfm.data

// Just for my own convenience during development
const val USE_ENGLISH_HACK = true

fun englishHack(id: String): String {
  println(id)
  if (!USE_ENGLISH_HACK) return "Card$id"
  if (id in ENGLISH_HACK) return ENGLISH_HACK[id]!!
  if (id.endsWith("F")) return ENGLISH_HACK[id.substring(0, id.length - 1)]!!
  return "Card$id"
}

val ENGLISH_HACK = mapOf(
    "001" to "ColonizerTrainingCamp",
    "002" to "AsteroidMiningConsortium",
    "003" to "DeepWellHeating",
    "004" to "CloudSeeding",
    "005" to "SearchForLife",
    "006" to "InventorsGuild",
    "007" to "MartianRails",
    "008" to "Capital",
    "009" to "AsteroidCard",
    "010" to "Comet",
    "011" to "BigAsteroid",
    "012" to "WaterImportFromEuropa",
    "013" to "SpaceElevator",
    "014" to "DevelopmentCenter",
    "015" to "EquatorialMagnetizer",
    "016" to "DomedCrater",
    "017" to "NoctisCity",
    "018" to "MethaneFromTitan",
    "019" to "ImportedHydrogen",
    "020" to "ResearchOutpost",
    "021" to "PhobosSpaceHaven",
    "022" to "BlackPolarDust",
    "023" to "ArcticAlgae",
    "024" to "Predators",
    "025" to "SpaceStation",
    "026" to "EosChasmaNationalPark",
    "027" to "InterstellarColonyShip",
    "028" to "SecurityFleet",
    "029" to "CupolaCity",
    "030" to "LunarBeam",
    "031" to "OptimalAerobraking",
    "032" to "UndergroundCity",
    "033" to "RegolithEaters",
    "034" to "GhgProducingBacteria",
    "035" to "Ants",
    "036" to "ReleaseOfInertGases",
    "037" to "NitrogenRichAsteroid",
    "038" to "RoverConstruction",
    "039" to "DeimosDown",
    "040" to "AsteroidMining",
    "041" to "FoodFactory",
    "042" to "Archaebacteria",
    "043" to "CarbonateProcessing",
    "044" to "NaturalPreserve",
    "045" to "NuclearPower",
    "046" to "LightningHarvest",
    "047" to "Algae",
    "048" to "AdaptedLichen",
    "049" to "Tardigrades",
    "050" to "Virus",
    "051" to "MirandaResort",
    "052" to "Fish",
    "053" to "LakeMarineris",
    "054" to "SmallAnimals",
    "055" to "KelpFarming",
    "056" to "Mine",
    "057" to "VestaShipyard",
    "058" to "BeamFromAThoriumAsteroid",
    "059" to "Mangrove",
    "060" to "Trees",
    "061" to "GreatEscarpmentConsortium",
    "062" to "MineralDeposit",
    "063" to "MiningExpedition",
    "064" to "MiningArea",
    "065" to "BuildingIndustries",
    "066" to "LandClaim",
    "067" to "MiningRights",
    "068" to "Sponsors",
    "069" to "ElectroCatapult",
    "070" to "EarthCatapult",
    "071" to "AdvancedAlloys",
    "072" to "Birds",
    "073" to "MarsUniversity",
    "074" to "ViralEnhancers",
    "075" to "TowingAComet",
    "076" to "SpaceMirrors",
    "077" to "SolarWindPower",
    "078" to "IceAsteroid",
    "079" to "QuantumExtractor",
    "080" to "GiantIceAsteroid",
    "081" to "GanymedeColony",
    "082" to "CallistoPenalMines",
    "083" to "GiantSpaceMirror",
    "084" to "TransNeptuneProbe",
    "085" to "CommercialDistrict",
    "086" to "RoboticWorkforce",
    "087" to "Grass",
    "088" to "Heather",
    "089" to "PeroxidePower",
    "090" to "Research",
    "091" to "GeneRepair",
    "092" to "IoMiningIndustries",
    "093" to "Bushes",
    "094" to "MassConverter",
    "095" to "PhysicsComplex",
    "096" to "Greenhouses",
    "097" to "NuclearZone",
    "098" to "TropicalResort",
    "099" to "TollStation",
    "100" to "FueledGenerators",
    "101" to "Ironworks",
    "102" to "PowerGrid",
    "103" to "Steelworks",
    "104" to "OreProcessor",
    "105" to "EarthOffice",
    "106" to "AcquiredCompany",
    "107" to "MediaArchives",
    "108" to "OpenCity",
    "109" to "MediaGroup",
    "110" to "BusinessNetwork",
    "111" to "BusinessContacts",
    "112" to "BribedCommittee",
    "113" to "SolarPower",
    "114" to "BreathingFilters",
    "115" to "ArtificialPhotosynthesis",
    "116" to "ArtificialLake",
    "117" to "GeothermalPower",
    "118" to "Farming",
    "119" to "DustSeals",
    "120" to "UrbanizedArea",
    "121" to "Sabotage",
    "122" to "Moss",
    "123" to "IndustrialCenter",
    "124" to "HiredRaiders",
    "125" to "Hackers",
    "126" to "GhgFactories",
    "127" to "SubterraneanReservoir",
    "128" to "EcologicalZone",
    "129" to "Zeppelins",
    "130" to "Worms",
    "131" to "Decomposers",
    "132" to "FusionPower",
    "133" to "SymbioticFungus",
    "134" to "ExtremeColdFungus",
    "135" to "AdvancedEcosystems",
    "136" to "GreatDam",
    "137" to "Cartel",
    "138" to "StripMine",
    "139" to "WavePower",
    "140" to "LavaFlows",
    "141" to "PowerPlantCard",
    "142" to "MoholeArea",
    "143" to "LargeConvoy",
    "144" to "TitaniumMine",
    "145" to "TectonicStressPower",
    "146" to "NitrophilicMoss",
    "147" to "Herbivores",
    "148" to "Insects",
    "149" to "CeosFavoriteProject",
    "150" to "AntiGravityTechnology",
    "151" to "InvestmentLoan",
    "152" to "Insulation",
    "153" to "AdaptationTechnology",
    "154" to "CaretakerContract",
    "155" to "DesignedMicroorganisms",
    "156" to "StandardTechnology",
    "157" to "NitriteReducingBacteria",
    "158" to "IndustrialMicrobes",
    "159" to "Lichen",
    "160" to "PowerSupplyConsortium",
    "161" to "ConvoyFromEuropa",
    "162" to "ImportedGhg",
    "163" to "ImportedNitrogen",
    "164" to "MicroMills",
    "165" to "MagneticFieldGenerators",
    "166" to "Shuttles",
    "167" to "ImportOfAdvancedGhg",
    "168" to "Windmills",
    "169" to "TundraFarming",
    "170" to "AerobrakedAmmoniaAsteroid",
    "171" to "MagneticFieldDome",
    "172" to "Pets",
    "173" to "ProtectedHabitats",
    "174" to "ProtectedValley",
    "175" to "Satellites",
    "176" to "NoctisFarming",
    "177" to "WaterSplittingPlant",
    "178" to "HeatTrappers",
    "179" to "SoilFactory",
    "180" to "FuelFactory",
    "181" to "IceCapMelting",
    "182" to "CorporateStronghold",
    "183" to "BiomassCombustors",
    "184" to "Livestock",
    "185" to "OlympusConference",
    "186" to "RadSuits",
    "187" to "AquiferPumping",
    "188" to "Flooding",
    "189" to "EnergySaving",
    "190" to "LocalHeatTrapping",
    "191" to "PermafrostExtraction",
    "192" to "InventionContest",
    "193" to "Plantation",
    "194" to "PowerInfrastructure",
    "195" to "IndenturedWorkers",
    "196" to "LagrangeObservatory",
    "197" to "TerraformingGanymede",
    "198" to "ImmigrationShuttles",
    "199" to "RestrictedArea",
    "200" to "ImmigrantCity",
    "201" to "EnergyTapping",
    "202" to "UndergroundDetonations",
    "203" to "Soletta",
    "204" to "TechnologyDemonstration",
    "205" to "RadChemFactory",
    "206" to "SpecialDesign",
    "207" to "MedicalLab",
    "208" to "AiCentral",
    "209" to "SmallAsteroid",
    "210" to "SelfReplicatingRobots",
    "211" to "SnowAlgae",
    "212" to "Penguins",
    "213" to "AerialMappers",
    "214" to "AerosportTournament",
    "215" to "AirScrappingExpedition",
    "216" to "AtalantaPlanitiaLab",
    "217" to "Atmoscoop",
    "218" to "CometForVenus",
    "219" to "CorroderSuits",
    "220" to "DawnCity",
    "221" to "DeuteriumExport",
    "222" to "Dirigibles",
    "223" to "ExtractorBalloons",
    "224" to "Extremophiles",
    "225" to "FloatingHabs",
    "226" to "ForcedPrecipitation",
    "227" to "FreyjaBiodomes",
    "228" to "GhgImportFromVenus",
    "229" to "GiantSolarShade",
    "230" to "Gyropolis",
    "231" to "HydrogenToVenus",
    "232" to "IoSulphurResearch",
    "233" to "IshtarMining",
    "234" to "JetStreamMicroscrappers",
    "235" to "LocalShading",
    "236" to "LunaMetropolis",
    "237" to "LuxuryFoods",
    "238" to "MaxwellBase",
    "239" to "MiningQuota",
    "240" to "NeutralizerFactory",
    "241" to "Omnicourt",
    "242" to "OrbitalReflectors",
    "243" to "RotatorImpacts",
    "244" to "SisterPlanetSupport",
    "245" to "Solarnet",
    "246" to "SpinInducingAsteroid",
    "247" to "SponsoredAcademies",
    "248" to "Stratopolis",
    "249" to "StratosphericBirds",
    "250" to "SulphurExports",
    "251" to "SulphurEatingBacteria",
    "252" to "TerraformingContract",
    "253" to "Thermophiles",
    "254" to "WaterToVenus",
    "255" to "VenusGovernor",
    "256" to "VenusMagnetizer",
    "257" to "VenusSoils",
    "258" to "VenusWaystation",
    "259" to "VenusianAnimals",
    "260" to "VenusianInsects",
    "261" to "VenusianPlants",
    "B00A" to "BeginnerCorporationA",
    "B00B" to "BeginnerCorporationB",
    "B00C" to "BeginnerCorporationC",
    "B00D" to "BeginnerCorporationD",
    "B00E" to "BeginnerCorporationE",
    "B01" to "Credicor",
    "B02" to "Ecoline",
    "B03" to "Helion",
    "B04" to "InterplanetaryCinematics",
    "B05" to "Inventrix",
    "B06" to "MiningGuild",
    "B07" to "Phobolog",
    "B08" to "TharsisRepublic",
    "B09" to "Thorgate",
    "B10" to "UnitedNationsMarsInitiative",
    "B11" to "SaturnSystems",
    "B12" to "Teractor",
    "C01" to "Airliners",
    "C02" to "AirRaid",
    "C03" to "AtmoCollectors",
    "C04" to "CommunityServices",
    "C05" to "Conscription",
    "C06" to "CoronaExtractor",
    "C07" to "CryoSleep",
    "C08" to "EarthElevator",
    "C09" to "EcologyResearch",
    "C10" to "FloaterLeasing",
    "C11" to "FloaterPrototypes",
    "C12" to "FloaterTechnology",
    "C13" to "GalileanWaystation",
    "C14" to "HeavyTaxation",
    "C15" to "IceMoonColony",
    "C16" to "ImpactorSwarm",
    "C17" to "InterplanetaryColonyShip",
    "C18" to "JovianLanterns",
    "C19" to "JupiterFloatingStation",
    "C20" to "LunaGovernor",
    "C21" to "LunarExports",
    "C22" to "LunarMining",
    "C23" to "MarketManipulation",
    "C24" to "MartianZoo",
    "C25" to "MiningColony",
    "C26" to "MinorityRefuge",
    "C27" to "MolecularPrinting",
    "C28" to "NitrogenFromTitan",
    "C29" to "PioneerSettlement",
    "C30" to "ProductiveOutpost",
    "C31" to "QuantumCommunications",
    "C32" to "RedSpotObservatory",
    "C33" to "RefugeeCamps",
    "C34" to "ResearchColony",
    "C35" to "RimFreighters",
    "C36" to "SkyDocks",
    "C37" to "SolarProbe",
    "C38" to "SolarReflectors",
    "C39" to "SpacePort",
    "C40" to "SpacePortColony",
    "C41" to "SpinOffDepartment",
    "C42" to "SubZeroSaltFish",
    "C43" to "TitanAirScrapping",
    "C44" to "TitanFloatingLaunchPad",
    "C45" to "TitanShuttles",
    "C46" to "TradeEnvoys",
    "C47" to "TradingColony",
    "C48" to "UrbanDecomposers",
    "C49" to "WarpDrive",
    "CC1" to "Aridor",
    "CC2" to "Arklight",
    "CC3" to "Polyphemos",
    "CC4" to "Poseidon",
    "CC5" to "StormcraftIncorporated",
    "P01" to "AlliedBank",
    "P02" to "AquiferTurbines",
    "P03" to "Biofuels",
    "P04" to "Biolab",
    "P05" to "BiosphereSupport",
    "P06" to "BusinessEmpire",
    "P07" to "DomeFarming",
    "P08" to "Donation",
    "P09" to "EarlySettlement",
    "P10" to "EcologyExperts",
    "P11" to "ExcentricSponsor",
    "P12" to "ExperimentalForest",
    "P13" to "GalileanMining",
    "P14" to "GreatAquifer",
    "P15" to "HugeAsteroid",
    "P16" to "IoResearchOutpost",
    "P17" to "Loan",
    "P18" to "MartianIndustries",
    "P19" to "MetalRichAsteroid",
    "P20" to "MetalsCompany",
    "P21" to "MiningOperations",
    "P22" to "Mohole",
    "P23" to "MoholeExcavation",
    "P24" to "NitrogenShipment",
    "P25" to "OrbitalConstructionYard",
    "P26" to "PolarIndustries",
    "P27" to "PowerGeneration",
    "P28" to "ResearchNetwork",
    "P29" to "SelfSufficientSettlement",
    "P30" to "SmeltingPlant",
    "P31" to "SocietySupport",
    "P32" to "Supplier",
    "P33" to "SupplyDrop",
    "P34" to "UnmiContractor",
    "P35" to "AcquiredSpaceAgency",
    "P36" to "HousePrinting",
    "P37" to "LavaTubeSettlement",
    "P38" to "MartianSurvey",
    "P39" to "Psychrophiles",
    "P40" to "ResearchCoordination",
    "P41" to "SfMemorial",
    "P42" to "SpaceHotels",
    "PC1" to "CheungShingMars",
    "PC2" to "PointLuna",
    "PC3" to "RobinsonIndustries",
    "PC4" to "ValleyTrust",
    "PC5" to "Vitor",
    "T01" to "AerialLenses",
    "T02" to "BannedDelegate",
    "T03" to "CulturalMetropolis",
    "T04" to "DiasporaMovement",
    "T05" to "EventAnalysts",
    "T06" to "GmoContract",
    "T07" to "MartianMediaCenter",
    "T08" to "ParliamentHall",
    "T09" to "PrOffice",
    "T10" to "PublicCelebrations",
    "T11" to "Recruitment",
    "T12" to "RedTourismWave",
    "T13" to "SponsoredMohole",
    "T14" to "SupportedResearch",
    "T15" to "WildlifeDome",
    "T16" to "VoteOfNoConfidence",
    "TC1" to "LakefrontResorts",
    "TC2" to "Pristar",
    "TC3" to "SeptemTribus",
    "TC4" to "TerralabsResearch",
    "TC5" to "UtopiaInvest",
    "V01" to "Aphrodite",
    "V02" to "Celestic",
    "V03" to "Manutech",
    "V04" to "MorningStarInc",
    "V05" to "Viron",
    "X01" to "DuskLaserMining",
    "X02" to "ProjectInspection",
    "X03" to "EnergyMarket",
    "X04" to "HiTechLab",
    "X05" to "InterplanetaryTrade",
    "X06" to "LawSuit",
    "X07" to "MercurianAlloys",
    "X08" to "OrbitalCleanup",
    "X09" to "PoliticalAlliance",
    "X10" to "RegoPlastics",
    "X11" to "SaturnSurfing",
    "X12" to "StanfordTorus",
    "X13" to "Advertising",
    "X14" to "AsteroidDeflectionSystem",
    "X15" to "AsteroidHollowing",
    "X16" to "CometAiming",
    "X17" to "CrashSiteCleanup",
    "X18" to "CuttingEdgeTechnology",
    "X19" to "DirectedImpactors",
    "X20" to "DiversitySupport",
    "X21" to "FieldCappedCity",
    "X22" to "ImportedNutrients",
    "X23" to "JovianEmbassy",
    "X24" to "MagneticShield",
    "X25" to "MeatIndustry",
    "X26" to "Meltworks",
    "X27" to "MoholeLake",
    "X28" to "Potatoes",
    "X29" to "SubCrustMeasurements",
    "X30" to "TopsoilContract",
    "X31" to "DeimosDownPromo",
    "X32" to "GreatDamPromo",
    "X33" to "MagneticFieldGeneratorsPromo",
    "X34" to "AsteroidRights",
    "X35" to "BactoviralResearch",
    "X36" to "BioPrintingFacility",
    "X37" to "Harvest",
    "X38" to "OutdoorSports",
    "X39" to "CorporateArchives",
    "X40" to "DoubleDown",
    "X41" to "Merger",
    "X42" to "NewPartner",
    "X43" to "HeadStart",
    "XC1" to "ArcadianCommunities",
    "XC2" to "Recyclon",
    "XC3" to "SpliceTacticalGenomics",
    "XC4" to "Factorum",
    "XC5" to "MonsInsurance",
    "XC6" to "Philares",
    "XC7" to "Astrodrill",
    "XC8" to "PharmacyUnion",
)
