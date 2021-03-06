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

package org.openmole.core.tools.collection

import org.scalatest.FlatSpec
import org.scalatest._
import org.scalatest.junit._

class OrderedSlidingListSpec extends FlatSpec with Matchers {
  "OrderedSlidingList" should "preserve the max size" in {
    val list = new OrderedSlidingList[Int](5)
    list += 2
    list += 8
    list += 7
    list += 7

    list.size should equal(4)

    list += 3
    list += 9
    list += 7

    list.size should equal(5)
  }

}
