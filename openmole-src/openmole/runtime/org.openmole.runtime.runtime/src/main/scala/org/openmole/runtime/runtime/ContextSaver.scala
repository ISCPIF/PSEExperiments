/*
 * Copyright (C) 2010 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.runtime.runtime

import java.util.concurrent.Semaphore
import org.openmole.core.tools.service.Logger
import org.openmole.core.workflow.data._
import org.openmole.core.workflow.job._
import org.openmole.core.workflow.tools._
import org.openmole.core.workflow.job.State._
import scala.collection.immutable.TreeMap
import util.{ Failure, Success, Try }

object ContextSaver extends Logger

import ContextSaver.Log._

class ContextSaver(val nbJobs: Int) {

  val allFinished = new Semaphore(0)

  var nbFinished = 0
  var _results = new TreeMap[MoleJobId, Try[Context]]
  def results = _results

  def save(job: MoleJob, oldState: State, newState: State) = synchronized {
    newState match {
      case COMPLETED | FAILED ⇒
        job.exception match {
          case None ⇒
            logger.fine(s"Job success ${job.id} ${job.context}")
            _results += job.id -> Success(job.context)
          case Some(t) ⇒
            logger.log(FINE, s"Job failure ${job.id}", t)
            _results += job.id -> Failure(t)
        }
      case _ ⇒
    }

    if (newState.isFinal) {
      nbFinished += 1
      if (nbFinished >= nbJobs) allFinished.release
    }
  }

  def waitAllFinished = {
    allFinished.acquire
    allFinished.release
  }

}
