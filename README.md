# PSEExperiments

This git repository contains the necessary files to reproduce the experiments presented in the paper "Beyond corroboration: strengthening model validation by looking for unexpected patterns." submitted to PLOS ONE by Guillaume Chérel, Romain Reuillon and Clémentine Cottineau.

In the experiments, we use the OpenMOLE software to run the exploration methods on the models flocking and MARIUS, and run the simulations on the EGI grid. The this document gives the directions to reproduce the experiments.

The final section of this paper gives directions on how to use the PSE method directly from the MGO library, without OpenMOLE.

## Quickstart

To run the experiment with PSE and the model flocking:

1. Clone this repository 

2. Make sure you have access to the biomed V.O. or modify the scripts you want to run to use other environments.

3. Start OpenMOLE in console mode with the jar and script files corresponding to the experiment you want to run and its associated model `openmole/openmole -c -p flockingbehaviour_2.11-0.2-SNAPSHOT.jar`

4. At the OpenMOLE prompt, type `:load openmolescripts/flocking/pse/flockingRepli.scala`

**Important:** To interrupt the distributed computation, you must do so explicitly before quitting OpenMOLE. If you don't, you will loose access to them. To quit the computation, run `ex.cancel` at the OpenMOLE prompt if you are running the MARIUS experiment script, and call `executions.foreach(_.cancel)` for any of the flocking experiment script.

## OpenMOLE

In order to run the experiments scripts, you will need to use [OpenMOLE](http://www.openmole.org). OpenMOLE is a software under active development, and as versions progresses the scripts in this repository can become obsolete. For this reason, this repository includes a frozen version of the software as of the time of the experiments. The directory `openmole` contains the executable software, and `openmole-src` contains the associated source code. Scala developpers willing to rebuild openmole from this source should follow these steps:

```
cd libraries
sbt install
cd ../openmole
sbt assemble
```

Please note that for any use of OpenMOLE other than reproducing these experiments, it is advised to use the current version of OpenMOLE on the [official website](http://www.openmole.org).

## Models

In order to run the experiments with OpenMOLE, the models must be available to OpenMOLE. The source of the models is available in the `simpuzzle` directory. This directory is a frozen version of [the simpuzzle git repository](https://github.com/ISCPIF/simpuzzle) as it was at the time of the experiments.Ready to use versions of the models are available in the `flockingbehaviour_2.11_0.2-SNAPSHOT.jar` and `mariusbehaviour_2.11_0.2-SNAPSHOT.jar` files at the root directories. To rebuild the models from the frozen sources, run the script `makeModels.sh`. You will need to have sbt installed to run the script.

## OpenMOLE scripts - Experiments

The folder `openmolescripts/` and its subfolders contains the scripts to run each experiment presented in the paper (in these scripts, PSE is refered to as BehaviourSearch):
- `flocking/`: 4 experiments with the collective behaviour model and each exploration method: pse, lhs, sobol, grid
- `marius/`: 1 experiment with the marius model using pse.

To rerun an experiment, type from the root directory:
```
openmole/openmole -c -s path_to_the_experiment_script.scala -p path_to_the_modeljar.jar 
```

The scripts are design to run the simulations on the [EGI grid](egi.eu) with the virtual organisation (V.O.) [biomed](http://lsgc.org/en/Biomed:home).
If you don't have access the V.O., please make sure to first get an access or modify the script to use a different environment. Please refer to the [OpenMOLE documentation](http://www.openmole.org/current/documentation_console_environment.html) for detailed instructions on how to deploy your simulation on other computing environment. However, note that these experiments required several days or weeks to run on the grid, and will probably take a prohibitively long time on less powerful computing environments.

To interrupt the distributed computation, you must do so explicitly before quitting OpenMOLE. If you don't, you will loose control over them. To quit the computation, you must call the `cancel` method of all the objects responsible for running the distant computations. In the MARIUS script, the only such object is `ex` (run `ex.cancel` at the OpenMOLE). In all the flocking scripts, the variable executions is a list of such objects. Call `executions.foreach(_.cancel)` if you are running any of the flocking scripts.

## MGO

For developpers who would like to integrate PSE in their applications, the method is directly available in the MGO scala library. MGO is available from its [github repository](https://github.com/openmole/mgo). The repository is designed to be used with [sbt](http://www.scala-sbt.org/). PSE is available in the file `src/main/scala/fr/iscpif/mgo/algorithm/PSE.scala` in the MGO repository, and the file `src/main/scala/fr/iscpif/mgo/test/TestPSE.scala` gives a example on how to run PSE on the ZDT4 function. The test file should provide a simple enough examples for developpers to get started using PSE. To run the example, simply enter sbt at the root of the MGO repository, and type `run`, then select `TestPSE`. The test will search for parameter values giving different output values according to the ZDT4 function. The whole population for each generation is written in `/tmp/PSE/`. Each individual in a population is described by the input parameters (par0, par1), the output values (bhv0,bhv1), the corresponding archive cell (niche0,niche1) and the cumulative number of hits in this cell.