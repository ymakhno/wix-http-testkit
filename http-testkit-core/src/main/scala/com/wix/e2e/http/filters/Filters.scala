package com.wix.e2e.http.filters

import com.wix.e2e.http.RequestFilter


trait Filters extends BodyFilters
              with PathFilters
              with QueryParamFilters
              with MethodFilters
              with HeaderFilters
              with Handlers {

  implicit def `RequestFilter -> FilterOps`(filter: RequestFilter) = new FilterOps(filter)
}

object Filters extends Filters

class FilterOps(val thisFilter: RequestFilter) extends AnyVal {
  def and(thatFilter: RequestFilter): RequestFilter = { rq => thisFilter(rq) && thatFilter(rq) }
  def or(thatFilter: RequestFilter): RequestFilter = { rq => thisFilter(rq) || thatFilter(rq) }
}
