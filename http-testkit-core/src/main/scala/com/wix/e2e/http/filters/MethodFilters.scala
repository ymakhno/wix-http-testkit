package com.wix.e2e.http.filters

import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import com.wix.e2e.http.RequestFilter

trait MethodFilters {

  def methodIsGet: RequestFilter = methodIs(HttpMethods.GET)
  def methodIsPost: RequestFilter = methodIs(HttpMethods.POST)
  def methodIsDelete: RequestFilter = methodIs(HttpMethods.DELETE)
  def methodIsPut: RequestFilter = methodIs(HttpMethods.PUT)
  def methodIsPatch: RequestFilter = methodIs(HttpMethods.PATCH)

  def methodIs(method: HttpMethod): RequestFilter = { _.method == method }

}