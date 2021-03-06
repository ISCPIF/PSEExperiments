/*
 * Copyright (C) 2010 Romain Reuillon
 * Copyright (C) 2014 Jonathan Passerat-Palmbach
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

package org.openmole.core.batch.jobservice

import org.openmole.core.eventdispatcher.{ Event, EventDispatcher }
import org.openmole.core.exception.InternalProcessingError
import org.openmole.core.workflow.execution.ExecutionState
import org.openmole.core.workflow.execution.ExecutionState._
import org.openmole.core.batch.control._
import org.openmole.core.batch.environment._

object BatchJob {
  case class StateChanged(newState: ExecutionState.ExecutionState, oldState: ExecutionState.ExecutionState) extends Event[BatchJob]
}

trait BatchJob { bj ⇒

  val jobService: JobService
  def resultPath: String

  var _state: ExecutionState = READY

  protected[jobservice] def state_=(state: ExecutionState) = synchronized {
    if (_state < state) {
      EventDispatcher.trigger(this, new BatchJob.StateChanged(state, _state))
      _state = state
    }
  }

  def hasBeenSubmitted: Boolean = state.compareTo(SUBMITTED) >= 0

  def kill(implicit token: AccessToken)
  def updateState(implicit token: AccessToken): ExecutionState

  def kill(id: jobService.J)(implicit token: AccessToken) = token.synchronized {
    synchronized {
      val oldState = state
      try if (state == SUBMITTED || state == RUNNING) jobService.cancel(id)
      finally state = KILLED
      if (oldState != KILLED) jobService.purge(id)
    }
  }

  def updateState(id: jobService.J)(implicit token: AccessToken): ExecutionState = token.synchronized {
    synchronized {
      state = jobService.state(id)
      state
    }
  }

  def state: ExecutionState = _state

}
