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

package controllers

import javax.inject.Inject

import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import models.ReportId
import org.joda.time.LocalDate
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.mvc.{Action, Controller, ResponseHeader, Result}
import services._

import scala.concurrent.ExecutionContext

class ReportsController @Inject()(
  val reportService: ReportService
)(implicit val ec: ExecutionContext)
  extends Controller {
  //noinspection TypeAnnotation
  def count = Action.async {
    reportService.count.map(count => Ok(obj("reportCount" -> count)))
  }

  def reports = Action { implicit request =>
    val publisher = reportService.list(LocalDate.now().minusMonths(24))

    val rowSource = Source.fromPublisher(publisher)
      .map(row => ByteString(Json.prettyPrint(Json.toJson(row))))
      .via(Flow[ByteString].intersperse(ByteString("["), ByteString(","), ByteString("]")))

    val entity = HttpEntity.Streamed(rowSource, None, Some("application/json"))
    Result(ResponseHeader(OK, Map()), entity)
  }

  //noinspection TypeAnnotation
  def report(reportId: ReportId) = Action.async { implicit request =>
    reportService.find(reportId).map {
      case Some(report) => Ok(Json.toJson(report))
      case None         => NotFound
    }
  }
}
