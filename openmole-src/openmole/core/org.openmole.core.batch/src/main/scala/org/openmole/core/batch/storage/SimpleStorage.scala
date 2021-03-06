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

package org.openmole.core.batch.storage

import fr.iscpif.gridscale
import fr.iscpif.gridscale.storage.{ Storage ⇒ GSStorage, FileType }
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.UUID
import org.openmole.core.batch.control._
import org.openmole.core.batch.environment._
import org.openmole.core.tools.io.FileUtil
import org.openmole.core.tools.service.Logger
import FileUtil._

import scala.collection.JavaConversions._

trait SimpleStorage extends Storage {
  override def exists(path: String): Boolean = super.exists(path)
  override def listNames(path: String): Seq[String] = super.listNames(path)
  override def list(path: String): Seq[(String, FileType)] = super.list(path)
  override def makeDir(path: String): Unit = super.makeDir(path)
  override def rmDir(path: String): Unit = super.rmDir(path)
  override def rmFile(path: String): Unit = super.rmFile(path)
  override def mv(from: String, to: String) = super.mv(from, to)

  override def openInputStream(path: String): InputStream = super.openInputStream(path)
  override def openOutputStream(path: String): OutputStream = super.openOutputStream(path)

  override def upload(src: File, dest: String): Unit = super.upload(src, dest)
  override def uploadGZ(src: File, dest: String): Unit = super.uploadGZ(src, dest)
  override def download(src: String, dest: File): Unit = super.download(src, dest)
  override def downloadGZ(src: String, dest: File): Unit = super.downloadGZ(src, dest)

  def create(dest: String) = {
    val os = openOutputStream(dest)
    try os.append("")
    finally os.close
  }

}
