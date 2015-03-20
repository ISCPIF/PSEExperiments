logger.level("FINE")

// Define the variables
val economicMultiplier = Val[Double]
val populationToWealthExponent = Val[Double]
val sizeEffectOnSupply = Val[Double]
val sizeEffectOnDemand = Val[Double]
val wealthToPopulationExponent = Val[Double]
val distanceDecay = Val[Double]
val bonusMultiplier = Val[Double]
val fixedCost = Val[Double]

val slope = Val[Double]
val popdiff = Val[Double]
val inversions = Val[Double]

val modelTask = 
  ScalaTask("""
    |val model = 
    |  new Marius with Bonus with FixedCostTransaction {
    |    def economicMultiplier = input.economicMultiplier
    |    def sizeEffectOnSupply = input.sizeEffectOnSupply
    |    def sizeEffectOnDemand = input.sizeEffectOnDemand
    |    def distanceDecay = input.distanceDecay
    |    def wealthToPopulationExponent = input.wealthToPopulationExponent
    |    def populationToWealthExponent = input.populationToWealthExponent
    |    def bonusMultiplier = input.bonusMultiplier
    |    def fixedCost = input.fixedCost 
    |    def census = 0
    |    def steps = 30
    |  }
    |
    |val res = BehaviourComputing(model).compute
    |val slope = if(res(0) < -10) -10 else res(0)
    |val popdiff = if(res(1) > 500000) 500000 else res(1)
    |val inversions = if(res(2) > 3) 3 else res(2)""".stripMargin
  ) set (
    usedClasses += classOf[fr.geocites.marius.Marius],
    imports += "fr.geocites.marius.behaviour._",
    imports += "fr.geocites.gugus.transaction._",
    imports += "fr.geocites.gugus.balance._",
    imports += "fr.geocites.marius._",
    inputs += (economicMultiplier, sizeEffectOnSupply, sizeEffectOnDemand, wealthToPopulationExponent, distanceDecay, populationToWealthExponent, bonusMultiplier, fixedCost),
    outputs += (slope, popdiff, inversions)
  )

val model = Capsule(modelTask)

val scales = 
  Seq(
    economicMultiplier -> (0.0, 100.0),
    sizeEffectOnSupply -> (1.0, 2.0),
    sizeEffectOnDemand -> (1.0, 2.0),
    wealthToPopulationExponent -> (0.0,2.0),
    distanceDecay -> (0.0, 10.0),
    populationToWealthExponent -> (1.0, 2.0),
    bonusMultiplier -> (0.0, 1000.0),
    fixedCost -> (0.0, 1000.0)
  )

val evolution = 
  BehaviourSearch (
    termination = 2 hours,
    inputs = scales,
    observables = Seq(slope, popdiff, inversions),
    gridSize = Seq(0.5, 5000.0, 1.0)
  )

val (ga, island) = IslandSteadyGA(evolution, model)(5000, 200000, 1000)

val hookCondition = s"${ga.generation.name} % 100 == 0"
val savePopulation = SavePopulationHook(ga, "pattern2") condition hookCondition

// Define the hook to display the generation in the console
val display = DisplayHook("Generation ${" + ga.generation.name + "}")

val env = DIRACEnvironment("biomed", "https://ccdirac06.in2p3.fr:9178", cpuTime = 4 hours, openMOLEMemory = 1200)

// Define the execution
val ex =
 (ga.puzzle + 
   (island on env) + 
   (ga.output hook savePopulation hook display)) toExecution

ex.start

