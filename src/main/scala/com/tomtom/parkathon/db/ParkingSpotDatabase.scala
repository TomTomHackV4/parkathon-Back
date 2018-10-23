package com.tomtom.parkathon.db

import java.time.Instant
import java.util.Date

import com.mongodb.MongoCredential.createCredential
import com.mongodb.client.result.DeleteResult
import com.tomtom.parkathon.db.ParkingSpotDatabase.{AvgEarthRadiusMeters, DefaultLocationTolerance, MetersPerDegree}
import com.tomtom.parkathon.db.model.DbParkingSpot
import com.tomtom.parkathon.domain.{Location, ParkingSpot}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.mongodb.scala.model.{Filters, Sorts, Updates}
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
                        timeoutSeconds: Option[Int] = None): Seq[ParkingSpot] =
    queryParkingSpotsRaw(location, radiusMeters, maxAgeSeconds, timeoutSeconds)
      .map(toDomainModel)

  private def queryParkingSpotsRaw(location: Location,
                                   radiusMeters: Int,
                                   maxAgeSeconds: Int,
                                   timeoutSeconds: Option[Int] = None): Seq[DbParkingSpot] = {
    val queryFuture: Future[Seq[DbParkingSpot]] =
      collection
        .find(Filters.and(
          Filters.gte("reportingTime", nowMinusSeconds(maxAgeSeconds)),
          Filters.geoWithinCenter("location", location.longitude, location.latitude, metersToDegrees(radiusMeters)))
        )
        .sort(Sorts.descending("reportingTime"))
        .toFuture()

    Await.result(queryFuture, timeoutSeconds.map(_ seconds).getOrElse(Duration.Inf))
  }

  private def toDomainModel(dbParkingSpot: DbParkingSpot): ParkingSpot =
    ParkingSpot(
      getLocation(dbParkingSpot),
      dbParkingSpot.reportingTime.toInstant
    )

  private def getLocation(dbParkingSpot: DbParkingSpot): Location = {
    val coords: Seq[Double] = dbParkingSpot.location.getPosition.getValues.asScala.map(_.doubleValue())
    Location(coords(1), coords(0))
  }

  def deleteParkingSpot(location: Location,
                        locationToleranceMeters: Int = DefaultLocationTolerance,
                        timeoutSeconds: Option[Int] = None): Unit = {
    val deleteFuture: Future[DeleteResult] =
      collection
        .deleteMany(
          Filters.geoWithinCenter("location", location.longitude, location.latitude, metersToDegrees(locationToleranceMeters))
        )
        .toFuture()

    Await.result(deleteFuture, timeoutSeconds.map(_ seconds).getOrElse(Duration.Inf))
  }

  private def distanceTo(location: Location)(dbParkingSpot: DbParkingSpot): Double = {
    val dbLocation: Location = getLocation(dbParkingSpot)

    val latDistance = Math.toRadians(location.latitude - dbLocation.latitude)
    val lngDistance = Math.toRadians(location.longitude - dbLocation.longitude)

    val sinLat = Math.sin(latDistance / 2)
    val sinLng = Math.sin(lngDistance / 2)

    val a = sinLat * sinLat + (Math.cos(Math.toRadians(location.latitude)) * Math.cos(Math.toRadians(dbLocation.latitude)) * sinLng * sinLng)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    (AvgEarthRadiusMeters * c).toInt
  }

  // Just a very coarse conversion
  private def metersToDegrees(radiusMeters: Double): Double =
    radiusMeters / MetersPerDegree

  def createParkingSpot(location: Location,
                        locationToleranceMeters: Int = DefaultLocationTolerance,
                        timeoutSeconds: Option[Int] = None): Unit = {
    val existingParkingSpots: Seq[DbParkingSpot] =
      queryParkingSpotsRaw(location, locationToleranceMeters, Int.MaxValue, timeoutSeconds = timeoutSeconds)
    val dbAction =
      if (existingParkingSpots.isEmpty) {
        collection
          .insertOne(DbParkingSpot(location.latitude, location.longitude, new Date()))
          .toFuture()
      } else {
        val closestParkingSpot: DbParkingSpot = existingParkingSpots.minBy(distanceTo(location))
        collection
          .updateOne(Filters.eq("_id", closestParkingSpot._id), Updates.set("reportingTime", new Date()))
          .toFuture()
      }
    Await.result(dbAction, timeoutSeconds.map(_ seconds).getOrElse(Duration.Inf))
  }

  override def close(): Unit = mongoClient.close()
}

object ParkingSpotDatabase {

  val AvgEarthRadiusMeters = 6371000
  val MetersPerDegree: Double = 111000

  val DefaultLocationTolerance: Int = 25
}