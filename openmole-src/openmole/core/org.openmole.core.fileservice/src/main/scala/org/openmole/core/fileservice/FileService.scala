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

package org.openmole.core.fileservice

import com.ice.tar.TarOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.logging.Logger
import org.openmole.core.filecache.{ FileCacheDeleteOnFinalize, IFileCache }
import org.openmole.core.tools.cache.AssociativeCache
import org.openmole.core.tools.io.{ HashService, FileUtil, TarArchiver }
import org.openmole.core.tools.service.Hash
import FileUtil._
import TarArchiver._
import org.openmole.core.updater.Updater
import org.openmole.core.workspace.{ Workspace, ConfigurationLocation }

object FileService {
  val GCInterval = new ConfigurationLocation("FileService", "GCInterval")
  Workspace += (GCInterval, "PT5M")

  //class CachedArchiveForDir(file: File, val lastModified: Long) extends FileCacheDeleteOnFinalize(file)

  private[fileservice] val hashCache = new AssociativeCache[String, Hash]
  private[fileservice] val archiveCache = new AssociativeCache[String, IFileCache]

  Updater.delay(new FileServiceGC, Workspace.preferenceAsDuration(FileService.GCInterval))

  def hash(file: File): Hash =
    hash(file, if (file.isDirectory) archiveForDir(file).file(false) else file)

  def invalidate(key: Object, file: File) = hashCache.invalidateCache(key, file.getAbsolutePath)

  def archiveForDir(file: File): IFileCache = archiveForDir(file, file)

  def hash(key: Object, file: File): Hash =
    hashCache.cache(
      key,
      file.getAbsolutePath,
      HashService.computeHash(if (file.isDirectory) archiveForDir(key, file).file(false) else file))

  def archiveForDir(key: Object, file: File) = {
    archiveCache.cache(key, file.getAbsolutePath, {
      val ret = Workspace.newFile("archive", ".tar")
      val os = new TarOutputStream(new FileOutputStream(ret))
      try os.createDirArchiveWithRelativePathNoVariableContent(file)
      finally os.close
      new FileCacheDeleteOnFinalize(ret)
    })
  }

}

