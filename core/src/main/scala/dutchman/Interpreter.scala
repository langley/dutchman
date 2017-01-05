package dutchman

import cats.~>
import dutchman.api._
import dutchman.dsl._
import dutchman.http._
import dutchman.marshalling.{ApiDataWriter, ResponseReader}

import scala.concurrent.{ExecutionContext, Future}

class Interpreter[Json](client: HttpClient, endpoint: Endpoint, signer: ElasticRequestSigner)
                       (implicit ec: ExecutionContext, writer: ApiDataWriter, reader: ResponseReader[Json])
  extends (ElasticOp ~> Future) {

  private def execute[A](api: ApiRepresentation, fx: Json ⇒ A): Future[A] = {
    val payload = api.data.get(BulkActionsKey) collect {
      case c: Seq[_] ⇒ writer.write(c.asInstanceOf[Seq[ApiData]])
    } getOrElse writer.write(api.data)

    val request = signer.sign(endpoint, api.request).copy(
      payload = payload
    )

    client.execute(endpoint)(request) map { response ⇒
      val json = reader.read(response)
      reader.readError(json) match {
        case Some(errors) ⇒ throw ElasticErrorsException(errors)
        case _            ⇒ fx(json)
      }
    } recover {
      case e: Throwable ⇒ throw HttpError(e.getMessage)
    }
  }

  def apply[A](op: ElasticOp[A]) = {
    val api = op.api
    (op match {
      case _: DocumentExists ⇒ client.documentExists(endpoint)(api.request)
      case _: Bulk           ⇒ execute(api, reader.bulk)
      case _: Delete         ⇒ execute(api, reader.delete)
      case _: Get[_]         ⇒ execute(api, reader.get)
      case _: Index          ⇒ execute(api, reader.index)
      case _: MultiGet[_]    ⇒ execute(api, reader.multiGet)
      case _: Update         ⇒ execute(api, reader.update)
      case _: DeleteIndex    ⇒ execute(api, reader.deleteIndex)
      case _: Refresh        ⇒ execute(api, reader.refresh)
      case _: Search[_]      ⇒ execute(api, reader.search)
    }) map (_.asInstanceOf[A])
  }
}