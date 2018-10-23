package com.tomtom.parkathon.db

import java.time.Instant
import java.util.Date

import com.mongodb.MongoCredential.createCredential
import com.tomtom.parkathon.db.model.DbParkingSpot
import com.tomtom.parkathon.domain.{Location, ParkingSpot}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.mongodb.scala.model.{Filters, Sorts}
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCollection, MongoCredential, MongoDatabase, ServerAddress}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ParkingSpotDatabase(host: String = "104.248.240.148",
                          databaseName: String = "parkathon",
                          collectionName: String = "spots") extends AutoCloseable {

  val user: String = "root" // the user name
  val source: String = "admin" // the source where the user is defined

  val password: Array[Char] = System.getProperty("mongopassword").toCharArray // the password as a character array
  val credential: MongoCredential = createCredential(user, source, password)

  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[DbParkingSpot]), DEFAULT_CODEC_REGISTRY)

  val settings: MongoClientSettings =
    MongoClientSettings
      .builder()
      .applyToClusterSettings(b => b.hosts(List(new ServerAddress(host)).asJava))
      .credential(credential)
      .codecRegistry(codecRegistry)
      .build()

  private val mongoClient: MongoClient = MongoClient(settings)
  private val database: MongoDatabase = mongoClient.getDatabase(databaseName)
  private val collection: MongoCollection[DbParkingSpot] = database.getCollection(collectionName)

  private def nowMinusSeconds(maxAgeSeconds: Int): Date =
    Date.from(Instant.now.minusSeconds(maxAgeSeconds))

  def queryParkingSpots(location: Location,
                        radiusMeters: Int,
                        maxAgeSeconds: Int,
                        limit: Option[Int] = None,
                        timeoutSeconds: Option[Int] = None): Seq[ParkingSpot] = {
    // Just a very coarse conversion
    val radiusDegrees: Double = radiusMeters / ParkingSpotDatabase.MetersPerDegree

    val queryFuture: Future[Seq[ParkingSpot]] =
      collection
        .find(Filters.and(
          Filters.gte("reportingTime", nowMinusSeconds(maxAgeSeconds)),
          Filters.geoWithinCenter("location", location.longitude, location.latitude, radiusDegrees))
        )
        .sort(Sorts.descending("reportingTime"))
        .limit(limit.getOrElse(Int.MaxValue))
        .map(toDomainModel)
        .toFuture()

    Await.result(queryFuture, timeoutSeconds.map(_ seconds).getOrElse(Duration.Inf))
  }

  private def toDomainModel(dbRecord: DbParkingSpot): ParkingSpot = {
    val coords: Seq[Double] = dbRecord.location.getPosition.getValues.asScala.map(_.doubleValue())
    ParkingSpot(
      Location(coords(1), coords(0)),
      dbRecord.reportingTime.toInstant
    )
  }

  override def close(): Unit = mongoClient.close()
}

object ParkingSpotDatabase {
  val MetersPerDegree: Double = 111000
}