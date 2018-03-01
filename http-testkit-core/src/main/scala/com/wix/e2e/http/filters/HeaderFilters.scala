package com.wix.e2e.http.filters

import com.wix.e2e.http.RequestFilter

trait HeaderFilters {

  def forHeaders(header: (String, String), headers: (String, String)*): RequestFilter = { rq =>
    val requested = rq.headers.map(h => h.name() -> h.value()).toMap
    val expected = (header +: headers).toMap

    expected forall { case (k, v) => requested.get(k).contains(v) }
  }
}