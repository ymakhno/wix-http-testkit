package com.wix.e2e.http.handlers

import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.client.sync._
import com.wix.e2e.http.json.JsonJacksonMarshaller
import com.wix.e2e.http.filters.Filters._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.e2e.http.matchers._
import com.wix.e2e.http.server.WebServerFactory.aMockWebServer
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class SimpleHandlersContractTest extends Spec {

  trait ctx extends Scope {
    val server = aMockWebServer.build.start()
    implicit val baseUri = server.baseUri
    implicit val marshaller: Marshaller = new JsonJacksonMarshaller

    val privetResponse = SimpleEntityResponse("privet!")
    val pakaResponse = SimpleEntityResponse("paka!")

  }

  "Simple handler" should {
    "allow to return string response on any request" in new ctx {
      private val validStringResponse = "Hello world"

      server.appendAll(respondOk(validStringResponse))

      get("/") must beSuccessfulWith(validStringResponse)
    }

    "allow to apply path matcher on handlers" in new ctx {
      server.appendAll(respondOk() when pathIs("/hello/world") )

      get("/hello/world") must beSuccessful
      get("/hello/world/") must beSuccessful
      get("hello/world/") must beSuccessful
      get("/hello") must beNotFound
    }

    "support wildcard in path matcher" in new ctx {
      server.appendAll(respondOk() when pathIs("*/world/*"))

      get("/hello/world/!") must beSuccessful
      get("/bye-bye/world/!") must beSuccessful
      get("/world") must beNotFound
    }

    "allow to apply query param matcher on handlers" in new ctx {
      server.appendAll(respondOk() when paramsContain("a" -> "b", "c" -> "d"))
      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beSuccessful
      get("/", but = withParam("c" -> "d")) must beNotFound
    }

    "allow to apply both path matcher and query param matcher on handlers" in new ctx {
      server.appendAll(respondOk() when paramsContain("a" -> "b", "c" -> "d") and pathIs("ololo"))

      get("/ololo", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beSuccessful
      get("/", but = withParams("a" -> "b", "c" -> "d", "x" -> "y")) must beNotFound
      get("/ololo", but = withParam("c" -> "d")) must beNotFound
    }

    "allow to respond with case class" in new ctx {
      val response = SimpleEntityResponse("privet!")

      server.appendAll(respondOk(response))

      get("/arbitrary/path") must beSuccessfulWith(response)
    }

    "allow to match via body" in new ctx {
      server.appendAll(respondOk() when bodyIs(privetResponse))

      post("/arbitrary/path", but = withPayload(privetResponse)) must beSuccessful
      post("/arbitrary/path", but = withPayload(pakaResponse)) must beNotFound
    }

    "allow to match via body with matcher" in new ctx {
      server.appendAll(respondOk() when bodyMatches(be_===(privetResponse)))

      post("/arbitrary/path", but = withPayload(privetResponse)) must beSuccessful
      post("/arbitrary/path", but = withPayload(pakaResponse)) must beNotFound
    }

    "allow to match via http method" in new ctx {
      server.appendAll(respondOk() when (methodIsGet or methodIsDelete))

      get("/arbitrary/path") must beSuccessful
      delete("/arbitrary/path") must beSuccessful
      post("/arbitrary/path") must beNotFound
      put("/arbitrary/path") must beNotFound
      patch("/arbitrary/path") must beNotFound
    }

    "allow to match via headers" in new ctx {
      server.appendAll(respondOk() when headersContain("a" -> "b", "c" -> "d"))

      get("/", but = withHeaders("a" -> "b", "c" -> "d", "x" -> "y")) must beSuccessful
      get("/", but = withHeader("c" -> "d")) must beNotFound
    }

    "allow to apply all together" in new ctx {
      val handler = respondOk() when
                      methodIsPost and
                      pathIs("/users/*") and
                      paramsContain("a" -> "x") and
                      headersContain("b" -> "y") and
                      bodyMatches(be_===(privetResponse))

      server.appendAll(handler)

      post("/users/1", but = withPayload(privetResponse) and withParam("a" -> "x") and withHeader("b" -> "y")) must beSuccessful
      post("/users/1", but = withPayload(privetResponse) and withParam("a" -> "x")) must beNotFound
      put("/users/1", but = withPayload(privetResponse) and withParam("a" -> "x")) must beNotFound
      post("/users/1", but = withPayload(privetResponse)) must beNotFound
      post("/users/1", but = withParam("a" -> "x")) must beNotFound
      get("/users/1", but = withParam("a" -> "x")) must beNotFound
      post("/users", but = withPayload(privetResponse) and withParam("a" -> "x")) must beNotFound
      post("/users/1", but = withPayload(pakaResponse) and withParam("a" -> "x")) must beNotFound
    }
  }
}

case class SimpleEntityResponse(response: String)
