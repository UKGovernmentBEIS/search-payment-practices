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

import config.{PageConfig, ServiceConfig}
import play.api.mvc.{Action, Controller}

class HomeController @Inject()(
  val pageConfig: PageConfig,
  serviceConfig: ServiceConfig
) extends Controller with PageHelper {

  private val pageTitle = "Search published payment practices reports"

  def index = Action { request =>
    serviceConfig.rootRedirectURL match {
      case None      => Redirect(controllers.routes.HomeController.start())
      case Some(url) => Redirect(url)
    }
  }

  def start = Action { implicit request =>
    Ok(page(pageTitle)(views.html.search.start()))
  }

  def maintenance = Action { implicit request =>
    Ok(page(pageTitle)(views.html.maintenance()))
  }

  /**
    * See https://www.gov.uk/service-manual/technology/managing-domain-names#using-robotstxt-and-root-level-redirections
    */
  def robots = Action {
    Ok("User-agent: * Disallow: /")
  }
}
