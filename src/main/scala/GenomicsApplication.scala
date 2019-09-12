import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.ContentNegotiator.Alternative.ContentType
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.apache.hadoop.fs.LocalFileSystem
import org.apache.hadoop.hdfs.DistributedFileSystem
import org.apache.spark.sql.SparkSession
import org.bdgenomics.formats.avro.Feature

import scala.io.StdIn
import spray.json._

object GenomicsApplication {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("microbrewery-genome-browser").getOrCreate()
    spark.sparkContext.hadoopConfiguration.set("fs.hdfs.impl", classOf[DistributedFileSystem].getName)
    spark.sparkContext.hadoopConfiguration.set("fs.file.impl", classOf[LocalFileSystem].getName)

    val genomeReference = new GenomeReference(spark.sparkContext, args(0), args(1))

    implicit val system = ActorSystem("microbrew-genome")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    import JsonFeatureProtocol._

    val features = concat (
      path("features") {
        get {
          val result = genomeReference.featureSet.rdd.take(100).toList.map(JsonFeature.apply)
          val jsonResult = result.toJson(listFormat(jsonFeatureFormat)).prettyPrint
          complete(HttpEntity(ContentTypes.`application/json`, jsonResult))
        }
      },
      path("hello") {
        complete(HttpEntity(ContentTypes.`application/json`, "[]"))
      }
    )

    val bindingFuture = Http().bindAndHandle(features, "localhost", 8087)

    println(s"Server online at http://localhost:8087/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => {
      spark.stop()
      system.terminate()
    }) // and shutdown when done
  }
}

final case class JsonFeature(val name: String, val start: Long, val end: Long)

object JsonFeature {
  def apply(feature: Feature): JsonFeature = {
    if(feature.getName == null)  JsonFeature("null", feature.getStart, feature.getEnd)
    else JsonFeature(feature.getName, feature.getStart, feature.getEnd)
  }
}

object JsonFeatureProtocol extends DefaultJsonProtocol {
  implicit val jsonFeatureFormat = jsonFormat3(JsonFeature.apply)
}
