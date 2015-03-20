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

package org.openmole.core.workflow.execution

import org.openmole.core.eventdispatcher.EventDispatcher
import org.openmole.core.workflow.job.MoleJob
import ExecutionState._

trait ExecutionJob {
  def environment: Environment
  def moleJobs: Iterable[MoleJob]

  private var _state: ExecutionState = READY

  def state = _state

  def state_=(state: ExecutionState) = synchronized {
    if (!this.state.isFinal) {
      EventDispatcher.trigger(environment, new Environment.JobStateChanged(this, state, this.state))
      _state = state
    }
  }
}
