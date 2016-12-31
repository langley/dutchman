package dutchman

import dutchman.api._

object marshalling {
  case class DecodingError(message: String) extends Exception(s"Error decoding json: $message")

  trait OperationWriter {
    def write[A](api: Api[A]): String
  }

  trait ResponseReader[Json] {
    def read(json: String): Json
    def error(json: String): ESError
    def readError(json: Json): Option[Seq[ESError]]
    def get(json: Json): GetResponse[Json]
    def index(json: Json): IndexResponse
    def multiGet(json: Json): MultiGetResponse
    def delete(json: Json): DeleteResponse
    def deleteIndex(json: Json): DeleteIndexResponse
    def bulk(json: Json): Seq[BulkResponse]
    def search(json: Json): SearchResponse[Json]
    def refresh(json: Json): RefreshResponse
    def scroll(json: Json): ScrollResponse[Json]
    def update(json: Json): UpdateResponse
  }
}
