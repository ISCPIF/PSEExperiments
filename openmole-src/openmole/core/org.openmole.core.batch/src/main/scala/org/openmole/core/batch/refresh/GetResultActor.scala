/*
 * Copyright (C) 2012 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.core.batch.refresh

import akka.actor.Actor
import akka.actor.ActorRef
import java.io.FileInputStream
import java.io.IOException
import org.openmole.core.batch.control._
import org.openmole.core.batch.environment._
import org.openmole.core.batch.message._
import org.openmole.core.batch.storage._
import org.openmole.core.eventdispatcher.EventDispatcher
import org.openmole.core.exception.InternalProcessingError
import org.openmole.core.tools.io.{ HashService, FileUtil }
import org.openmole.core.tools.service.Logger
import org.openmole.core.workflow.execution._
import org.openmole.core.workflow.job._
import org.openmole.core.serializer.SerialiserService
import org.openmole.core.batch.environment.BatchEnvironment._
import FileUtil._
import org.openmole.core.workspace.Workspace
import util.{ Failure, Success }

object GetResultActor extends Logger

import GetResultActor.Log._

class GetResultActor(jobManager: ActorRef) extends Actor {

  def receive = withRunFinalization {
    case msg @ GetResult(job, sj, resultPath) ⇒
      try sj.storage.tryWithToken {
        case Some(token) ⇒
          getResult(sj.storage, resultPath, job)(token)
          jobManager ! Kill(job)
        case None ⇒
          jobManager ! Delay(msg, Workspace.preferenceAsDuration(BatchEnvironment.NoTokenForServiceRetryInterval))
      } catch {
        case e: Throwable ⇒
          jobManager ! Error(job, e)
          jobManager ! Kill(job)
      }
  }

  def getResult(storage: StorageService, outputFilePath: String, batchJob: BatchExecutionJob)(implicit token: AccessToken): Unit = {
    import batchJob.job
    val runtimeResult = getRuntimeResult(outputFilePath, storage)

    display(runtimeResult.stdOut, "Output", storage)
    display(runtimeResult.stdErr, "Error output", storage)

    runtimeResult.result match {
      case Failure(exception) ⇒ throw new JobRemoteExecutionException(exception, "Fatal exception thrown during the execution of the job execution on the execution node")
      case Success((result, log)) ⇒
        val contextResults = getContextResults(result, storage)

        EventDispatcher.trigger(storage.environment: Environment, Environment.JobCompleted(batchJob, log))

        //Try to download the results for all the jobs of the group
        for (moleJob ← job.moleJobs) {
          if (contextResults.results.isDefinedAt(moleJob.id)) {
            val executionResult = contextResults.results(moleJob.id)
            executionResult match {
              case Success(context) ⇒ moleJob.finish(context)
              case Failure(e)       ⇒ if (!moleJob.finished) sender ! MoleJobError(moleJob, batchJob, e)
            }
          }
        }
    }
  }

  private def getRuntimeResult(outputFilePath: String, storage: StorageService)(implicit token: AccessToken): RuntimeResult = {
    val resultFile = Workspace.newFile
    try {
      signalDownload(storage.downloadGZ(outputFilePath, resultFile), outputFilePath, storage)
      SerialiserService.deserialise(resultFile)
    }
    finally resultFile.delete
  }

  private def display(message: Option[FileMessage], description: String, storage: StorageService)(implicit token: AccessToken) = {
    message match {
      case Some(message) ⇒
        try {
          val tmpFile = Workspace.newFile
          try {
            signalDownload(storage.downloadGZ(message.path, tmpFile), message.path, storage)

            /*val stdOutHash = HashService.computeHash(stdOutFile)
             if (stdOutHash != message.hash)
             logger.log(WARNING, "The standard output has been corrupted during the transfer.")
             */

            System.out.synchronized {
              System.out.println("-----------------" + description + " on remote host-----------------")
              val fis = new FileInputStream(tmpFile)
              try fis.copy(System.out) finally fis.close
              System.out.println("-------------------------------------------------------")
            }
          }
          finally tmpFile.delete
        }
        catch {
          case (e: IOException) ⇒
            logger.log(WARNING, description + " transfer has failed.")
            logger.log(FINE, "Stack of the error during tranfert", e)
        }
      case None ⇒
    }
  }

  private def getContextResults(resultPath: FileMessage, storage: StorageService)(implicit token: AccessToken): ContextResults = {
    if (resultPath == null) throw new InternalProcessingError("Context results path is null")
    val contextResutsFileCache = Workspace.newFile
    try {
      signalDownload(storage.downloadGZ(resultPath.path, contextResutsFileCache), resultPath.path, storage)
      if (HashService.computeHash(contextResutsFileCache) != resultPath.hash) throw new InternalProcessingError("Results have been corrupted during the transfer.")
      SerialiserService.deserialiseAndExtractFiles(contextResutsFileCache)
    }
    finally contextResutsFileCache.delete
  }

}
