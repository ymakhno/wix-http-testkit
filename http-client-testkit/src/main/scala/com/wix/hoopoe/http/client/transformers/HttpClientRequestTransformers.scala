package com.wix.hoopoe.http.client.transformers

import akka.http.scaladsl.client.RequestBuilding.RequestTransformer
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Cookie, RawHeader}
import com.wix.hoopoe.http.client.internals.AkkaClientResources

import scala.xml.Node

trait HttpClientRequestTransformers extends HttpClientContentTypes {

  def withParam(param: (String, String)): RequestTransformer = withParams(param)
  def withParams(params: (String, String)*): RequestTransformer = r =>
    r.copy(uri = r.uri
                  .withQuery( Query(r.uri.rawQueryString
                                         .map( Query(_).toSeq )
                                         .getOrElse(Nil)
                                      ++ params: _*)) )

  def withHeader(header: (String, String)): RequestTransformer = withHeaders(header)
  def withHeaders(headers: (String, String)*): RequestTransformer = appendHeaders( headers.map(p => RawHeader(p._1, p._2)) )

  def withCookie(cookie: (String, String)): RequestTransformer = withCookies(cookie)
  def withCookies(cookies: (String, String)*): RequestTransformer = appendHeaders( cookies.map(p => Cookie(p._1, p._2)) )

  def withPayload(body: String, contentType: ContentType = TextPlain): RequestTransformer = setBody(body)
  def withPayload(bytes: Array[Byte], contentType: ContentType): RequestTransformer = setBody(HttpEntity(contentType, bytes))
  def withPayload(xml: Node): RequestTransformer = setBody(HttpEntity(XmlContent, AkkaClientResources.xmlPrinter.format(xml)))
  def withPayload(entity: AnyRef): RequestTransformer = setBody(HttpEntity(JsonContent, AkkaClientResources.jsonMapper.writeValueAsString(entity)))

  def withFormData(formParams: (String, String)*): RequestTransformer = identity
//  { r: HttpRequest =>
//    val (entity, headers) = marshalToEntityAndHeaders(FormData(formParams)).right.get
//    r.copy(entity = entity, headers = headers.toList ++ r.headers)
//    r
//  }

  private def setBody(entity: RequestEntity): RequestTransformer = _.copy(entity = entity)
  private def appendHeaders[H <: HttpHeader](headers: Iterable[H]): RequestTransformer = r =>
    r.withHeaders( r.headers ++ headers/* :_**/)

  implicit class TransformerConcatenation(first: RequestTransformer) {
    def and(second: RequestTransformer): RequestTransformer = first andThen second
  }
}

trait HttpClientContentTypes {

  val TextPlain = ContentTypes.`text/plain(UTF-8)`
  val JsonContent = ContentTypes.`application/json`
  val XmlContent = ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`)
}