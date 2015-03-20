#!/bin/bash

SCALAVERSION=2.11

cd simpuzzle-src &&
sbt 'project flockingbehaviour' osgiBundle 'project mariusbehaviour' osgiBundle && 
cp models/flocking/behaviour/target/scala-$SCALAVERSION/flockingbehaviour_$SCALAVERSION-0.2-SNAPSHOT.jar ../  
cp models/marius/behaviour/target/scala-$SCALAVERSION/mariusbehaviour_$SCALAVERSION-0.2-SNAPSHOT.jar ../

