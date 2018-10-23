package com.tomtom.parkathon.db

import com.mongodb.MongoCredential.createCredential
import com.tomtom.parkathon.db.model.{ParkingSpot => DbParkingSpot}
import com.tomtom.parkathon.domain.ParkingSpot
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCollection, MongoCredential, MongoDatabase, ServerAddress}

import scala.collection.JavaConverters._
import scala.concurrent.Future

class ParkingSpotDatabase(host: String = "104.248.240.148",
                          databaseName: String = "parkathon",
                          collectionName: String = "spots") {

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

  val mongoClient: MongoClient = MongoClient(settings)

  val database: MongoDatabase = mongoClient.getDatabase(databaseName)

  val collection: MongoCollection[DbParkingSpot] = database.getCollection(collectionName)

  def queryParkingSpots(latitude: Double, longitude: Double, radius: Double, limit: Option[Int] = None): Future[Seq[ParkingSpot]] =
    collection
      .find(Filters.geoWithinCenter("location", longitude, latitude, radius))
      .limit(limit.getOrElse(Int.MaxValue))
      .map(toDomainModel)
      .toFuture()

  private def toDomainModel(dbModel: DbParkingSpot): ParkingSpot = ???
}
