package com.tomtom.parkathon.rest

import java.util.Optional

import com.tomtom.parkathon.db.ParkingSpotDatabase
import com.tomtom.parkathon.domain.{Location, ParkingSpot}
import com.tomtom.parkathon.rest.ParkingSpotRestController.{DefaultMaxAgeSeconds, DefaultRadiusMeters}
import org.springframework.web.bind.annotation._

import scala.compat.java8.OptionConverters.RichOptionalGeneric

@RestController
class ParkingSpotRestController {

  val parkingSpotDatabase: ParkingSpotDatabase = new ParkingSpotDatabase()

  @GetMapping(Array("/parking-spots"))
  def getAvailableParkingSpots(@RequestParam position: String,
                               @RequestParam radiusMeters: Optional[Integer],
                               @RequestParam maxAgeSeconds: Optional[Integer]): Seq[ParkingSpot] =
    parkingSpotDatabase.queryParkingSpots(
      parseLocation(position),
      radiusMeters.asScala.map(_.intValue).getOrElse(DefaultRadiusMeters),
      maxAgeSeconds.asScala.map(_.intValue).getOrElse(DefaultMaxAgeSeconds)
    )

  private def parseLocation(position: String): Location = {
    val coords: Array[Double] = position.split(":").map(_.toDouble)
    Location(coords(0), coords(1))
  }

  @PostMapping(Array("/park-start"))
  def startParking(@RequestBody location: Location): Unit =
    parkingSpotDatabase.deleteParkingSpot(location)

  @PostMapping(Array("/park-stop"))
  def stopParking(@RequestBody location: Location): Unit =
    parkingSpotDatabase.createParkingSpot(location)
}

object ParkingSpotRestController {

  val DefaultRadiusMeters: Int = 1000
  val DefaultMaxAgeSeconds: Int = 600
}