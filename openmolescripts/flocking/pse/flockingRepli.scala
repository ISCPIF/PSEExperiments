
val itermax=100000
val nbreplications=100

val vision = Val[Double]
val minimumSeparation = Val[Double]
val maxAlignTurn = Val[Double]
val maxCohereTurn = Val[Double]
val maxSeparateTurn = Val[Double]

val groupCount = Val[Double]
val relativeDiffusion = Val[Double]
val velocity = Val[Double]

val seed = Val[Long]

val modelTask =
  ScalaTask("""
    |val behaviour = Behaviour(32.0, 32.0, 128, 0.5, vision, minimumSeparation, maxAlignTurn, maxCohereTurn, maxSeparateTurn)
    |val groupCount = behaviour(0)
    |val relativeDiffusion = if(behaviour(1) < -1.0) -1.0 else behaviour(1)
    |val velocity = behaviour(2)""".stripMargin
  ) set (
    usedClasses += classOf[fr.iscpif.flocking.behaviour.Behaviour],
    imports += "fr.iscpif.flocking.behaviour._",
    inputs += (vision, minimumSeparation, maxAlignTurn, maxCohereTurn, maxSeparateTurn, seed),
    outputs += (groupCount, relativeDiffusion, velocity)
  )


val model = Capsule(modelTask)


val stat = 
  StatisticsTask() set (
    statistics += (groupCount, groupCount, median),
    statistics += (relativeDiffusion, relativeDiffusion, median),
    statistics += (velocity, velocity, median)
  )

val replicateModel = Capsule(MoleTask(Replicate(model, seed in UniformDistribution[Long]() take nbreplications, stat)))

val scales =
  Seq(
    vision -> (0.0, 32.0),
    minimumSeparation -> (0.0, 32.0),
    maxAlignTurn -> (0.0, math.Pi),
    maxCohereTurn -> (0.0, math.Pi),
    maxSeparateTurn -> (0.0, math.Pi)
  )

val evolution =
  BehaviourSearch (
    termination = itermax,
    inputs = scales,
    observables = Seq(groupCount, relativeDiffusion, velocity),
    gridSize = Seq(1.0, 0.2, 0.05),
    reevaluate = 0.01
  )

val nsga2 = SteadyGA(evolution)(replicateModel, 5000)

// Define the execution
val env = DIRACEnvironment("biomed", "https://ccdirac06.in2p3.fr:9178", cpuTime = 4 hours, openMOLEMemory = 1200)

def execution(i: Int) = {
  val hookCondition = s"${nsga2.generation.name} % 100 == 0"
  val savePopulation = SavePopulationHook(nsga2, s"./${i}/") when hookCondition
  val display = DisplayHook("Generation ${" + nsga2.generation.name + "} for " + i)
  (nsga2.puzzle + (replicateModel by 10 on env) + (nsga2.output hook savePopulation hook display)) toExecution
}

val executions = (0 until 10).map(execution)
executions.foreach(_.start)

