package com.tomtom.parkathon.db.model

import java.util.Date

import com.mongodb.client.model.geojson.Point
import org.mongodb.scala.bson.ObjectId

private[db] case class ParkingSpot(_id: ObjectId, location: Point, reportingTime: Date, source: String)
