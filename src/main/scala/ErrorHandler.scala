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

import javax.inject._

import config.PageConfig
import controllers.PageHelper
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(
                              env: Environment,
                              conf: Configuration,
                              sourceMapper: OptionalSourceMapper,
                              router: Provider[Router],
                              val pageConfig: PageConfig
                            )
  extends DefaultHttpErrorHandler(env, conf, sourceMapper, router)
    with PageHelper {

  override protected def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    implicit val r: RequestHeader = request
    Future.successful(InternalServerError(page("Something went wrong at our end")(home, views.html.errors.error500())))
  }

  override protected def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    implicit val r: RequestHeader = request
    Future.successful(InternalServerError(page("We could not handle that request")(home, views.html.errors.error400())))
  }

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    implicit val r: RequestHeader = request
    if (env.mode != Mode.Prod) super.onNotFound(request, message)
    else Future.successful(NotFound(page("Page not found")(home, views.html.errors.error404())))
  }
}