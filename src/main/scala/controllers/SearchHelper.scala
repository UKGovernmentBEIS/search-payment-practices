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

import models.{CompaniesHouseId, PagedResults}
import play.api.Logger
import play.twirl.api.Html
import services.{CompanySearchResult, CompanySearchService, ReportService}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait SearchHelper {
  def companySearch: CompanySearchService

  def reportService: ReportService

  implicit def ec: ExecutionContext

  type ResultsPageFunction = (String, Option[PagedResults[CompanySearchResult]]) => Html
  type ResultsErrorFunction = (String, String) => Html

  def doSearch(query: Option[String], pageNumber: Option[Int], itemsPerPage: Option[Int], resultsPage: ResultsPageFunction, resultsError: ResultsErrorFunction, timeout: Duration): Future[Html] = {
    query match {
      case Some(q) => companySearch.searchCompanies(q, pageNumber.getOrElse(1), itemsPerPage.getOrElse(25), timeout).flatMap { results =>
        Future.apply(resultsPage(q, Some(results)))
      }.recover {
        case NonFatal(e) =>
          Logger.warn(e.getMessage)
          resultsError(q, "We're having trouble connecting to the Companies House search service. Please try again in a few minutes.")
      }

      case None => Future.successful(resultsPage("", None))
    }
  }
}
