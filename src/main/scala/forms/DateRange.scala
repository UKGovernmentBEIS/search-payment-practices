/*
 * Copyright (C) 2017  Department for Business, Energy and Industrial Strategy
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

package forms

import org.joda.time.{LocalDate, Months}
import play.api.libs.json.{Json, OFormat}

case class DateRange(startDate: LocalDate, endDate: LocalDate) {
  def startsOnOrAfter(localDate: LocalDate): Boolean = !startDate.isBefore(localDate)

  val monthsInRange: Int = Months.monthsBetween(startDate, endDate).getMonths + 1

  /**
    * Add the number of months to the date by the usual logic, but if the incoming date
    * represents the last day of the month, then ensure the resulting date is adjusted to the
    * last day of the month if needed. E.g. 28 Feb + 6 months would give 31 August, not 28th August.
    */
  def addMonthsWithStickyEnd(input: LocalDate, months: Int): LocalDate = {
    if (input.getDayOfMonth == input.dayOfMonth().withMaximumValue().getDayOfMonth)
      input.plusMonths(months).dayOfMonth().withMaximumValue()
    else
      input.plusMonths(months)
  }
}

object DateRange {
  implicit val format: OFormat[DateRange] = Json.format
}