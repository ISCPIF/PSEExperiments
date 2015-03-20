/*
 * Copyright (C) 2015 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
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

package org.openmole.core.dsl

import org.openmole.core.workflow._

trait Classes {

  lazy val Prototype = org.openmole.core.workflow.data.Prototype

  lazy val LocalEnvironment = org.openmole.core.workflow.execution.local.LocalEnvironment

  lazy val EmptyTask = task.EmptyTask
  lazy val ExplorationTask = task.ExplorationTask
  lazy val MoleTask = task.MoleTask

  lazy val Capsule = mole.Capsule
  lazy val StrainerCapsule = mole.StrainerCapsule
  lazy val MasterCapsule = mole.MasterCapsule

  lazy val Slot = transition.Slot

}
