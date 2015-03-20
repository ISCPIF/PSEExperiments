/*
 * Copyright (C) 2011 Romain Reuillon
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

package org.openmole.plugin.environment.desktopgrid

import org.openmole.core.batch.environment._
import org.openmole.core.batch.storage._
import org.openmole.core.batch.control._
import org.openmole.core.workspace.Workspace

import org.openmole.plugin.tool.sftpserver.SFTPServer
import java.net.URI
import fr.iscpif.gridscale.storage.{ LocalStorage ⇒ GSLocalStorage }

object DesktopGridEnvironment {
  val timeStempsDirName = "timeStemps"
  val jobsDirName = "jobs"
  val tmpJobsDirName = "tmpjobs"
  val resultsDirName = "results"
  val tmpResultsDirName = "tmpresults"
  val timeStempSeparator = '@'

  def apply(
    port: Int,
    login: String,
    password: String,
    openMOLEMemory: Option[Int] = None,
    threads: Option[Int] = None) =
    new DesktopGridEnvironment(port, login, password, openMOLEMemory, threads)
}

class DesktopGridEnvironment(
    val port: Int,
    val login: String, password: String,
    override val openMOLEMemory: Option[Int],
    override val threads: Option[Int]) extends BatchEnvironment { env ⇒

  type SS = VolatileStorageService
  type JS = DesktopGridJobService

  val path = Workspace.newDir
  new SFTPServer(path, login, password, port)

  val url = new URI("desktop", login, "localhost", port, null, null, null)

  @transient lazy val batchStorage = new VolatileStorageService with UnlimitedAccess {
    def environment = env
    val remoteStorage: RemoteStorage = new DumyStorage
    def url = env.url
    def root = path.getPath
    val storage = new GSLocalStorage {}
    val id = url.toString
  }

  @transient override lazy val allStorages = List(batchStorage)

  @transient override lazy val allJobServices = List(
    new DesktopGridJobService {
      def environment = env
    })

}
