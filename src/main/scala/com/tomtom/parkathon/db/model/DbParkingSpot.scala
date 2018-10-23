package com.tomtom.parkathon.db.model

import java.util.Date

import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.geojson.{NamedCoordinateReferenceSystem, Point, Position}

private[db] case class DbParkingSpot(_id: ObjectId, location: Point, reportingTime: Date, source: String)

object DbParkingSpot {
  def apply(latitude: Double, longitude: Double, reportingTime: Date): DbParkingSpot = {
    val location = Point(NamedCoordinateReferenceSystem.EPSG_4326, Position(longitude, latitude))
    new DbParkingSpot(new ObjectId(), location, reportingTime, "AppUser")
  }
}