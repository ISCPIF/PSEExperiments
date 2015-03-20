
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
    outputs += (vision, minimumSeparation, maxAlignTurn, maxCohereTurn, maxSeparateTurn, groupCount, relativeDiffusion, velocity)
  )

val model = Capsule(modelTask)

import org.openmole.plugin.method.stochastic._

val stat = 
  StatisticsTask() set (
    statistics += (groupCount, groupCount, median),
    statistics += (relativeDiffusion, relativeDiffusion, median),
    statistics += (velocity, velocity, median)
  )

val seedFactor = seed in (UniformDistribution[Long]() take nbreplications)
val replicateModel = StrainerCapsule(MoleTask(Replicate(model, seedFactor, stat)))

// Define the execution
val env = DIRACEnvironment("biomed", "https://ccdirac06.in2p3.fr:9178", cpuTime = 10 hours, openMOLEMemory = 1200)

def execution(samples: Int) = {
  val explo =
    ExplorationTask (
      SobolSampling (
        samples,
        (vision in Range(0.0, 32.0)),
        (minimumSeparation in Range(0.0, 32.0)),
        (maxAlignTurn in Range(0.0, math.Pi)),
        (maxCohereTurn in Range(0.0, math.Pi)),
        (maxSeparateTurn in Range(0.0, math.Pi))
      )
    )

  val outputCSV = AppendToCSVFileHook(s"./sobol${samples}.csv", vision, minimumSeparation, maxAlignTurn, maxCohereTurn, maxSeparateTurn, groupCount, relativeDiffusion, velocity)
  explo -< (replicateModel on env by 20 hook outputCSV) toExecution
}

val executions = 
  for {
    samples <- 10000 to 100000 by 10000
  } yield execution(samples)

executions.foreach(_.start)

